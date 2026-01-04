package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.local.UserEntity
import es.etg.lectoguard.data.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class LoginUseCaseTest {

    private lateinit var repository: UserRepository
    private lateinit var loginUseCase: LoginUseCase

    @Before
    fun setup() {
        repository = mockk()
        loginUseCase = LoginUseCase(repository)
    }

    @Test
    fun `login with valid credentials returns user`() = runBlocking {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val expectedUser = UserEntity(
            id = 1,
            name = "Test User",
            email = email,
            phone = "123456789",
            password = "",
            signupDate = "1234567890"
        )

        coEvery { repository.login(email, password) } returns expectedUser

        // When
        val result = loginUseCase(email, password)

        // Then
        assertEquals(expectedUser, result)
        coVerify(exactly = 1) { repository.login(email, password) }
    }

    @Test
    fun `login with invalid credentials returns null`() = runBlocking {
        // Given
        val email = "test@example.com"
        val password = "wrongpassword"

        coEvery { repository.login(email, password) } returns null

        // When
        val result = loginUseCase(email, password)

        // Then
        assertNull(result)
        coVerify(exactly = 1) { repository.login(email, password) }
    }

    @Test
    fun `login with non-existent email returns null`() = runBlocking {
        // Given
        val email = "nonexistent@example.com"
        val password = "password123"

        coEvery { repository.login(email, password) } returns null

        // When
        val result = loginUseCase(email, password)

        // Then
        assertNull(result)
        coVerify(exactly = 1) { repository.login(email, password) }
    }
}

