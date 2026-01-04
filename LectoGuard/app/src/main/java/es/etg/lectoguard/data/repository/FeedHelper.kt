package es.etg.lectoguard.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import es.etg.lectoguard.data.local.BookDao
import es.etg.lectoguard.data.remote.FeedService
import es.etg.lectoguard.data.remote.UserProfileService
import es.etg.lectoguard.domain.model.FeedItem
import es.etg.lectoguard.domain.model.FeedItemType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Helper para crear feed items automáticamente cuando se realizan acciones
 */
object FeedHelper {
    private const val TAG = "FeedHelper"
    
    /**
     * Crea un feed item cuando un usuario guarda un libro
     */
    fun createBookSavedFeedItem(
        userId: String,
        bookId: Int,
        bookTitle: String?,
        bookCoverUrl: String?,
        firestore: FirebaseFirestore
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val profileService = UserProfileService(firestore)
                val profile = profileService.getProfile(userId)
                
                if (profile != null) {
                    val feedItem = FeedItem(
                        type = FeedItemType.BOOK_SAVED,
                        bookId = bookId,
                        bookTitle = bookTitle,
                        bookCoverUrl = bookCoverUrl,
                        timestamp = System.currentTimeMillis()
                    )
                    
                    val feedService = FeedService(firestore)
                    feedService.createFeedItemForFollowers(
                        userId,
                        profile.displayName,
                        profile.avatarUrl,
                        feedItem
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creando feed item para libro guardado: ${e.message}", e)
            }
        }
    }
    
    /**
     * Crea un feed item cuando un usuario valora un libro
     */
    fun createRatingFeedItem(
        userId: String,
        bookId: Int,
        rating: Int,
        bookTitle: String?,
        bookCoverUrl: String?,
        firestore: FirebaseFirestore
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Iniciando creación de feed item para valoración: userId=$userId, bookId=$bookId")
                val profileService = UserProfileService(firestore)
                val profile = profileService.getProfile(userId)
                
                if (profile != null) {
                    val feedItem = FeedItem(
                        type = FeedItemType.RATING,
                        bookId = bookId,
                        bookTitle = bookTitle,
                        bookCoverUrl = bookCoverUrl,
                        rating = rating,
                        timestamp = System.currentTimeMillis()
                    )
                    
                    val feedService = FeedService(firestore)
                    val result = feedService.createFeedItemForFollowers(
                        userId,
                        profile.displayName,
                        profile.avatarUrl,
                        feedItem
                    )
                    Log.d(TAG, "Resultado creación feed item para valoración: $result")
                } else {
                    Log.w(TAG, "Perfil no encontrado para userId: $userId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creando feed item para valoración: ${e.message}", e)
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Crea un feed item cuando un usuario escribe una reseña
     */
    fun createReviewFeedItem(
        userId: String,
        bookId: Int,
        reviewId: String,
        reviewText: String,
        rating: Int,
        bookTitle: String?,
        bookCoverUrl: String?,
        firestore: FirebaseFirestore
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Iniciando creación de feed item para reseña: userId=$userId, bookId=$bookId, reviewId=$reviewId")
                val profileService = UserProfileService(firestore)
                val profile = profileService.getProfile(userId)
                
                if (profile != null) {
                    val feedItem = FeedItem(
                        type = FeedItemType.REVIEW,
                        bookId = bookId,
                        bookTitle = bookTitle,
                        bookCoverUrl = bookCoverUrl,
                        rating = rating,
                        reviewText = reviewText,
                        reviewId = reviewId,
                        timestamp = System.currentTimeMillis()
                    )
                    
                    val feedService = FeedService(firestore)
                    val result = feedService.createFeedItemForFollowers(
                        userId,
                        profile.displayName,
                        profile.avatarUrl,
                        feedItem
                    )
                    Log.d(TAG, "Resultado creación feed item para reseña: $result")
                } else {
                    Log.w(TAG, "Perfil no encontrado para userId: $userId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creando feed item para reseña: ${e.message}", e)
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Crea un feed item cuando un usuario sigue a otro usuario
     */
    fun createFollowFeedItem(
        userId: String,
        targetUserId: String,
        targetUserName: String,
        firestore: FirebaseFirestore
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val profileService = UserProfileService(firestore)
                val profile = profileService.getProfile(userId)
                
                if (profile != null) {
                    val feedItem = FeedItem(
                        type = FeedItemType.FOLLOW,
                        userId = userId,
                        userName = profile.displayName,
                        userAvatarUrl = profile.avatarUrl,
                        targetUserId = targetUserId,
                        targetUserName = targetUserName,
                        timestamp = System.currentTimeMillis()
                    )
                    
                    Log.d(TAG, "Creando feed item FOLLOW: userId=$userId, targetUserId=$targetUserId, targetUserName=$targetUserName, type=${feedItem.type.name}")
                    
                    val feedService = FeedService(firestore)
                    feedService.createFeedItemForFollowers(
                        userId,
                        profile.displayName,
                        profile.avatarUrl,
                        feedItem
                    )
                    
                    Log.d(TAG, "Feed item FOLLOW creado exitosamente")
                } else {
                    Log.w(TAG, "Perfil no encontrado para userId: $userId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creando feed item para follow: ${e.message}", e)
                e.printStackTrace()
            }
        }
    }
}

