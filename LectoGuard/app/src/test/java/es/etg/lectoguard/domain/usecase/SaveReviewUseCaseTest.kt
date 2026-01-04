package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.RatingRepository
import es.etg.lectoguard.domain.model.Review
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class SaveReviewUseCaseTest {

    private lateinit var repository: RatingRepository
    private lateinit var saveReviewUseCase: SaveReviewUseCase

    @Before
    fun setup() {
        repository = mockk()
        saveReviewUseCase = SaveReviewUseCase(repository)
    }

    @Test
    fun `saveReview saves review successfully`() = runBlocking {
        // Given
        val review = Review(
            id = "",
            bookId = 1,
            userId = "user123",
            userName = "Test User",
            rating = 5,
            text = "Excellent book!",
            likes = emptyList(),
            createdAt = System.currentTimeMillis()
        )
        val expectedReviewId = "review123"

        coEvery { repository.saveReview(review) } returns expectedReviewId

        // When
        val result = saveReviewUseCase(review)

        // Then
        assertEquals(expectedReviewId, result)
        coVerify(exactly = 1) { repository.saveReview(review) }
    }

    @Test
    fun `saveReview returns null on error`() = runBlocking {
        // Given
        val review = Review(
            id = "",
            bookId = 1,
            userId = "user123",
            userName = "Test User",
            rating = 5,
            text = "Excellent book!",
            likes = emptyList(),
            createdAt = System.currentTimeMillis()
        )

        coEvery { repository.saveReview(review) } returns null

        // When
        val result = saveReviewUseCase(review)

        // Then
        assertNull(result)
        coVerify(exactly = 1) { repository.saveReview(review) }
    }
}

