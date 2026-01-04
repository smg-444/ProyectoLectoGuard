package es.etg.lectoguard.domain.model

/**
 * Modelo para una recomendación de libro
 */
data class BookRecommendation(
    val bookId: Int,
    val bookTitle: String,
    val bookCoverUrl: String?,
    val reason: String, // Razón de la recomendación
    val similarityScore: Double = 0.0, // Score de similitud con usuarios que tienen este libro
    val recommendedBy: List<String> = emptyList(), // UIDs de usuarios con intereses similares que tienen este libro
    val genre: BookGenre? = null // Género del libro (si está disponible)
)

