package es.etg.lectoguard.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.util.Log

class UserBookService(
    private val firestore: FirebaseFirestore
) {
    private fun userBooksCollection() = firestore.collection("user_books")
    
    /**
     * Guarda o actualiza un libro del usuario en Firestore
     */
    suspend fun saveUserBook(
        userId: String,
        bookId: Int,
        bookTitle: String,
        bookCoverUrl: String?
    ) {
        try {
            val data = hashMapOf<String, Any>(
                "userId" to userId,
                "bookId" to bookId,
                "bookTitle" to bookTitle,
                "savedAt" to System.currentTimeMillis()
            )
            
            bookCoverUrl?.let { data["bookCoverUrl"] = it }
            
            userBooksCollection()
                .document("${userId}_${bookId}")
                .set(data)
                .await()
            
            Log.d("UserBookService", "Libro guardado en Firestore: userId=$userId, bookId=$bookId")
        } catch (e: Exception) {
            Log.e("UserBookService", "Error guardando libro en Firestore: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * Obtiene los IDs de libros guardados por un usuario
     */
    suspend fun getUserBookIds(userId: String): List<Int> {
        return try {
            val snapshot = userBooksCollection()
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                (doc.data?.get("bookId") as? Number)?.toInt()
            }
        } catch (e: Exception) {
            Log.e("UserBookService", "Error obteniendo libros del usuario: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Obtiene los libros guardados de múltiples usuarios
     */
    suspend fun getBooksFromUsers(userIds: List<String>): Map<String, List<Int>> {
        return try {
            val result = mutableMapOf<String, List<Int>>()
            
            // Firestore tiene límite de 10 elementos en whereIn, así que procesamos en lotes
            userIds.chunked(10).forEach { batch ->
                val snapshot = userBooksCollection()
                    .whereIn("userId", batch)
                    .get()
                    .await()
                
                snapshot.documents.forEach { doc ->
                    val userId = doc.data?.get("userId") as? String ?: return@forEach
                    val bookId = (doc.data?.get("bookId") as? Number)?.toInt() ?: return@forEach
                    
                    if (!result.containsKey(userId)) {
                        result[userId] = mutableListOf()
                    }
                    (result[userId] as? MutableList)?.add(bookId)
                }
            }
            
            result
        } catch (e: Exception) {
            Log.e("UserBookService", "Error obteniendo libros de usuarios: ${e.message}", e)
            emptyMap()
        }
    }
    
    /**
     * Elimina un libro del usuario en Firestore
     */
    suspend fun removeUserBook(userId: String, bookId: Int) {
        try {
            userBooksCollection()
                .document("${userId}_${bookId}")
                .delete()
                .await()
            
            Log.d("UserBookService", "Libro eliminado de Firestore: userId=$userId, bookId=$bookId")
        } catch (e: Exception) {
            Log.e("UserBookService", "Error eliminando libro de Firestore: ${e.message}", e)
        }
    }
}

