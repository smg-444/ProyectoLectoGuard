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

class DeleteReviewUseCaseTest {

    private lateinit var repository: RatingRepository
    private lateinit var deleteReviewUseCase: DeleteReviewUseCase

    @Before
    fun setup() {
        repository = mockk()
        deleteReviewUseCase = DeleteReviewUseCase(repository)
    }

    @Test
    fun `deleteReview deletes review successfully`() = runBlocking {
        // Given
        val reviewId = "review123"
        val userId = "user123"

        coEvery { repository.deleteReview(reviewId, userId) } returns true

        // When
        val result = deleteReviewUseCase(reviewId, userId)

        // Then
        assertTrue(result)
        coVerify(exactly = 1) { repository.deleteReview(reviewId, userId) }
    }

    @Test
    fun `deleteReview returns false when review not found`() = runBlocking {
        // Given
        val reviewId = "nonexistent"
        val userId = "user123"

        coEvery { repository.deleteReview(reviewId, userId) } returns false

        // When
        val result = deleteReviewUseCase(reviewId, userId)

        // Then
        assertFalse(result)
        coVerify(exactly = 1) { repository.deleteReview(reviewId, userId) }
    }

    @Test
    fun `deleteReview returns false when user is not owner`() = runBlocking {
        // Given
        val reviewId = "review123"
        val userId = "otherUser" // No es el propietario

        coEvery { repository.deleteReview(reviewId, userId) } returns false

        // When
        val result = deleteReviewUseCase(reviewId, userId)

        // Then
        assertFalse(result)
        coVerify(exactly = 1) { repository.deleteReview(reviewId, userId) }
    }
}

