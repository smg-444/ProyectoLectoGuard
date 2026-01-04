package es.etg.lectoguard.domain.model

data class Rating(
    val id: String = "",
    val bookId: Int,
    val userId: String,
    val rating: Int, // 1-5
    val createdAt: Long = System.currentTimeMillis()
)

