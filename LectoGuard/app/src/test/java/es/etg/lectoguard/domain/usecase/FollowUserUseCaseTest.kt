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

class FollowUserUseCaseTest {

    private lateinit var repository: UserRepository
    private lateinit var followUserUseCase: FollowUserUseCase

    @Before
    fun setup() {
        repository = mockk()
        followUserUseCase = FollowUserUseCase(repository)
    }

    @Test
    fun `followUser follows user successfully`() = runBlocking {
        // Given
        val selfUid = "user1"
        val targetUid = "user2"
        val targetUserName = "Target User"

        coEvery { repository.followUser(selfUid, targetUid, targetUserName) } returns true

        // When
        val result = followUserUseCase(selfUid, targetUid, targetUserName)

        // Then
        assertTrue(result)
        coVerify(exactly = 1) { repository.followUser(selfUid, targetUid, targetUserName) }
    }

    @Test
    fun `followUser returns false when already following`() = runBlocking {
        // Given
        val selfUid = "user1"
        val targetUid = "user2"
        val targetUserName = "Target User"

        coEvery { repository.followUser(selfUid, targetUid, targetUserName) } returns false

        // When
        val result = followUserUseCase(selfUid, targetUid, targetUserName)

        // Then
        assertFalse(result)
        coVerify(exactly = 1) { repository.followUser(selfUid, targetUid, targetUserName) }
    }
}

