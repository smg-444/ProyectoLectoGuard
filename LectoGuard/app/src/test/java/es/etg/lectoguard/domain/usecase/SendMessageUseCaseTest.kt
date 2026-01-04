package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.ChatRepository
import es.etg.lectoguard.domain.model.Message
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class SendMessageUseCaseTest {

    private lateinit var repository: ChatRepository
    private lateinit var sendMessageUseCase: SendMessageUseCase

    @Before
    fun setup() {
        repository = mockk()
        sendMessageUseCase = SendMessageUseCase(repository)
    }

    @Test
    fun `sendMessage sends message successfully`() = runBlocking {
        // Given
        val conversationId = "conv123"
        val message = Message(
            id = "",
            conversationId = conversationId,
            senderId = "user1",
            senderName = "Test User",
            content = "Hello, how are you?",
            timestamp = System.currentTimeMillis()
        )
        val expectedMessageId = "msg123"

        coEvery { repository.sendMessage(conversationId, message) } returns expectedMessageId

        // When
        val result = sendMessageUseCase(conversationId, message)

        // Then
        assertEquals(expectedMessageId, result)
        coVerify(exactly = 1) { repository.sendMessage(conversationId, message) }
    }

    @Test
    fun `sendMessage returns null on error`() = runBlocking {
        // Given
        val conversationId = "conv123"
        val message = Message(
            id = "",
            conversationId = conversationId,
            senderId = "user1",
            senderName = "Test User",
            content = "Test message",
            timestamp = System.currentTimeMillis()
        )

        coEvery { repository.sendMessage(conversationId, message) } returns null

        // When
        val result = sendMessageUseCase(conversationId, message)

        // Then
        assertNull(result)
        coVerify(exactly = 1) { repository.sendMessage(conversationId, message) }
    }
}

