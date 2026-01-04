package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.ChatRepository
import es.etg.lectoguard.domain.model.Message

class SendMessageUseCase(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(conversationId: String, message: Message): String? =
        chatRepository.sendMessage(conversationId, message)
}

