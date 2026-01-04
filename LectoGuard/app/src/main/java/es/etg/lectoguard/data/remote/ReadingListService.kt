package es.etg.lectoguard.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import es.etg.lectoguard.domain.model.ReadingList
import kotlinx.coroutines.tasks.await
import android.util.Log

class ReadingListService(
    private val firestore: FirebaseFirestore
) {
    private fun readingListsCollection() = firestore.collection("reading_lists")
    private fun listFollowersCollection(listId: String) = 
        readingListsCollection().document(listId).collection("followers")

    /**
     * Guarda o actualiza una lista de lectura
     */
    suspend fun saveReadingList(list: ReadingList): String? {
        return try {
            val doc = if (list.id.isEmpty()) {
                readingListsCollection().document()
            } else {
                readingListsCollection().document(list.id)
            }

            val data = hashMapOf<String, Any>(
                "userId" to list.userId,
                "name" to list.name,
                "description" to list.description,
                "isPublic" to list.isPublic,
                "createdAt" to list.createdAt,
                "updatedAt" to System.currentTimeMillis(),
                "bookIds" to list.bookIds,
                "followerCount" to list.followerCount
            )

            doc.set(data).await()
            val listId = doc.id
            Log.d("ReadingListService", "Lista guardada exitosamente: $listId")
            listId
        } catch (e: Exception) {
            Log.e("ReadingListService", "Error guardando lista: ${e.message}", e)
            null
        }
    }

    /**
     * Obtiene una lista por su ID
     */
    suspend fun getReadingList(listId: String): ReadingList? {
        return try {
            val doc = readingListsCollection().document(listId).get().await()
            if (doc.exists()) {
                val data = doc.data ?: return null
                ReadingList(
                    id = doc.id,
                    userId = data["userId"] as? String ?: "",
                    name = data["name"] as? String ?: "",
                    description = data["description"] as? String ?: "",
                    isPublic = data["isPublic"] as? Boolean ?: false,
                    createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                    updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                    bookIds = (data["bookIds"] as? List<*>?)?.mapNotNull { (it as? Number)?.toInt() } ?: emptyList(),
                    followerCount = (data["followerCount"] as? Number)?.toInt() ?: 0
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("ReadingListService", "Error obteniendo lista: ${e.message}", e)
            null
        }
    }

    /**
     * Obtiene todas las listas públicas
     */
    suspend fun getPublicReadingLists(limit: Int = 50): List<ReadingList> {
        return try {
            val query = readingListsCollection()
                .whereEqualTo("isPublic", true)
                .orderBy("followerCount", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .orderBy("updatedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            query.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                ReadingList(
                    id = doc.id,
                    userId = data["userId"] as? String ?: "",
                    name = data["name"] as? String ?: "",
                    description = data["description"] as? String ?: "",
                    isPublic = data["isPublic"] as? Boolean ?: false,
                    createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                    updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                    bookIds = (data["bookIds"] as? List<*>?)?.mapNotNull { (it as? Number)?.toInt() } ?: emptyList(),
                    followerCount = (data["followerCount"] as? Number)?.toInt() ?: 0
                )
            }
        } catch (e: Exception) {
            Log.e("ReadingListService", "Error obteniendo listas públicas: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Obtiene todas las listas de un usuario
     */
    suspend fun getUserReadingLists(userId: String): List<ReadingList> {
        return try {
            val query = readingListsCollection()
                .whereEqualTo("userId", userId)
                .orderBy("updatedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            query.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                ReadingList(
                    id = doc.id,
                    userId = data["userId"] as? String ?: "",
                    name = data["name"] as? String ?: "",
                    description = data["description"] as? String ?: "",
                    isPublic = data["isPublic"] as? Boolean ?: false,
                    createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                    updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                    bookIds = (data["bookIds"] as? List<*>?)?.mapNotNull { (it as? Number)?.toInt() } ?: emptyList(),
                    followerCount = (data["followerCount"] as? Number)?.toInt() ?: 0
                )
            }
        } catch (e: Exception) {
            Log.e("ReadingListService", "Error obteniendo listas del usuario: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Sigue una lista (agrega el usuario a los seguidores)
     */
    suspend fun followList(listId: String, userId: String): Boolean {
        return try {
            // Agregar a la subcolección de seguidores
            listFollowersCollection(listId).document(userId).set(
                hashMapOf(
                    "userId" to userId,
                    "followedAt" to System.currentTimeMillis()
                )
            ).await()

            // Incrementar contador de seguidores
            readingListsCollection().document(listId)
                .update("followerCount", com.google.firebase.firestore.FieldValue.increment(1))
                .await()

            Log.d("ReadingListService", "Usuario $userId ahora sigue la lista $listId")
            true
        } catch (e: Exception) {
            Log.e("ReadingListService", "Error siguiendo lista: ${e.message}", e)
            false
        }
    }

    /**
     * Deja de seguir una lista
     */
    suspend fun unfollowList(listId: String, userId: String): Boolean {
        return try {
            // Eliminar de la subcolección de seguidores
            listFollowersCollection(listId).document(userId).delete().await()

            // Decrementar contador de seguidores
            readingListsCollection().document(listId)
                .update("followerCount", com.google.firebase.firestore.FieldValue.increment(-1))
                .await()

            Log.d("ReadingListService", "Usuario $userId dejó de seguir la lista $listId")
            true
        } catch (e: Exception) {
            Log.e("ReadingListService", "Error dejando de seguir lista: ${e.message}", e)
            false
        }
    }

    /**
     * Verifica si un usuario sigue una lista
     */
    suspend fun isFollowingList(listId: String, userId: String): Boolean {
        return try {
            val doc = listFollowersCollection(listId).document(userId).get().await()
            doc.exists()
        } catch (e: Exception) {
            Log.e("ReadingListService", "Error verificando si sigue lista: ${e.message}", e)
            false
        }
    }

    /**
     * Obtiene las listas que sigue un usuario
     */
    suspend fun getFollowedLists(userId: String): List<ReadingList> {
        return try {
            // Buscar en todas las listas públicas donde el usuario está en la subcolección de seguidores
            val allPublicLists = readingListsCollection()
                .whereEqualTo("isPublic", true)
                .get()
                .await()

            val followedLists = mutableListOf<ReadingList>()
            for (doc in allPublicLists.documents) {
                val followerDoc = listFollowersCollection(doc.id).document(userId).get().await()
                if (followerDoc.exists()) {
                    val data = doc.data ?: continue
                    followedLists.add(
                        ReadingList(
                            id = doc.id,
                            userId = data["userId"] as? String ?: "",
                            name = data["name"] as? String ?: "",
                            description = data["description"] as? String ?: "",
                            isPublic = data["isPublic"] as? Boolean ?: false,
                            createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                            updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                            bookIds = (data["bookIds"] as? List<*>?)?.mapNotNull { (it as? Number)?.toInt() } ?: emptyList(),
                            followerCount = (data["followerCount"] as? Number)?.toInt() ?: 0
                        )
                    )
                }
            }
            followedLists
        } catch (e: Exception) {
            Log.e("ReadingListService", "Error obteniendo listas seguidas: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Elimina una lista
     */
    suspend fun deleteReadingList(listId: String): Boolean {
        return try {
            // Eliminar subcolección de seguidores primero
            val followers = listFollowersCollection(listId).get().await()
            followers.documents.forEach { doc ->
                doc.reference.delete().await()
            }

            // Eliminar la lista
            readingListsCollection().document(listId).delete().await()
            Log.d("ReadingListService", "Lista eliminada: $listId")
            true
        } catch (e: Exception) {
            Log.e("ReadingListService", "Error eliminando lista: ${e.message}", e)
            false
        }
    }
}

