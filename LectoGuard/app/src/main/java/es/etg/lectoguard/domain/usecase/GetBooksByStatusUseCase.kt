package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.local.BookEntity
import es.etg.lectoguard.data.repository.BookRepository
import es.etg.lectoguard.domain.model.ReadingStatus

class GetBooksByStatusUseCase(private val repository: BookRepository) {
    suspend operator fun invoke(userId: Int, status: ReadingStatus): List<BookEntity> =
        repository.getBooksByUserAndStatus(userId, status)
}

