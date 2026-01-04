package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MarkMessagesAsReadUseCaseTest {

    private lateinit var repository: ChatRepository
    private lateinit var markMessagesAsReadUseCase: MarkMessagesAsReadUseCase

    @Before
    fun setup() {
        repository = mockk()
        markMessagesAsReadUseCase = MarkMessagesAsReadUseCase(repository)
    }

    @Test
    fun `markMessagesAsRead marks messages successfully`() = runBlocking {
        // Given
        val conversationId = "conv123"
        val userId = "user1"

        coEvery { repository.markMessagesAsRead(conversationId, userId) } returns true

        // When
        val result = markMessagesAsReadUseCase(conversationId, userId)

        // Then
        assertTrue(result)
        coVerify(exactly = 1) { repository.markMessagesAsRead(conversationId, userId) }
    }

    @Test
    fun `markMessagesAsRead returns false on error`() = runBlocking {
        // Given
        val conversationId = "nonexistent"
        val userId = "user1"

        coEvery { repository.markMessagesAsRead(conversationId, userId) } returns false

        // When
        val result = markMessagesAsReadUseCase(conversationId, userId)

        // Then
        assertFalse(result)
        coVerify(exactly = 1) { repository.markMessagesAsRead(conversationId, userId) }
    }
}

