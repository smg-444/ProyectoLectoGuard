package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.RatingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateReviewUseCaseTest {

    private lateinit var repository: RatingRepository
    private lateinit var updateReviewUseCase: UpdateReviewUseCase

    @Before
    fun setup() {
        repository = mockk()
        updateReviewUseCase = UpdateReviewUseCase(repository)
    }

    @Test
    fun `updateReview updates review successfully`() = runBlocking {
        // Given
        val reviewId = "review123"
        val userId = "user123"
        val newText = "Updated review text"
        val newRating = 4

        coEvery { repository.updateReview(reviewId, userId, newText, newRating) } returns true

        // When
        val result = updateReviewUseCase(reviewId, userId, newText, newRating)

        // Then
        assertTrue(result)
        coVerify(exactly = 1) { repository.updateReview(reviewId, userId, newText, newRating) }
    }

    @Test
    fun `updateReview returns false when review not found`() = runBlocking {
        // Given
        val reviewId = "nonexistent"
        val userId = "user123"
        val newText = "Updated text"
        val newRating = 5

        coEvery { repository.updateReview(reviewId, userId, newText, newRating) } returns false

        // When
        val result = updateReviewUseCase(reviewId, userId, newText, newRating)

        // Then
        assertFalse(result)
        coVerify(exactly = 1) { repository.updateReview(reviewId, userId, newText, newRating) }
    }

    @Test
    fun `updateReview returns false when user is not owner`() = runBlocking {
        // Given
        val reviewId = "review123"
        val userId = "otherUser" // No es el propietario
        val newText = "Updated text"
        val newRating = 5

        coEvery { repository.updateReview(reviewId, userId, newText, newRating) } returns false

        // When
        val result = updateReviewUseCase(reviewId, userId, newText, newRating)

        // Then
        assertFalse(result)
        coVerify(exactly = 1) { repository.updateReview(reviewId, userId, newText, newRating) }
    }
}

