package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.local.BookEntity
import es.etg.lectoguard.data.repository.BookRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetBooksUseCaseTest {

    private lateinit var repository: BookRepository
    private lateinit var getBooksUseCase: GetBooksUseCase

    @Before
    fun setup() {
        repository = mockk()
        getBooksUseCase = GetBooksUseCase(repository)
    }

    @Test
    fun `getBooks with online returns books from repository`() = runBlocking {
        // Given
        val expectedBooks = listOf(
            BookEntity(1, "Book 1", "url1"),
            BookEntity(2, "Book 2", "url2")
        )

        coEvery { repository.getAllBooks(true) } returns expectedBooks

        // When
        val result = getBooksUseCase(true)

        // Then
        assertEquals(expectedBooks, result)
        coVerify(exactly = 1) { repository.getAllBooks(true) }
    }

    @Test
    fun `getBooks with offline returns cached books`() = runBlocking {
        // Given
        val expectedBooks = listOf(
            BookEntity(1, "Book 1", "url1")
        )

        coEvery { repository.getAllBooks(false) } returns expectedBooks

        // When
        val result = getBooksUseCase(false)

        // Then
        assertEquals(expectedBooks, result)
        coVerify(exactly = 1) { repository.getAllBooks(false) }
    }

    @Test
    fun `getBooks returns empty list when no books available`() = runBlocking {
        // Given
        val expectedBooks = emptyList<BookEntity>()

        coEvery { repository.getAllBooks(true) } returns expectedBooks

        // When
        val result = getBooksUseCase(true)

        // Then
        assertEquals(expectedBooks, result)
        assertEquals(0, result.size)
    }
}

