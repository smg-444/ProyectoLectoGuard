package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.ReadingListRepository

class DeleteReadingListUseCase(private val repository: ReadingListRepository) {
    suspend operator fun invoke(listId: String, syncToFirestore: Boolean = true): Boolean =
        repository.deleteReadingList(listId, syncToFirestore)
}

