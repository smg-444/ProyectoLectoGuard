package es.etg.lectoguard.domain.model

/**
 * Modelo de datos para items del feed social
 */
data class FeedItem(
    val id: String = "",
    val userId: String = "", // Usuario que realizó la acción
    val userName: String = "",
    val userAvatarUrl: String? = null,
    val type: FeedItemType = FeedItemType.BOOK_SAVED,
    val timestamp: Long = System.currentTimeMillis(),
    // Datos específicos según el tipo
    val bookId: Int? = null,
    val bookTitle: String? = null,
    val bookCoverUrl: String? = null,
    val rating: Int? = null, // Para valoraciones (1-5)
    val reviewText: String? = null, // Para reseñas
    val reviewId: String? = null, // Para reseñas
    val targetUserId: String? = null, // Para follows
    val targetUserName: String? = null // Para follows
)

/**
 * Tipos de actividades en el feed
 */
enum class FeedItemType {
    BOOK_SAVED,      // Usuario guardó un libro
    RATING,          // Usuario valoró un libro
    REVIEW,          // Usuario escribió una reseña
    FOLLOW           // Usuario siguió a otro usuario
}

