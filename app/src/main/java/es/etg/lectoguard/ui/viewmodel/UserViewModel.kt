package es.etg.lectoguard.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.etg.lectoguard.data.local.UserEntity
import es.etg.lectoguard.domain.usecase.LoginUseCase
import es.etg.lectoguard.domain.usecase.RegisterUseCase
import kotlinx.coroutines.launch

class UserViewModel(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    val user = MutableLiveData<UserEntity?>()
    val loginResult = MutableLiveData<Boolean>()
    val registerResult = MutableLiveData<Boolean>()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val result = loginUseCase(email, password)
            user.postValue(result)
            loginResult.postValue(result != null)
        }
    }

    fun register(userEntity: UserEntity) {
        viewModelScope.launch {
            val id = registerUseCase(userEntity)
            registerResult.postValue(id > 0)
        }
    }
} 