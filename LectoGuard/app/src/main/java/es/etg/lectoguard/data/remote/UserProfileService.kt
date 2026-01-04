package es.etg.lectoguard.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import es.etg.lectoguard.domain.model.UserProfile
import kotlinx.coroutines.tasks.await

class UserProfileService(
    private val firestore: FirebaseFirestore
) {
    private fun usersCollection() = firestore.collection("users")

    suspend fun getProfile(uid: String): UserProfile? {
        return try {
            val snap = usersCollection().document(uid).get().await()
            if (snap.exists()) {
                // Mapear manualmente para evitar problemas con deserialización
                val data = snap.data
                val displayName = data?.get("displayName") as? String ?: ""
                val email = data?.get("email") as? String ?: ""
                val avatarUrl = (data?.get("avatarUrl") as? String)?.takeIf { it.isNotBlank() }
                val bio = (data?.get("bio") as? String)?.takeIf { it.isNotBlank() }
                
                android.util.Log.d("UserProfileService", "Perfil obtenido para UID $uid: avatarUrl=${avatarUrl ?: "null"}, displayName=$displayName")
                
                UserProfile(
                    uid = uid,
                    displayName = displayName,
                    email = email,
                    avatarUrl = avatarUrl,
                    bio = bio,
                    fcmToken = (data?.get("fcmToken") as? String)?.takeIf { it.isNotBlank() },
                    booksRead = (data?.get("booksRead") as? Number)?.toInt() ?: 0,
                    followers = (data?.get("followers") as? Number)?.toInt() ?: 0,
                    following = (data?.get("following") as? Number)?.toInt() ?: 0,
                    createdAt = (data?.get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis(),
                    updatedAt = (data?.get("updatedAt") as? Number)?.toLong() ?: System.currentTimeMillis()
                )
            } else {
                android.util.Log.w("UserProfileService", "Perfil no existe para UID: $uid")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("UserProfileService", "Error obteniendo perfil para UID $uid: ${e.message}", e)
            e.printStackTrace()
            null
        }
    }

    suspend fun upsertProfile(profile: UserProfile) {
        try {
            val doc = usersCollection().document(profile.uid)
            val data = hashMapOf<String, Any>(
                "uid" to profile.uid,
                "displayName" to profile.displayName,
                "email" to profile.email,
                "booksRead" to profile.booksRead,
                "followers" to profile.followers,
                "following" to profile.following,
                "createdAt" to profile.createdAt,
                "updatedAt" to System.currentTimeMillis()
            )
            
            // Solo agregar avatarUrl y bio si no son null o vacíos
            profile.avatarUrl?.takeIf { it.isNotBlank() }?.let { data["avatarUrl"] = it }
            profile.bio?.takeIf { it.isNotBlank() }?.let { data["bio"] = it }
            
            android.util.Log.d("UserProfileService", "Guardando perfil para UID ${profile.uid}: avatarUrl=${profile.avatarUrl?.takeIf { it.isNotBlank() } ?: "null"}")
            doc.set(data).await()
            android.util.Log.d("UserProfileService", "Perfil actualizado exitosamente para UID: ${profile.uid}")
        } catch (e: Exception) {
            android.util.Log.e("UserProfileService", "Error actualizando perfil para UID ${profile.uid}: ${e.message}", e)
            throw e // Re-lanzar el error para que se maneje en el repositorio
        }
    }
    
    suspend fun searchUsers(query: String, limit: Int = 20, excludeUid: String? = null): List<UserProfile> {
        return try {
            val queryLower = query.lowercase().trim()
            if (queryLower.isEmpty()) return emptyList()
            
            android.util.Log.d("UserProfileService", "Buscando usuarios con query: $queryLower")
            
            // Obtener todos los usuarios y filtrar en memoria
            // Esto es necesario porque Firestore es case-sensitive
            // y no podemos hacer búsqueda case-insensitive directamente
            val allUsers = usersCollection()
                .get()
                .await()
            
            android.util.Log.d("UserProfileService", "Total usuarios encontrados en Firestore: ${allUsers.size()}")
            
            val results = mutableListOf<UserProfile>()
            
            allUsers.documents.forEach { doc ->
                try {
                    // Mapear manualmente para evitar problemas con campos null
                    val data = doc.data
                    val uid = data?.get("uid") as? String ?: doc.id
                    val displayName = data?.get("displayName") as? String ?: ""
                    val email = data?.get("email") as? String ?: ""
                    
                    if (displayName.isEmpty() && email.isEmpty()) {
                        android.util.Log.w("UserProfileService", "Usuario sin nombre ni email: $uid")
                        return@forEach
                    }
                    
                    val profile = UserProfile(
                        uid = uid,
                        displayName = displayName,
                        email = email,
                        avatarUrl = data?.get("avatarUrl") as? String,
                        bio = data?.get("bio") as? String,
                        booksRead = (data?.get("booksRead") as? Number)?.toInt() ?: 0,
                        followers = (data?.get("followers") as? Number)?.toInt() ?: 0,
                        following = (data?.get("following") as? Number)?.toInt() ?: 0,
                        createdAt = (data?.get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis(),
                        updatedAt = (data?.get("updatedAt") as? Number)?.toLong() ?: System.currentTimeMillis()
                    )
                    
                    // Excluir el usuario actual si se especifica
                    if (excludeUid != null && profile.uid == excludeUid) {
                        return@forEach
                    }
                    
                    // Buscar en displayName (case-insensitive)
                    val nameMatch = profile.displayName.lowercase().contains(queryLower)
                    
                    // Buscar en email (case-insensitive)
                    val emailMatch = profile.email.lowercase().contains(queryLower)
                    
                    if (nameMatch || emailMatch) {
                        android.util.Log.d("UserProfileService", "Usuario encontrado: ${profile.displayName} (${profile.email})")
                        results.add(profile)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("UserProfileService", "Error procesando documento ${doc.id}: ${e.message}")
                    e.printStackTrace()
                }
            }
            
            android.util.Log.d("UserProfileService", "Usuarios encontrados después de filtrar: ${results.size}")
            
            // Ordenar: primero los que empiezan con la query, luego los que contienen
            results.sortedWith(compareBy<UserProfile> { profile ->
                val nameLower = profile.displayName.lowercase()
                val emailLower = profile.email.lowercase()
                when {
                    nameLower.startsWith(queryLower) -> 0
                    emailLower.startsWith(queryLower) -> 1
                    nameLower.contains(queryLower) -> 2
                    emailLower.contains(queryLower) -> 3
                    else -> 4
                }
            }).take(limit)
        } catch (e: Exception) {
            android.util.Log.e("UserProfileService", "Error en searchUsers: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun getAllUsers(limit: Int = 50, excludeUid: String? = null): List<UserProfile> {
        return try {
            android.util.Log.d("UserProfileService", "Obteniendo todos los usuarios (limit: $limit, excludeUid: $excludeUid)")
            
            val results = usersCollection()
                .limit(limit.toLong())
                .get()
                .await()
            
            android.util.Log.d("UserProfileService", "Total documentos obtenidos: ${results.size()}")
            
            results.documents.mapNotNull { doc ->
                try {
                    // Mapear manualmente para evitar problemas con campos null
                    val data = doc.data
                    val uid = data?.get("uid") as? String ?: doc.id
                    val displayName = data?.get("displayName") as? String ?: ""
                    val email = data?.get("email") as? String ?: ""
                    
                    if (displayName.isEmpty() && email.isEmpty()) {
                        android.util.Log.w("UserProfileService", "Usuario sin nombre ni email: $uid")
                        return@mapNotNull null
                    }
                    
                    // Excluir el usuario actual si se especifica
                    if (excludeUid != null && uid == excludeUid) {
                        return@mapNotNull null
                    }
                    
                    UserProfile(
                        uid = uid,
                        displayName = displayName,
                        email = email,
                        avatarUrl = data?.get("avatarUrl") as? String,
                        bio = data?.get("bio") as? String,
                        booksRead = (data?.get("booksRead") as? Number)?.toInt() ?: 0,
                        followers = (data?.get("followers") as? Number)?.toInt() ?: 0,
                        following = (data?.get("following") as? Number)?.toInt() ?: 0,
                        createdAt = (data?.get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis(),
                        updatedAt = (data?.get("updatedAt") as? Number)?.toLong() ?: System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    android.util.Log.e("UserProfileService", "Error procesando documento ${doc.id}: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("UserProfileService", "Error en getAllUsers: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
}


