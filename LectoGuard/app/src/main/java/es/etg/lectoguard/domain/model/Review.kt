package es.etg.lectoguard.domain.model

data class Review(
    val id: String = "",
    val bookId: Int,
    val userId: String,
    val userName: String = "",
    val userAvatarUrl: String? = null,
    val rating: Int, // 1-5
    val text: String,
    val likes: Int = 0,
    val likedBy: List<String> = emptyList(), // Lista de UIDs que dieron like
    val createdAt: Long = System.currentTimeMillis()
)

