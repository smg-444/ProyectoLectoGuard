package es.etg.lectoguard.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import es.etg.lectoguard.data.local.UserDao
import es.etg.lectoguard.data.local.UserEntity
import es.etg.lectoguard.data.remote.FollowService
import es.etg.lectoguard.data.remote.UserProfileService
import es.etg.lectoguard.domain.model.FollowCounts
import es.etg.lectoguard.domain.model.UserProfile
import kotlinx.coroutines.tasks.await
 
class UserRepository(
    private val userDao: UserDao,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val profileService by lazy { UserProfileService(firestore) }
    private val followService by lazy { FollowService(firestore) }

    suspend fun login(email: String, password: String): UserEntity? {
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
        return userDao.getUserById(id.toInt())
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
} 