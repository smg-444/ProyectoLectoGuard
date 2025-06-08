package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.BookRepository

class GetBookDetailUseCase(val repository: BookRepository) {
    suspend operator fun invoke(id: Int) = repository.getBookDetailOnline(id)
} 