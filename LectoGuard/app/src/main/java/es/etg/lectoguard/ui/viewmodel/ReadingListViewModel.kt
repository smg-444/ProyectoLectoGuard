package es.etg.lectoguard.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.etg.lectoguard.domain.model.ReadingList
import es.etg.lectoguard.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReadingListViewModel @Inject constructor(
    private val getUserReadingListsUseCase: GetUserReadingListsUseCase,
    private val getReadingListByIdUseCase: GetReadingListByIdUseCase,
    private val saveReadingListUseCase: SaveReadingListUseCase,
    private val updateListOrderUseCase: UpdateListOrderUseCase,
    private val addBookToListUseCase: AddBookToListUseCase,
    private val removeBookFromListUseCase: RemoveBookFromListUseCase,
    private val deleteReadingListUseCase: DeleteReadingListUseCase,
    private val getPublicReadingListsUseCase: GetPublicReadingListsUseCase,
    private val followListUseCase: FollowListUseCase,
    private val unfollowListUseCase: UnfollowListUseCase,
    private val isFollowingListUseCase: IsFollowingListUseCase
) : ViewModel() {

    // Estado de las listas del usuario
    private val _userReadingLists = MutableStateFlow<List<ReadingList>>(emptyList())
    val userReadingLists: StateFlow<List<ReadingList>> = _userReadingLists.asStateFlow()

    // Lista actual seleccionada
    private val _currentList = MutableStateFlow<ReadingList?>(null)
    val currentList: StateFlow<ReadingList?> = _currentList.asStateFlow()

    // Listas públicas
    private val _publicReadingLists = MutableLiveData<List<ReadingList>>()
    val publicReadingLists: LiveData<List<ReadingList>> = _publicReadingLists

    // Estado de carga
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Resultados de operaciones
    private val _saveResult = MutableLiveData<Boolean?>()
    val saveResult: LiveData<Boolean?> = _saveResult

    private val _deleteResult = MutableLiveData<Boolean>()
    val deleteResult: LiveData<Boolean> = _deleteResult

    private val _followResult = MutableLiveData<Boolean>()
    val followResult: LiveData<Boolean> = _followResult

    // Estado de si está siguiendo una lista
    private val _isFollowing = MutableLiveData<Boolean>()
    val isFollowing: LiveData<Boolean> = _isFollowing

    /**
     * Carga las listas del usuario
     */
    fun loadUserReadingLists(userId: String) {
        viewModelScope.launch {
            getUserReadingListsUseCase(userId).collect { lists ->
                _userReadingLists.value = lists
            }
        }
    }

    /**
     * Carga una lista por ID
     */
    fun loadReadingList(listId: String) {
        viewModelScope.launch {
            getReadingListByIdUseCase(listId).collect { list ->
                _currentList.value = list
            }
        }
    }

    /**
     * Crea o actualiza una lista
     */
    fun saveReadingList(list: ReadingList, syncToFirestore: Boolean = true) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val listId = saveReadingListUseCase(list, syncToFirestore)
                _saveResult.value = listId != null
                if (listId != null) {
                    // Recargar listas del usuario
                    loadUserReadingLists(list.userId)
                }
            } catch (e: Exception) {
                _saveResult.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Actualiza el orden de los libros en una lista
     */
    fun updateListOrder(listId: String, bookIds: List<Int>, syncToFirestore: Boolean = true) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = updateListOrderUseCase(listId, bookIds, syncToFirestore)
                if (success) {
                    // Recargar la lista actual
                    loadReadingList(listId)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Agrega un libro a una lista
     */
    fun addBookToList(listId: String, bookId: Int, syncToFirestore: Boolean = true) {
        viewModelScope.launch {
            val success = addBookToListUseCase(listId, bookId, syncToFirestore)
            if (success) {
                loadReadingList(listId)
                // Recargar todas las listas del usuario para actualizar el display
                val currentUserId = _userReadingLists.value.firstOrNull()?.userId
                if (currentUserId != null) {
                    loadUserReadingLists(currentUserId)
                }
            }
        }
    }

    /**
     * Elimina un libro de una lista
     */
    fun removeBookFromList(listId: String, bookId: Int, syncToFirestore: Boolean = true) {
        viewModelScope.launch {
            val success = removeBookFromListUseCase(listId, bookId, syncToFirestore)
            if (success) {
                loadReadingList(listId)
                // Recargar todas las listas del usuario para actualizar el display
                val currentUserId = _userReadingLists.value.firstOrNull()?.userId
                if (currentUserId != null) {
                    loadUserReadingLists(currentUserId)
                }
            }
        }
    }

    /**
     * Elimina una lista
     */
    fun deleteReadingList(listId: String, userId: String, syncToFirestore: Boolean = true) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = deleteReadingListUseCase(listId, syncToFirestore)
                _deleteResult.value = success
                if (success) {
                    // Recargar listas del usuario
                    loadUserReadingLists(userId)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Carga listas públicas
     */
    fun loadPublicReadingLists(limit: Int = 50) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val lists = getPublicReadingListsUseCase(limit)
                _publicReadingLists.value = lists
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Sigue una lista
     */
    fun followList(listId: String, userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = followListUseCase(listId, userId)
                _followResult.value = success
                if (success) {
                    checkIfFollowing(listId, userId)
                    // Recargar la lista para actualizar contador
                    loadReadingList(listId)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Deja de seguir una lista
     */
    fun unfollowList(listId: String, userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = unfollowListUseCase(listId, userId)
                _followResult.value = success
                if (success) {
                    checkIfFollowing(listId, userId)
                    // Recargar la lista para actualizar contador
                    loadReadingList(listId)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Verifica si el usuario está siguiendo una lista
     */
    fun checkIfFollowing(listId: String, userId: String) {
        viewModelScope.launch {
            val isFollowing = isFollowingListUseCase(listId, userId)
            _isFollowing.value = isFollowing
        }
    }
}

