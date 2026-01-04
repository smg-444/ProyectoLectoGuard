package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.ReadingListRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateListOrderUseCaseTest {

    private lateinit var repository: ReadingListRepository
    private lateinit var updateListOrderUseCase: UpdateListOrderUseCase

    @Before
    fun setup() {
        repository = mockk()
        updateListOrderUseCase = UpdateListOrderUseCase(repository)
    }

    @Test
    fun `updateListOrder updates order successfully`() = runBlocking {
        // Given
        val listId = "list123"
        val newOrder = listOf(3, 1, 2, 4)

        coEvery { repository.updateListOrder(listId, newOrder, true) } returns true

        // When
        val result = updateListOrderUseCase(listId, newOrder, true)

        // Then
        assertTrue(result)
        coVerify(exactly = 1) { repository.updateListOrder(listId, newOrder, true) }
    }

    @Test
    fun `updateListOrder returns false when list not found`() = runBlocking {
        // Given
        val listId = "nonexistent"
        val newOrder = listOf(1, 2, 3)

        coEvery { repository.updateListOrder(listId, newOrder, true) } returns false

        // When
        val result = updateListOrderUseCase(listId, newOrder, true)

        // Then
        assertFalse(result)
        coVerify(exactly = 1) { repository.updateListOrder(listId, newOrder, true) }
    }

    @Test
    fun `updateListOrder with empty list`() = runBlocking {
        // Given
        val listId = "list123"
        val emptyOrder = emptyList<Int>()

        coEvery { repository.updateListOrder(listId, emptyOrder, true) } returns true

        // When
        val result = updateListOrderUseCase(listId, emptyOrder, true)

        // Then
        assertTrue(result)
        coVerify(exactly = 1) { repository.updateListOrder(listId, emptyOrder, true) }
    }
}

