package es.etg.lectoguard.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import es.etg.lectoguard.domain.model.Rating
import kotlinx.coroutines.tasks.await

class RatingService(
    private val firestore: FirebaseFirestore
) {
    private fun ratingsCollection() = firestore.collection("ratings")
    
    suspend fun saveRating(rating: Rating): Boolean {
        return try {
            val doc = ratingsCollection()
                .document("${rating.bookId}_${rating.userId}")
            
            val data = hashMapOf(
                "bookId" to rating.bookId,
                "userId" to rating.userId,
                "rating" to rating.rating,
                "createdAt" to rating.createdAt
            )
            
            doc.set(data).await()
            true
        } catch (e: Exception) {
            android.util.Log.e("RatingService", "Error guardando valoración: ${e.message}")
            false
        }
    }
    
    suspend fun getUserRating(bookId: Int, userId: String): Rating? {
        return try {
            val doc = ratingsCollection()
                .document("${bookId}_${userId}")
                .get()
                .await()
            
            if (doc.exists()) {
                val data = doc.data ?: return null
                Rating(
                    id = doc.id,
                    bookId = (data["bookId"] as? Number)?.toInt() ?: bookId,
                    userId = data["userId"] as? String ?: userId,
                    rating = (data["rating"] as? Number)?.toInt() ?: 0,
                    createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("RatingService", "Error obteniendo valoración: ${e.message}")
            null
        }
    }
    
    suspend fun getBookRatings(bookId: Int): List<Rating> {
        return try {
            val results = ratingsCollection()
                .whereEqualTo("bookId", bookId)
                .get()
                .await()
            
            results.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                Rating(
                    id = doc.id,
                    bookId = (data["bookId"] as? Number)?.toInt() ?: bookId,
                    userId = data["userId"] as? String ?: "",
                    rating = (data["rating"] as? Number)?.toInt() ?: 0,
                    createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("RatingService", "Error obteniendo valoraciones: ${e.message}")
            emptyList()
        }
    }
    
    suspend fun getAverageRating(bookId: Int): Double {
        return try {
            val ratings = getBookRatings(bookId)
            if (ratings.isEmpty()) 0.0
            else ratings.map { it.rating }.average()
        } catch (e: Exception) {
            0.0
        }
    }
}

