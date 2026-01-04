package es.etg.lectoguard.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.etg.lectoguard.domain.model.BookRecommendation
import es.etg.lectoguard.domain.model.UserInterests
import es.etg.lectoguard.domain.usecase.CalculateUserInterestsUseCase
import es.etg.lectoguard.domain.usecase.GetRecommendationsUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecommendationViewModel @Inject constructor(
    private val calculateInterestsUseCase: CalculateUserInterestsUseCase,
    private val getRecommendationsUseCase: GetRecommendationsUseCase
) : ViewModel() {

    val userInterests = MutableLiveData<UserInterests?>()
    val recommendations = MutableLiveData<List<BookRecommendation>>(emptyList())
    val isLoading = MutableLiveData<Boolean>(false)

    fun calculateInterests(userId: Int, firebaseUid: String) {
        viewModelScope.launch {
            isLoading.postValue(true)
            try {
                val interests = calculateInterestsUseCase(userId, firebaseUid)
                userInterests.postValue(interests)
            } catch (e: Exception) {
                android.util.Log.e("RecommendationViewModel", "Error calculando intereses: ${e.message}", e)
                userInterests.postValue(null)
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    fun loadRecommendations(
        userId: Int,
        firebaseUid: String,
        limit: Int = 10,
        genreFilter: es.etg.lectoguard.domain.model.BookGenre? = null,
        onlyFromFollowing: Boolean = false
    ) {
        viewModelScope.launch {
            isLoading.postValue(true)
            try {
                val recs = getRecommendationsUseCase(userId, firebaseUid, limit, genreFilter, onlyFromFollowing)
                recommendations.postValue(recs)
            } catch (e: Exception) {
                android.util.Log.e("RecommendationViewModel", "Error cargando recomendaciones: ${e.message}", e)
                recommendations.postValue(emptyList())
            } finally {
                isLoading.postValue(false)
            }
        }
    }

}

