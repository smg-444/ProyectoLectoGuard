package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.local.UserBookEntity
import es.etg.lectoguard.data.repository.BookRepository
import es.etg.lectoguard.domain.model.ReadingStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SaveBookUseCaseTest {

    private lateinit var repository: BookRepository
    private lateinit var saveBookUseCase: SaveBookUseCase

    @Before
    fun setup() {
        repository = mockk()
        saveBookUseCase = SaveBookUseCase(repository)
    }

    @Test
    fun `saveBook with new book returns true`() = runBlocking {
        // Given
        val userBook = UserBookEntity(
            userId = 1,
            bookId = 1,
            savedDate = "1234567890",
            readingStatus = ReadingStatus.WANT_TO_READ,
            tags = emptyList()
        )
        val userId = "user123"
        val bookTitle = "Test Book"
        val bookCover = "cover_url"

        coEvery { repository.saveBook(userBook, userId, bookTitle, bookCover) } returns true

        // When
        val result = saveBookUseCase(userBook, userId, bookTitle, bookCover)

        // Then
        assertTrue(result)
        coVerify(exactly = 1) { repository.saveBook(userBook, userId, bookTitle, bookCover) }
    }

    @Test
    fun `saveBook with duplicate book returns false`() = runBlocking {
        // Given
        val userBook = UserBookEntity(
            userId = 1,
            bookId = 1,
            savedDate = "1234567890",
            readingStatus = ReadingStatus.WANT_TO_READ,
            tags = emptyList()
        )
        val userId = "user123"
        val bookTitle = "Test Book"
        val bookCover = "cover_url"

        coEvery { repository.saveBook(userBook, userId, bookTitle, bookCover) } returns false

        // When
        val result = saveBookUseCase(userBook, userId, bookTitle, bookCover)

        // Then
        assertFalse(result)
        coVerify(exactly = 1) { repository.saveBook(userBook, userId, bookTitle, bookCover) }
    }
}

