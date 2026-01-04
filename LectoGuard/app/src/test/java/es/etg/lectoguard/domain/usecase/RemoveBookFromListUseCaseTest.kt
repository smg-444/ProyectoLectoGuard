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

class RemoveBookFromListUseCaseTest {

    private lateinit var repository: ReadingListRepository
    private lateinit var removeBookFromListUseCase: RemoveBookFromListUseCase

    @Before
    fun setup() {
        repository = mockk()
        removeBookFromListUseCase = RemoveBookFromListUseCase(repository)
    }

    @Test
    fun `removeBookFromList removes book successfully`() = runBlocking {
        // Given
        val listId = "list123"
        val bookId = 5

        coEvery { repository.removeBookFromList(listId, bookId, true) } returns true

        // When
        val result = removeBookFromListUseCase(listId, bookId, true)

        // Then
        assertTrue(result)
        coVerify(exactly = 1) { repository.removeBookFromList(listId, bookId, true) }
    }

    @Test
    fun `removeBookFromList returns false when book not in list`() = runBlocking {
        // Given
        val listId = "list123"
        val bookId = 999

        coEvery { repository.removeBookFromList(listId, bookId, true) } returns false

        // When
        val result = removeBookFromListUseCase(listId, bookId, true)

        // Then
        assertFalse(result)
        coVerify(exactly = 1) { repository.removeBookFromList(listId, bookId, true) }
    }

    @Test
    fun `removeBookFromList with syncToFirestore false`() = runBlocking {
        // Given
        val listId = "list123"
        val bookId = 5

        coEvery { repository.removeBookFromList(listId, bookId, false) } returns true

        // When
        val result = removeBookFromListUseCase(listId, bookId, false)

        // Then
        assertTrue(result)
        coVerify(exactly = 1) { repository.removeBookFromList(listId, bookId, false) }
    }
}

