package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.local.UserEntity
import es.etg.lectoguard.data.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RegisterUseCaseTest {

    private lateinit var repository: UserRepository
    private lateinit var registerUseCase: RegisterUseCase

    @Before
    fun setup() {
        repository = mockk()
        registerUseCase = RegisterUseCase(repository)
    }

    @Test
    fun `register with valid data returns user id`() = runBlocking {
        // Given
        val user = UserEntity(
            id = 0,
            name = "Test User",
            email = "test@example.com",
            phone = "123456789",
            password = "password123",
            signupDate = "1234567890"
        )
        val expectedId = 1L

        coEvery { repository.register(user) } returns expectedId

        // When
        val result = registerUseCase(user)

        // Then
        assertEquals(expectedId, result)
        coVerify(exactly = 1) { repository.register(user) }
    }

    @Test
    fun `register with duplicate email returns zero`() = runBlocking {
        // Given
        val user = UserEntity(
            id = 0,
            name = "Test User",
            email = "existing@example.com",
            phone = "123456789",
            password = "password123",
            signupDate = "1234567890"
        )

        coEvery { repository.register(user) } returns 0L

        // When
        val result = registerUseCase(user)

        // Then
        assertEquals(0L, result)
        coVerify(exactly = 1) { repository.register(user) }
    }
}

