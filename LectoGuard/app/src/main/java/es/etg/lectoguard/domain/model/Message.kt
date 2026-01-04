package es.etg.lectoguard.domain.model

data class Message(
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderAvatarUrl: String? = null,
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val readBy: List<String> = emptyList() // Lista de UIDs que han leído el mensaje
)

