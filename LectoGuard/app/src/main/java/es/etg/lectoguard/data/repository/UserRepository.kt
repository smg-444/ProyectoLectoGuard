package es.etg.lectoguard.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import es.etg.lectoguard.data.local.UserDao
import es.etg.lectoguard.data.local.UserEntity
import es.etg.lectoguard.data.remote.FollowService
import es.etg.lectoguard.data.remote.StorageService
import es.etg.lectoguard.data.remote.UserProfileService
import es.etg.lectoguard.domain.model.FollowCounts
import es.etg.lectoguard.domain.model.UserProfile
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuthException
import android.util.Log
 
class UserRepository(
    private val userDao: UserDao,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    private val profileService by lazy { UserProfileService(firestore) }
    private val followService by lazy { FollowService(firestore) }
    private val storageService by lazy { StorageService(storage) }

    suspend fun login(email: String, password: String, isOnline: Boolean = true): UserEntity? {
        return try {
            if (isOnline) {
                // Login online con Firebase
                auth.signInWithEmailAndPassword(email, password).await()
                val uid = auth.currentUser?.uid ?: return null

                // Asegurar perfil en Firestore
                val existingProfile = profileService.getProfile(uid)
                if (existingProfile == null) {
                    val profile = UserProfile(
                        uid = uid,
                        displayName = email.substringBefore("@"),
                        email = email
                    )
                    profileService.upsertProfile(profile)
                }

                // Usuario local para id entero
                val existingLocal = userDao.getUserByEmail(email)
                if (existingLocal != null) return existingLocal
                val newUser = UserEntity(
                    name = email.substringBefore("@"),
                    email = email,
                    phone = "",
                    password = "",
                    signupDate = System.currentTimeMillis().toString()
                )
                val id = userDao.insert(newUser)
                userDao.getUserById(id.toInt())
            } else {
                // Login offline: usar datos locales
                Log.d("UserRepository", "Intentando login offline para: $email")
                val localUser = userDao.getUserByEmail(email)
                if (localUser != null) {
                    Log.d("UserRepository", "Usuario encontrado localmente: ${localUser.name}")
                    localUser
                } else {
                    Log.w("UserRepository", "Usuario no encontrado localmente")
                    null
                }
            }
        } catch (e: FirebaseAuthException) {
            Log.e("UserRepository", "Error de autenticación: ${e.message}", e)
            null
        } catch (e: Exception) {
            Log.e("UserRepository", "Error inesperado en login: ${e.message}", e)
            null
        }
    }
    
    suspend fun checkExistingSession(): UserEntity? {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val email = currentUser.email
                if (email != null) {
                    val localUser = userDao.getUserByEmail(email)
                    if (localUser != null) {
                        Log.d("UserRepository", "Sesión existente encontrada: ${localUser.name}")
                        return localUser
                    }
                }
            }
            null
        } catch (e: Exception) {
            Log.e("UserRepository", "Error verificando sesión: ${e.message}", e)
            null
        }
    }

    suspend fun register(user: UserEntity): Long {
        auth.createUserWithEmailAndPassword(user.email, user.password).await()
        val uid = auth.currentUser?.uid ?: ""
        // Crear perfil en Firestore
        val profile = UserProfile(
            uid = uid,
            displayName = user.name,
            email = user.email
        )
        profileService.upsertProfile(profile)
        // Persistencia local
        return userDao.insert(user.copy(password = ""))
    }

    suspend fun getUserById(id: Int) = userDao.getUserById(id)

    suspend fun getRemoteProfile(uid: String) = profileService.getProfile(uid)

    suspend fun updateUserProfile(profile: UserProfile) = profileService.upsertProfile(profile)
    
    suspend fun updateProfileWithAvatar(uid: String, imageUri: Uri, bio: String?): Boolean {
        return try {
            android.util.Log.d("UserRepository", "Actualizando perfil con avatar para: $uid")
            
            // Subir avatar
            val avatarUrl = storageService.uploadAvatar(uid, imageUri)
            if (avatarUrl == null) {
                android.util.Log.w("UserRepository", "No se pudo subir el avatar, pero continuamos con la actualización")
            } else {
                android.util.Log.d("UserRepository", "Avatar subido exitosamente: $avatarUrl")
            }
            
            // Obtener perfil actual o crear uno nuevo si no existe
            var currentProfile = profileService.getProfile(uid)
            if (currentProfile == null) {
                android.util.Log.w("UserRepository", "Perfil no existe para UID: $uid, creando uno nuevo")
                // Obtener email del usuario autenticado
                val user = auth.currentUser
                val email = user?.email ?: ""
                val displayName = user?.displayName ?: email.substringBefore("@")
                
                // Crear perfil básico
                currentProfile = UserProfile(
                    uid = uid,
                    displayName = displayName,
                    email = email,
                    avatarUrl = null,
                    bio = null,
                    booksRead = 0,
                    followers = 0,
                    following = 0,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                android.util.Log.d("UserRepository", "Perfil básico creado para: $displayName ($email)")
            }
            
            // Actualizar perfil con nuevo avatar (si se subió) y bio
            val updatedProfile = currentProfile.copy(
                avatarUrl = avatarUrl ?: currentProfile.avatarUrl, // Mantener el avatar anterior si falla la subida
                bio = bio ?: currentProfile.bio, // Mantener bio anterior si no se proporciona nueva
                updatedAt = System.currentTimeMillis()
            )
            
            // Guardar en Firestore
            profileService.upsertProfile(updatedProfile)
            
            android.util.Log.d("UserRepository", "Perfil actualizado exitosamente en Firestore")
            true
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Error actualizando perfil: ${e.message}", e)
            e.printStackTrace()
            false
        }
    }
    
    suspend fun updateProfileBio(uid: String, bio: String): Boolean {
        return try {
            android.util.Log.d("UserRepository", "Actualizando bio para UID: $uid")
            
            // Obtener perfil actual o crear uno nuevo si no existe
            var currentProfile = profileService.getProfile(uid)
            if (currentProfile == null) {
                android.util.Log.w("UserRepository", "Perfil no existe para UID: $uid, creando uno nuevo")
                // Obtener email del usuario autenticado
                val user = auth.currentUser
                val email = user?.email ?: ""
                val displayName = user?.displayName ?: email.substringBefore("@")
                
                // Crear perfil básico
                currentProfile = UserProfile(
                    uid = uid,
                    displayName = displayName,
                    email = email,
                    avatarUrl = null,
                    bio = null,
                    booksRead = 0,
                    followers = 0,
                    following = 0,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                android.util.Log.d("UserRepository", "Perfil básico creado para: $displayName ($email)")
            }
            
            val updatedProfile = currentProfile.copy(
                bio = bio,
                updatedAt = System.currentTimeMillis()
            )
            profileService.upsertProfile(updatedProfile)
            android.util.Log.d("UserRepository", "Bio actualizada exitosamente")
            true
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Error actualizando bio: ${e.message}", e)
            e.printStackTrace()
            false
        }
    }

    suspend fun followUser(selfUid: String, targetUid: String) =
        followService.follow(selfUid, targetUid)

    suspend fun unfollowUser(selfUid: String, targetUid: String) =
        followService.unfollow(selfUid, targetUid)

    suspend fun isFollowing(selfUid: String, targetUid: String) =
        followService.isFollowing(selfUid, targetUid)

    suspend fun getFollowCounts(uid: String): FollowCounts =
        FollowCounts(
            followers = followService.getFollowersCount(uid),
            following = followService.getFollowingCount(uid)
        )
    
    suspend fun searchUsers(query: String, limit: Int = 20, excludeUid: String? = null) = 
        profileService.searchUsers(query, limit, excludeUid)
    
    suspend fun getAllUsers(limit: Int = 50, excludeUid: String? = null) = profileService.getAllUsers(limit, excludeUid)
} 