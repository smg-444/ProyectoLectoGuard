package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.ReadingListRepository
import es.etg.lectoguard.domain.model.ReadingList
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SaveReadingListUseCaseTest {

    private lateinit var repository: ReadingListRepository
    private lateinit var saveReadingListUseCase: SaveReadingListUseCase

    @Before
    fun setup() {
        repository = mockk()
        saveReadingListUseCase = SaveReadingListUseCase(repository)
    }

    @Test
    fun `saveReadingList creates new list successfully`() = runBlocking {
        // Given
        val readingList = ReadingList(
            id = "",
            userId = "user123",
            name = "My Favorites",
            description = "My favorite books",
            isPublic = false,
            bookIds = emptyList()
        )
        val expectedId = "list123"

        coEvery { repository.saveReadingList(readingList) } returns expectedId

        // When
        val result = saveReadingListUseCase(readingList)

        // Then
        assertEquals(expectedId, result)
        coVerify(exactly = 1) { repository.saveReadingList(readingList) }
    }

    @Test
    fun `saveReadingList updates existing list`() = runBlocking {
        // Given
        val readingList = ReadingList(
            id = "list123",
            userId = "user123",
            name = "Updated Name",
            description = "Updated description",
            isPublic = true,
            bookIds = listOf(1, 2, 3)
        )

        coEvery { repository.saveReadingList(readingList) } returns "list123"

        // When
        val result = saveReadingListUseCase(readingList)

        // Then
        assertEquals("list123", result)
        coVerify(exactly = 1) { repository.saveReadingList(readingList) }
    }
}

