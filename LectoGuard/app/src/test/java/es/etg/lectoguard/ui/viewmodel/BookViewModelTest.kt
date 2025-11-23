package es.etg.lectoguard.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import es.etg.lectoguard.data.local.BookEntity
import es.etg.lectoguard.data.repository.BookRepository
import es.etg.lectoguard.domain.usecase.GetBooksUseCase
import es.etg.lectoguard.domain.usecase.SaveBookUseCase
import es.etg.lectoguard.domain.usecase.GetBookDetailUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: BookRepository
    private lateinit var viewModel: BookViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        viewModel = BookViewModel(
            repository,
            GetBooksUseCase(repository),
            SaveBookUseCase(repository),
            GetBookDetailUseCase(repository)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadBooks posts books from repository`() {
        val books = listOf(BookEntity(1, "Test Book", "url"))
        coEvery { repository.getAllBooks(true) } returns books

        viewModel.loadBooks(true)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(books, viewModel.books.value)
    }
}