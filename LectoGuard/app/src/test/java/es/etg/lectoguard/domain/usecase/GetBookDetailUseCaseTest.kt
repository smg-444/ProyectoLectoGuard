package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.remote.BookDetailResponse
import es.etg.lectoguard.data.repository.BookRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class GetBookDetailUseCaseTest {

    private lateinit var repository: BookRepository
    private lateinit var getBookDetailUseCase: GetBookDetailUseCase

    @Before
    fun setup() {
        repository = mockk()
        getBookDetailUseCase = GetBookDetailUseCase(repository)
    }

    @Test
    fun `getBookDetail returns successful response`() = runBlocking {
        // Given
        val bookId = 1
        val expectedDetail = BookDetailResponse(
            id = 1,
            title = "Test Book",
            coverImage = "cover_url",
            sinopsis = "Test synopsis",
            firstPage = "First page content",
            genres = listOf("FICTION")
        )
        val response = Response.success(expectedDetail)

        coEvery { repository.getBookDetailOnline(bookId) } returns response

        // When
        val result = getBookDetailUseCase(bookId)

        // Then
        assertTrue(result.isSuccessful)
        assertEquals(expectedDetail, result.body())
        coVerify(exactly = 1) { repository.getBookDetailOnline(bookId) }
    }

    @Test
    fun `getBookDetail returns error response`() = runBlocking {
        // Given
        val bookId = 999
        val response = mockk<Response<BookDetailResponse>>(relaxed = true)
        coEvery { response.isSuccessful } returns false
        coEvery { response.code() } returns 404

        coEvery { repository.getBookDetailOnline(bookId) } returns response

        // When
        val result = getBookDetailUseCase(bookId)

        // Then
        assertFalse(result.isSuccessful)
        assertEquals(404, result.code())
        coVerify(exactly = 1) { repository.getBookDetailOnline(bookId) }
    }
}

