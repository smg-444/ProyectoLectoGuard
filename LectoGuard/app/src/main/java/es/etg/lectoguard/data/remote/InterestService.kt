package es.etg.lectoguard.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import es.etg.lectoguard.domain.model.BookGenre
import es.etg.lectoguard.domain.model.UserInterests
import kotlinx.coroutines.tasks.await
import android.util.Log

class InterestService(
    private val firestore: FirebaseFirestore
) {
    private fun interestsCollection() = firestore.collection("user_interests")
    
    /**
     * Guarda o actualiza los intereses de un usuario
     */
    suspend fun saveUserInterests(interests: UserInterests) {
        try {
            val data = hashMapOf<String, Any>(
                "userId" to interests.userId,
                "lastUpdated" to interests.lastUpdated
            )
            
            // Convertir géneros a formato Firestore
            val genresMap = mutableMapOf<String, Int>()
            interests.genres.forEach { (genre, count) ->
                genresMap[genre.name] = count
            }
            data["genres"] = genresMap
            
            interestsCollection().document(interests.userId).set(data).await()
            Log.d("InterestService", "Intereses guardados para usuario: ${interests.userId}, géneros: ${interests.genres.map { "${it.key.name}=${it.value}" }.joinToString()}")
        } catch (e: Exception) {
            Log.e("InterestService", "Error guardando intereses: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * Obtiene los intereses de un usuario
     */
    suspend fun getUserInterests(userId: String): UserInterests? {
        return try {
            val snap = interestsCollection().document(userId).get().await()
            if (snap.exists()) {
                val data = snap.data ?: return null
                val genresMap = mutableMapOf<BookGenre, Int>()
                
                @Suppress("UNCHECKED_CAST")
                val genresData = data["genres"] as? Map<String, Int> ?: emptyMap()
                genresData.forEach { (genreName, count) ->
                    val genre = BookGenre.fromString(genreName)
                    genresMap[genre] = count
                }
                
                UserInterests(
                    userId = userId,
                    genres = genresMap,
                    lastUpdated = (data["lastUpdated"] as? Number)?.toLong() ?: System.currentTimeMillis()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("InterestService", "Error obteniendo intereses: ${e.message}", e)
            null
        }
    }
    
    /**
     * Obtiene todos los usuarios con intereses (para calcular recomendaciones)
     */
    suspend fun getAllUsersWithInterests(excludeUserId: String? = null): List<UserInterests> {
        return try {
            Log.d("InterestService", "Obteniendo usuarios con intereses (excluyendo: $excludeUserId)")
            val query = if (excludeUserId != null) {
                interestsCollection().whereNotEqualTo("userId", excludeUserId).get().await()
            } else {
                interestsCollection().get().await()
            }
            
            Log.d("InterestService", "Documentos encontrados en user_interests: ${query.documents.size}")
            
            query.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                val userId = data["userId"] as? String ?: return@mapNotNull null
                
                val genresMap = mutableMapOf<BookGenre, Int>()
                @Suppress("UNCHECKED_CAST")
                val genresData = data["genres"] as? Map<String, Int> ?: emptyMap()
                genresData.forEach { (genreName, count) ->
                    val genre = BookGenre.fromString(genreName)
                    genresMap[genre] = count
                }
                
                UserInterests(
                    userId = userId,
                    genres = genresMap,
                    lastUpdated = (data["lastUpdated"] as? Number)?.toLong() ?: System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            Log.e("InterestService", "Error obteniendo usuarios con intereses: ${e.message}", e)
            emptyList()
        }
    }
}

