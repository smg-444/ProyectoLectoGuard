package es.etg.lectoguard.data.remote

import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FollowService(
    private val firestore: FirebaseFirestore
) {
    private fun followingCollection(uid: String) =
        firestore.collection("follows").document(uid).collection("following")

    suspend fun follow(selfUid: String, targetUid: String) {
        if (selfUid == targetUid) return
        val doc = followingCollection(selfUid).document(targetUid)
        doc.set(
            mapOf(
                "createdAt" to System.currentTimeMillis(),
                "targetUid" to targetUid
            )
        ).await()
    }

    suspend fun unfollow(selfUid: String, targetUid: String) {
        followingCollection(selfUid).document(targetUid).delete().await()
    }

    suspend fun isFollowing(selfUid: String, targetUid: String): Boolean {
        return try {
            val snap = followingCollection(selfUid).document(targetUid).get().await()
            snap.exists()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getFollowingCount(uid: String): Int {
        return try {
            val snaps = followingCollection(uid).get().await()
            snaps.size()
        } catch (e: Exception) {
            0
        }
    }

    suspend fun getFollowingList(uid: String): List<String> {
        return try {
            val snaps = followingCollection(uid).get().await()
            snaps.documents.mapNotNull { doc ->
                doc.data?.get("targetUid") as? String
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getFollowersCount(uid: String): Int {
        // Consulta collectionGroup 'following' filtrando por campo 'targetUid'
        return try {
            val snaps = firestore.collectionGroup("following")
                .whereEqualTo("targetUid", uid)
                .get()
                .await()
            val count = snaps.size()
            android.util.Log.d("FollowService", "getFollowersCount para $uid: $count")
            count
        } catch (e: Exception) {
            android.util.Log.e("FollowService", "Error obteniendo seguidores para $uid: ${e.message}")
            e.printStackTrace()
            0
        }
    }
}


