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
            if (snap.exists()) snap.toObject(UserProfile::class.java) else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun upsertProfile(profile: UserProfile) {
        try {
            val doc = usersCollection().document(profile.uid)
            val data = hashMapOf(
                "uid" to profile.uid,
                "displayName" to profile.displayName,
                "email" to profile.email,
                "avatarUrl" to profile.avatarUrl,
                "bio" to profile.bio,
                "booksRead" to profile.booksRead,
                "followers" to profile.followers,
                "following" to profile.following,
                "createdAt" to profile.createdAt,
                "updatedAt" to System.currentTimeMillis()
            )
            doc.set(data).await()
        } catch (e: Exception) {
            // Ignorado en primera fase para no interrumpir el login/registro
        }
    }
}


