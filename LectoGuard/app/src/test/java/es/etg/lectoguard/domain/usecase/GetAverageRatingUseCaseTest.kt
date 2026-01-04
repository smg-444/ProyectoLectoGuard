package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.RatingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetAverageRatingUseCaseTest {

    private lateinit var repository: RatingRepository
    private lateinit var getAverageRatingUseCase: GetAverageRatingUseCase

    @Before
    fun setup() {
        repository = mockk()
        getAverageRatingUseCase = GetAverageRatingUseCase(repository)
    }

    @Test
    fun `getAverageRating returns correct average`() = runBlocking {
        // Given
        val bookId = 1
        val expectedAverage = 4.5

        coEvery { repository.getAverageRating(bookId) } returns expectedAverage

        // When
        val result = getAverageRatingUseCase(bookId)

        // Then
        assertEquals(expectedAverage, result, 0.01)
        coVerify(exactly = 1) { repository.getAverageRating(bookId) }
    }

    @Test
    fun `getAverageRating returns zero for book with no ratings`() = runBlocking {
        // Given
        val bookId = 999
        val expectedAverage = 0.0

        coEvery { repository.getAverageRating(bookId) } returns expectedAverage

        // When
        val result = getAverageRatingUseCase(bookId)

        // Then
        assertEquals(expectedAverage, result, 0.01)
        coVerify(exactly = 1) { repository.getAverageRating(bookId) }
    }

    @Test
    fun `getAverageRating returns correct average for multiple ratings`() = runBlocking {
        // Given
        val bookId = 1
        // Promedio de 5, 4, 3 = 4.0
        val expectedAverage = 4.0

        coEvery { repository.getAverageRating(bookId) } returns expectedAverage

        // When
        val result = getAverageRatingUseCase(bookId)

        // Then
        assertEquals(expectedAverage, result, 0.01)
        coVerify(exactly = 1) { repository.getAverageRating(bookId) }
    }
}

