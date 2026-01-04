package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.ChatRepository

class MarkMessagesAsReadUseCase(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(conversationId: String, userId: String): Boolean =
        chatRepository.markMessagesAsRead(conversationId, userId)
}

