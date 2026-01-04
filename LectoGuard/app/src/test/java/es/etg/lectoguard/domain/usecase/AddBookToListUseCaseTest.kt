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

class AddBookToListUseCaseTest {

    private lateinit var repository: ReadingListRepository
    private lateinit var addBookToListUseCase: AddBookToListUseCase

    @Before
    fun setup() {
        repository = mockk()
        addBookToListUseCase = AddBookToListUseCase(repository)
    }

    @Test
    fun `addBookToList adds book successfully`() = runBlocking {
        // Given
        val listId = "list123"
        val bookId = 5

        coEvery { repository.addBookToList(listId, bookId) } returns true

        // When
        val result = addBookToListUseCase(listId, bookId)

        // Then
        assertTrue(result)
        coVerify(exactly = 1) { repository.addBookToList(listId, bookId) }
    }

    @Test
    fun `addBookToList returns false when list not found`() = runBlocking {
        // Given
        val listId = "nonexistent"
        val bookId = 5

        coEvery { repository.addBookToList(listId, bookId) } returns false

        // When
        val result = addBookToListUseCase(listId, bookId)

        // Then
        assertFalse(result)
        coVerify(exactly = 1) { repository.addBookToList(listId, bookId) }
    }
}

