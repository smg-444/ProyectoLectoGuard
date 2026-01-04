package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.ReadingListRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeleteReadingListUseCaseTest {

    private lateinit var repository: ReadingListRepository
    private lateinit var deleteReadingListUseCase: DeleteReadingListUseCase

    @Before
    fun setup() {
        repository = mockk()
        deleteReadingListUseCase = DeleteReadingListUseCase(repository)
    }

    @Test
    fun `deleteReadingList deletes list successfully`() = runBlocking {
        // Given
        val listId = "list123"

        coEvery { repository.deleteReadingList(listId, true) } returns true

        // When
        val result = deleteReadingListUseCase(listId, true)

        // Then
        assertTrue(result)
        coVerify(exactly = 1) { repository.deleteReadingList(listId, true) }
    }

    @Test
    fun `deleteReadingList returns false when list not found`() = runBlocking {
        // Given
        val listId = "nonexistent"

        coEvery { repository.deleteReadingList(listId, true) } returns false

        // When
        val result = deleteReadingListUseCase(listId, true)

        // Then
        assertFalse(result)
        coVerify(exactly = 1) { repository.deleteReadingList(listId, true) }
    }

    @Test
    fun `deleteReadingList with syncToFirestore false`() = runBlocking {
        // Given
        val listId = "list123"

        coEvery { repository.deleteReadingList(listId, false) } returns true

        // When
        val result = deleteReadingListUseCase(listId, false)

        // Then
        assertTrue(result)
        coVerify(exactly = 1) { repository.deleteReadingList(listId, false) }
    }
}

