package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.local.BookEntity
import es.etg.lectoguard.data.repository.BookRepository

class GetBooksByTagUseCase(private val repository: BookRepository) {
    suspend operator fun invoke(userId: Int, tag: String): List<BookEntity> =
        repository.getBooksByTag(userId, tag)
}

