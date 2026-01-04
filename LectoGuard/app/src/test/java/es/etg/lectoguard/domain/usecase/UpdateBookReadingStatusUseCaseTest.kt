package es.etg.lectoguard.domain.usecase

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

class UpdateBookReadingStatusUseCaseTest {

    private lateinit var repository: BookRepository
    private lateinit var updateBookReadingStatusUseCase: UpdateBookReadingStatusUseCase

    @Before
    fun setup() {
        repository = mockk()
        updateBookReadingStatusUseCase = UpdateBookReadingStatusUseCase(repository)
    }

    @Test
    fun `updateReadingStatus updates status successfully`() = runBlocking {
        // Given
        val userId = 1
        val bookId = 5
        val newStatus = ReadingStatus.READING

        coEvery { repository.updateReadingStatus(userId, bookId, newStatus) } returns true

        // When
        val result = updateBookReadingStatusUseCase(userId, bookId, newStatus)

        // Then
        assertTrue(result)
        coVerify(exactly = 1) { repository.updateReadingStatus(userId, bookId, newStatus) }
    }

    @Test
    fun `updateReadingStatus returns false when book not found`() = runBlocking {
        // Given
        val userId = 1
        val bookId = 999
        val newStatus = ReadingStatus.READ

        coEvery { repository.updateReadingStatus(userId, bookId, newStatus) } returns false

        // When
        val result = updateBookReadingStatusUseCase(userId, bookId, newStatus)

        // Then
        assertFalse(result)
        coVerify(exactly = 1) { repository.updateReadingStatus(userId, bookId, newStatus) }
    }

    @Test
    fun `updateReadingStatus updates to different statuses`() = runBlocking {
        // Given
        val userId = 1
        val bookId = 5

        coEvery { repository.updateReadingStatus(userId, bookId, ReadingStatus.WANT_TO_READ) } returns true
        coEvery { repository.updateReadingStatus(userId, bookId, ReadingStatus.READING) } returns true
        coEvery { repository.updateReadingStatus(userId, bookId, ReadingStatus.READ) } returns true
        coEvery { repository.updateReadingStatus(userId, bookId, ReadingStatus.ABANDONED) } returns true

        // When & Then
        assertTrue(updateBookReadingStatusUseCase(userId, bookId, ReadingStatus.WANT_TO_READ))
        assertTrue(updateBookReadingStatusUseCase(userId, bookId, ReadingStatus.READING))
        assertTrue(updateBookReadingStatusUseCase(userId, bookId, ReadingStatus.READ))
        assertTrue(updateBookReadingStatusUseCase(userId, bookId, ReadingStatus.ABANDONED))
    }
}

