package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetOrCreateConversationUseCaseTest {

    private lateinit var repository: ChatRepository
    private lateinit var getOrCreateConversationUseCase: GetOrCreateConversationUseCase

    @Before
    fun setup() {
        repository = mockk()
        getOrCreateConversationUseCase = GetOrCreateConversationUseCase(repository)
    }

    @Test
    fun `getOrCreateConversation returns existing conversation`() = runBlocking {
        // Given
        val userId1 = "user1"
        val userId2 = "user2"
        val expectedConversationId = "conv123"

        coEvery { repository.getOrCreateConversation(userId1, userId2) } returns expectedConversationId

        // When
        val result = getOrCreateConversationUseCase(userId1, userId2)

        // Then
        assertEquals(expectedConversationId, result)
        coVerify(exactly = 1) { repository.getOrCreateConversation(userId1, userId2) }
    }

    @Test
    fun `getOrCreateConversation creates new conversation`() = runBlocking {
        // Given
        val userId1 = "user1"
        val userId2 = "user2"
        val newConversationId = "conv456"

        coEvery { repository.getOrCreateConversation(userId1, userId2) } returns newConversationId

        // When
        val result = getOrCreateConversationUseCase(userId1, userId2)

        // Then
        assertEquals(newConversationId, result)
        coVerify(exactly = 1) { repository.getOrCreateConversation(userId1, userId2) }
    }

    @Test
    fun `getOrCreateConversation returns null on error`() = runBlocking {
        // Given
        val userId1 = "user1"
        val userId2 = "user2"

        coEvery { repository.getOrCreateConversation(userId1, userId2) } returns null

        // When
        val result = getOrCreateConversationUseCase(userId1, userId2)

        // Then
        assertNull(result)
        coVerify(exactly = 1) { repository.getOrCreateConversation(userId1, userId2) }
    }
}

