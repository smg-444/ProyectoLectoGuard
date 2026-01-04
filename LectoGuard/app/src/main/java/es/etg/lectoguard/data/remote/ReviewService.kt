package es.etg.lectoguard.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import es.etg.lectoguard.domain.model.Review
import kotlinx.coroutines.tasks.await

class ReviewService(
    private val firestore: FirebaseFirestore
) {
    private fun reviewsCollection() = firestore.collection("reviews")
    
    suspend fun saveReview(review: Review): String? {
        return try {
            val doc = if (review.id.isEmpty()) {
                reviewsCollection().document()
            } else {
                reviewsCollection().document(review.id)
            }
            
            val data = hashMapOf(
                "bookId" to review.bookId,
                "userId" to review.userId,
                "userName" to review.userName,
                "userAvatarUrl" to review.userAvatarUrl,
                "rating" to review.rating,
                "text" to review.text,
                "likes" to review.likes,
                "likedBy" to review.likedBy,
                "createdAt" to review.createdAt
            )
            
            doc.set(data).await()
            val reviewId = doc.id
            
            // Crear feed item para los seguidores del usuario
            // Nota: La información del libro se pasa desde el ViewModel/Activity
            android.util.Log.d("ReviewService", "Reseña guardada exitosamente: $reviewId")
            
            reviewId
        } catch (e: Exception) {
            android.util.Log.e("ReviewService", "Error guardando reseña: ${e.message}")
            null
        }
    }
    
    suspend fun getBookReviews(bookId: Int, limit: Int = 50): List<Review> {
        return try {
            val results = reviewsCollection()
                .whereEqualTo("bookId", bookId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            
            results.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                Review(
                    id = doc.id,
                    bookId = (data["bookId"] as? Number)?.toInt() ?: bookId,
                    userId = data["userId"] as? String ?: "",
                    userName = data["userName"] as? String ?: "",
                    userAvatarUrl = data["userAvatarUrl"] as? String,
                    rating = (data["rating"] as? Number)?.toInt() ?: 0,
                    text = data["text"] as? String ?: "",
                    likes = (data["likes"] as? Number)?.toInt() ?: 0,
                    likedBy = (data["likedBy"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                    createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("ReviewService", "Error obteniendo reseñas: ${e.message}")
            emptyList()
        }
    }
    
    suspend fun toggleLikeReview(reviewId: String, userId: String, isLiked: Boolean): Boolean {
        return try {
            val doc = reviewsCollection().document(reviewId)
            val review = doc.get().await()
            
            if (!review.exists()) return false
            
            val data = review.data ?: return false
            val currentLikes = (data["likes"] as? Number)?.toInt() ?: 0
            val likedBy = (data["likedBy"] as? List<*>)?.mapNotNull { it as? String }?.toMutableList() ?: mutableListOf()
            
            if (isLiked) {
                // Añadir like
                if (!likedBy.contains(userId)) {
                    likedBy.add(userId)
                    doc.update("likes", currentLikes + 1, "likedBy", likedBy).await()
                }
            } else {
                // Quitar like
                if (likedBy.contains(userId)) {
                    likedBy.remove(userId)
                    doc.update("likes", currentLikes - 1, "likedBy", likedBy).await()
                }
            }
            
            true
        } catch (e: Exception) {
            android.util.Log.e("ReviewService", "Error dando like: ${e.message}")
            false
        }
    }
    
    suspend fun updateReview(reviewId: String, userId: String, newText: String, newRating: Int): Boolean {
        return try {
            val doc = reviewsCollection().document(reviewId)
            val review = doc.get().await()
            
            if (!review.exists()) return false
            
            val data = review.data
            val reviewUserId = data?.get("userId") as? String
            
            // Solo el autor puede editar su reseña
            if (reviewUserId == userId) {
                doc.update(
                    "text", newText,
                    "rating", newRating,
                    "updatedAt", System.currentTimeMillis()
                ).await()
                android.util.Log.d("ReviewService", "Reseña actualizada exitosamente: $reviewId")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("ReviewService", "Error actualizando reseña: ${e.message}")
            false
        }
    }
    
    suspend fun deleteReview(reviewId: String, userId: String): Boolean {
        return try {
            val doc = reviewsCollection().document(reviewId)
            val review = doc.get().await()
            
            if (!review.exists()) return false
            
            val data = review.data
            val reviewUserId = data?.get("userId") as? String
            
            // Solo el autor puede eliminar su reseña
            if (reviewUserId == userId) {
                doc.delete().await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("ReviewService", "Error eliminando reseña: ${e.message}")
            false
        }
    }
}

