package es.etg.lectoguard.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.etg.lectoguard.domain.model.FeedItem
import es.etg.lectoguard.domain.usecase.GetUserFeedUseCase
import es.etg.lectoguard.domain.usecase.ObserveUserFeedUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val getUserFeedUseCase: GetUserFeedUseCase,
    private val observeUserFeedUseCase: ObserveUserFeedUseCase,
    private val getMoreFeedItemsUseCase: es.etg.lectoguard.domain.usecase.GetMoreFeedItemsUseCase? = null
) : ViewModel() {
    
    val feedItems = MutableLiveData<List<FeedItem>>()
    val isLoading = MutableLiveData<Boolean>()
    val isLoadingMore = MutableLiveData<Boolean>(false)
    val error = MutableLiveData<String?>()
    private var hasMoreItems = true
    
    /**
     * Carga el feed del usuario (una sola vez)
     */
    fun loadUserFeed(userId: String, limit: Int = 50) {
        viewModelScope.launch {
            isLoading.postValue(true)
            try {
                val items = getUserFeedUseCase(userId, limit)
                feedItems.postValue(items)
                error.postValue(null)
            } catch (e: Exception) {
                error.postValue(e.message)
            } finally {
                isLoading.postValue(false)
            }
        }
    }
    
    /**
     * Inicia el listener en tiempo real para el feed del usuario
     */
    fun startObservingFeed(userId: String, limit: Int = 50) {
        observeUserFeedUseCase(userId, limit)
            .onEach { items ->
                feedItems.postValue(items)
                error.postValue(null)
                hasMoreItems = items.size >= limit
            }
            .launchIn(viewModelScope)
    }
    
    /**
     * Carga más items del feed (paginación)
     */
    fun loadMoreFeedItems(userId: String, limit: Int = 20) {
        val getMoreUseCase = getMoreFeedItemsUseCase ?: return
        val currentItems = feedItems.value ?: emptyList()
        if (currentItems.isEmpty() || !hasMoreItems) return
        
        viewModelScope.launch {
            isLoadingMore.postValue(true)
            try {
                val lastTimestamp = currentItems.lastOrNull()?.timestamp ?: System.currentTimeMillis()
                val moreItems = getMoreUseCase(userId, lastTimestamp, limit)
                
                if (moreItems.isEmpty()) {
                    hasMoreItems = false
                } else {
                    val updatedItems = currentItems + moreItems
                    feedItems.postValue(updatedItems)
                }
                error.postValue(null)
            } catch (e: Exception) {
                error.postValue(e.message)
            } finally {
                isLoadingMore.postValue(false)
            }
        }
    }
    
    /**
     * Verifica si hay más items para cargar
     */
    fun hasMoreItems(): Boolean = hasMoreItems
}

