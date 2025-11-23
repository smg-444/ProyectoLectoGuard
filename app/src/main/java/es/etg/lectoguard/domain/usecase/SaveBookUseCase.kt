package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.BookRepository
import es.etg.lectoguard.data.local.UserBookEntity

class SaveBookUseCase(private val repository: BookRepository) {
    suspend operator fun invoke(userBook: UserBookEntity): Boolean = repository.saveBook(userBook)
} 