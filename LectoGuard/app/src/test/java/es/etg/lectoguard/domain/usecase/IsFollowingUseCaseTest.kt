package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class IsFollowingUseCaseTest {

    private lateinit var repository: UserRepository
    private lateinit var isFollowingUseCase: IsFollowingUseCase

    @Before
    fun setup() {
        repository = mockk()
        isFollowingUseCase = IsFollowingUseCase(repository)
    }

    @Test
    fun `isFollowing returns true when following`() = runBlocking {
        // Given
        val selfUid = "user1"
        val targetUid = "user2"

        coEvery { repository.isFollowing(selfUid, targetUid) } returns true

        // When
        val result = isFollowingUseCase(selfUid, targetUid)

        // Then
        assertTrue(result)
        coVerify(exactly = 1) { repository.isFollowing(selfUid, targetUid) }
    }

    @Test
    fun `isFollowing returns false when not following`() = runBlocking {
        // Given
        val selfUid = "user1"
        val targetUid = "user2"

        coEvery { repository.isFollowing(selfUid, targetUid) } returns false

        // When
        val result = isFollowingUseCase(selfUid, targetUid)

        // Then
        assertFalse(result)
        coVerify(exactly = 1) { repository.isFollowing(selfUid, targetUid) }
    }
}

