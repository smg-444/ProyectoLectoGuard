package es.etg.lectoguard.domain.usecase

import es.etg.lectoguard.data.repository.UserRepository
 
class LoginUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(email: String, password: String, isOnline: Boolean = true) = 
        repository.login(email, password, isOnline)
} 