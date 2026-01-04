package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.RatingRepository
import es.etg.lectoguard.domain.model.Rating
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SaveRatingUseCaseTest {

    private lateinit var repository: RatingRepository
    private lateinit var saveRatingUseCase: SaveRatingUseCase

    @Before
    fun setup() {
        repository = mockk()
        saveRatingUseCase = SaveRatingUseCase(repository)
    }

    @Test
    fun `saveRating saves rating successfully`() = runBlocking {
        // Given
        val rating = Rating(
            id = "",
            bookId = 1,
            userId = "user123",
            rating = 5,
            createdAt = System.currentTimeMillis()
        )

        coEvery { repository.saveRating(rating) } returns true

        // When
        val result = saveRatingUseCase(rating)

        // Then
        assertTrue(result)
        coVerify(exactly = 1) { repository.saveRating(rating) }
    }

    @Test
    fun `saveRating returns false on error`() = runBlocking {
        // Given
        val rating = Rating(
            id = "",
            bookId = 1,
            userId = "user123",
            rating = 3,
            createdAt = System.currentTimeMillis()
        )

        coEvery { repository.saveRating(rating) } returns false

        // When
        val result = saveRatingUseCase(rating)

        // Then
        assertFalse(result)
        coVerify(exactly = 1) { repository.saveRating(rating) }
    }

    @Test
    fun `saveRating with different ratings`() = runBlocking {
        // Given
        val rating1 = Rating(bookId = 1, userId = "user1", rating = 1)
        val rating2 = Rating(bookId = 1, userId = "user2", rating = 5)

        coEvery { repository.saveRating(rating1) } returns true
        coEvery { repository.saveRating(rating2) } returns true

        // When & Then
        assertTrue(saveRatingUseCase(rating1))
        assertTrue(saveRatingUseCase(rating2))
        coVerify(exactly = 1) { repository.saveRating(rating1) }
        coVerify(exactly = 1) { repository.saveRating(rating2) }
    }
}

