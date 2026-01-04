package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.UserRepository
import es.etg.lectoguard.domain.model.FollowCounts
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetFollowCountsUseCaseTest {

    private lateinit var repository: UserRepository
    private lateinit var getFollowCountsUseCase: GetFollowCountsUseCase

    @Before
    fun setup() {
        repository = mockk()
        getFollowCountsUseCase = GetFollowCountsUseCase(repository)
    }

    @Test
    fun `getFollowCounts returns correct counts`() = runBlocking {
        // Given
        val uid = "user123"
        val expectedCounts = FollowCounts(followers = 10, following = 5)

        coEvery { repository.getFollowCounts(uid) } returns expectedCounts

        // When
        val result = getFollowCountsUseCase(uid)

        // Then
        assertEquals(expectedCounts, result)
        assertEquals(10, result.followers)
        assertEquals(5, result.following)
        coVerify(exactly = 1) { repository.getFollowCounts(uid) }
    }

    @Test
    fun `getFollowCounts returns zero counts for new user`() = runBlocking {
        // Given
        val uid = "newUser"
        val expectedCounts = FollowCounts(followers = 0, following = 0)

        coEvery { repository.getFollowCounts(uid) } returns expectedCounts

        // When
        val result = getFollowCountsUseCase(uid)

        // Then
        assertEquals(expectedCounts, result)
        assertEquals(0, result.followers)
        assertEquals(0, result.following)
        coVerify(exactly = 1) { repository.getFollowCounts(uid) }
    }
}

