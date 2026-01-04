package es.etg.lectoguard.domain.model

data class Conversation(
    val id: String = "",
    val participants: List<String> = emptyList(), // Lista de UIDs de los participantes
    val lastMessage: String? = null,
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val lastMessageSenderId: String? = null,
    val unreadCount: Map<String, Int> = emptyMap(), // Map<userId, unreadCount>
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

