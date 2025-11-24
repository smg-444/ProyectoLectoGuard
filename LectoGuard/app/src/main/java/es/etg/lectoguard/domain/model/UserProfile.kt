package es.etg.lectoguard.domain.model

data class UserProfile(
    val uid: String,
    val displayName: String,
    val email: String,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val booksRead: Int = 0,
    val followers: Int = 0,
    val following: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)


