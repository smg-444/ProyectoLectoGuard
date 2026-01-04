package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.ReadingListRepository
import es.etg.lectoguard.domain.model.ReadingList

class SaveReadingListUseCase(private val repository: ReadingListRepository) {
    suspend operator fun invoke(list: ReadingList, syncToFirestore: Boolean = true): String? =
        repository.saveReadingList(list, syncToFirestore)
}

