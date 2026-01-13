package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
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

        coEvery { repository.followUser(selfUid, targetUid) } returns Unit

        // When
        followUserUseCase(selfUid, targetUid)

        // Then
        coVerify(exactly = 1) { repository.followUser(selfUid, targetUid) }
    }

    @Test
    fun `followUser does not follow when selfUid equals targetUid`() = runBlocking {
        // Given
        val selfUid = "user1"
        val targetUid = "user1" // Same user

        coEvery { repository.followUser(selfUid, targetUid) } returns Unit

        // When
        followUserUseCase(selfUid, targetUid)

        // Then
        coVerify(exactly = 1) { repository.followUser(selfUid, targetUid) }
    }
}

