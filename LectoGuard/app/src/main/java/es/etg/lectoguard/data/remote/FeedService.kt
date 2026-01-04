package es.etg.lectoguard.data.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import es.etg.lectoguard.domain.model.FeedItem
import es.etg.lectoguard.domain.model.FeedItemType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FeedService(
    private val firestore: FirebaseFirestore
) {
    private fun feedsCollection() = firestore.collection("feeds")
    
    /**
     * Crea un item en el feed de todos los seguidores de un usuario
     * cuando ese usuario realiza una acción (guarda libro, valora, reseña, etc.)
     */
    suspend fun createFeedItemForFollowers(
        actorUserId: String,
        actorUserName: String,
        actorAvatarUrl: String?,
        feedItem: FeedItem
    ): Boolean {
        return try {
            // Obtener todos los seguidores del usuario que realiza la acción
            // El campo en FollowService es "targetUid", no "targetId"
            val followersQuery = firestore.collectionGroup("following")
                .whereEqualTo("targetUid", actorUserId)
                .get()
                .await()
            
            val followers = followersQuery.documents.mapNotNull { doc ->
                // El userId está en el path: follows/{userId}/following/{targetUid}
                doc.reference.parent.parent?.id
            }
            
            Log.d("FeedService", "Creando feed item para ${followers.size} seguidores de $actorUserId")
            
            if (followers.isEmpty()) {
                Log.w("FeedService", "No se encontraron seguidores para $actorUserId")
                return false
            }
            
            // Crear el item en el feed de cada seguidor
            val batch = firestore.batch()
            followers.forEach { followerId ->
                val feedItemRef = feedsCollection()
                    .document(followerId)
                    .collection("items")
                    .document()
                
                val data = hashMapOf<String, Any?>(
                    "userId" to actorUserId,
                    "userName" to actorUserName,
                    "userAvatarUrl" to actorAvatarUrl,
                    "type" to feedItem.type.name,
                    "timestamp" to feedItem.timestamp
                )
                
                // Agregar campos opcionales solo si no son null
                feedItem.bookId?.let { data["bookId"] = it }
                feedItem.bookTitle?.let { data["bookTitle"] = it }
                feedItem.bookCoverUrl?.let { data["bookCoverUrl"] = it }
                feedItem.rating?.let { data["rating"] = it }
                feedItem.reviewText?.let { data["reviewText"] = it }
                feedItem.reviewId?.let { data["reviewId"] = it }
                feedItem.targetUserId?.let { data["targetUserId"] = it }
                feedItem.targetUserName?.let { data["targetUserName"] = it }
                
                batch.set(feedItemRef, data)
            }
            
            batch.commit().await()
            Log.d("FeedService", "Feed items creados exitosamente para ${followers.size} seguidores")
            true
        } catch (e: Exception) {
            Log.e("FeedService", "Error creando feed items: ${e.message}", e)
            false
        }
    }
    
    /**
     * Obtiene el feed de un usuario (actividades de usuarios que sigue)
     */
    suspend fun getUserFeed(userId: String, limit: Int = 50): List<FeedItem> {
        return try {
            val snapshot = feedsCollection()
                .document(userId)
                .collection("items")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                mapFeedItem(doc)
            }
        } catch (e: Exception) {
            Log.e("FeedService", "Error obteniendo feed: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Obtiene más items del feed usando paginación (después del último timestamp)
     */
    suspend fun getMoreFeedItems(userId: String, lastTimestamp: Long, limit: Int = 20): List<FeedItem> {
        return try {
            val snapshot = feedsCollection()
                .document(userId)
                .collection("items")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .whereLessThan("timestamp", lastTimestamp)
                .limit(limit.toLong())
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                mapFeedItem(doc)
            }
        } catch (e: Exception) {
            Log.e("FeedService", "Error obteniendo más feed items: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Observa el feed de un usuario en tiempo real
     */
    fun observeUserFeed(userId: String, limit: Int = 50): Flow<List<FeedItem>> {
        return callbackFlow {
            val listenerRegistration = feedsCollection()
                .document(userId)
                .collection("items")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("FeedService", "Error observando feed: ${e.message}", e)
                        close(e)
                        return@addSnapshotListener
                    }
                    
                    val feedItems = snapshot?.documents?.mapNotNull { doc ->
                        mapFeedItem(doc)
                    } ?: emptyList()
                    
                    trySend(feedItems)
                }
            
            awaitClose { listenerRegistration.remove() }
        }
    }
    
    /**
     * Mapea un DocumentSnapshot a FeedItem
     */
    private fun mapFeedItem(doc: com.google.firebase.firestore.DocumentSnapshot): FeedItem? {
        return try {
            val data = doc.data ?: return null
            
            val typeString = data["type"] as? String ?: "BOOK_SAVED"
            val feedItemType = try {
                FeedItemType.valueOf(typeString)
            } catch (e: Exception) {
                Log.w("FeedService", "Tipo de feed item desconocido: $typeString, usando BOOK_SAVED por defecto")
                FeedItemType.BOOK_SAVED
            }
            
            val feedItem = FeedItem(
                id = doc.id,
                userId = data["userId"] as? String ?: "",
                userName = data["userName"] as? String ?: "",
                userAvatarUrl = data["userAvatarUrl"] as? String,
                type = feedItemType,
                timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                bookId = (data["bookId"] as? Number)?.toInt(),
                bookTitle = data["bookTitle"] as? String,
                bookCoverUrl = data["bookCoverUrl"] as? String,
                rating = (data["rating"] as? Number)?.toInt(),
                reviewText = data["reviewText"] as? String,
                reviewId = data["reviewId"] as? String,
                targetUserId = data["targetUserId"] as? String,
                targetUserName = data["targetUserName"] as? String
            )
            
            Log.d("FeedService", "Feed item mapeado: id=${feedItem.id}, type=${feedItem.type.name}, userId=${feedItem.userId}, targetUserId=${feedItem.targetUserId}, targetUserName=${feedItem.targetUserName}")
            
            feedItem
        } catch (e: Exception) {
            Log.e("FeedService", "Error mapeando feed item ${doc.id}: ${e.message}", e)
            null
        }
    }
}

