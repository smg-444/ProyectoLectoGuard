package es.etg.lectoguard.domain.model

/**
 * Modelo de dominio para una lista de lectura personalizada
 */
data class ReadingList(
    val id: String = "",
    val userId: String, // Firebase UID del usuario propietario
    val name: String,
    val description: String = "",
    val isPublic: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val bookIds: List<Int> = emptyList(), // IDs de libros en orden
    val followerCount: Int = 0 // Número de usuarios que siguen esta lista
)

