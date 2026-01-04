package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.ReadingListRepository

class RemoveBookFromListUseCase(private val repository: ReadingListRepository) {
    suspend operator fun invoke(listId: String, bookId: Int, syncToFirestore: Boolean = true): Boolean =
        repository.removeBookFromList(listId, bookId, syncToFirestore)
}

