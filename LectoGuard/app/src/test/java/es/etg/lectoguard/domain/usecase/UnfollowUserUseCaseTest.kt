package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class UnfollowUserUseCaseTest {

    private lateinit var repository: UserRepository
    private lateinit var unfollowUserUseCase: UnfollowUserUseCase

    @Before
    fun setup() {
        repository = mockk()
        unfollowUserUseCase = UnfollowUserUseCase(repository)
    }

    @Test
    fun `unfollowUser unfollows user successfully`() = runBlocking {
        // Given
        val selfUid = "user1"
        val targetUid = "user2"

        coEvery { repository.unfollowUser(selfUid, targetUid) } returns Unit

        // When
        unfollowUserUseCase(selfUid, targetUid)

        // Then
        coVerify(exactly = 1) { repository.unfollowUser(selfUid, targetUid) }
    }

    @Test
    fun `unfollowUser when not following`() = runBlocking {
        // Given
        val selfUid = "user1"
        val targetUid = "user2"

        coEvery { repository.unfollowUser(selfUid, targetUid) } returns Unit

        // When
        unfollowUserUseCase(selfUid, targetUid)

        // Then
        coVerify(exactly = 1) { repository.unfollowUser(selfUid, targetUid) }
    }
}

