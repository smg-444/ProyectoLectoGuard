package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.BookRepository

class GetBooksUseCase(private val repository: BookRepository) {
    suspend operator fun invoke(isOnline: Boolean) = repository.getAllBooks(isOnline)
} 