package com.example.shots.ui.theme

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shots.data.Distance
import com.example.shots.data.FirebaseRepository
import com.example.shots.data.ShowMe
import com.example.shots.data.User
import com.example.shots.data.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FilterUiState(
    var showMe: ShowMe? = null,
    val showUsers: Distance = Distance.ANYWHERE,
    val acceptShots: Distance = Distance.ANYWHERE,
    val ageMinToShow: Int = 18,
    val ageMaxToShow: Int = 35,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class FilterViewModel @Inject constructor(
    firebaseRepository: FirebaseRepository,
    private val userRepository: UserRepository,
    private val dispatcher: CoroutineDispatcher
) : ViewModel() {

    val scope = CoroutineScope(dispatcher)

    var user: User? = null

    private val _uiState = MutableStateFlow(FilterUiState())
    val uiState: StateFlow<FilterUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(dispatcher) {
            userRepository.fetchUpdatedCurrentUser().collect { returnedUser ->
                user = returnedUser
            }
            try {
                loadFilters()
            } catch(npe: NullPointerException) {
                Log.d("FilterViewModel", "NullPointerException")
            }
        }
    }

     fun loadFilters() {
        viewModelScope.launch(dispatcher) {
            Log.d("FilterViewModel", "inside loadFilters")
            var user = User()
            userRepository.fetchUpdatedCurrentUser().collect { returnedUser ->
                Log.d("FilterViewModel", "inside loadFilters - returnedUser: $returnedUser")
                _uiState.value = FilterUiState( // Update the MutableStateFlow
                    showMe = returnedUser.showMe,
                    showUsers = returnedUser.showUsers,
                    acceptShots = returnedUser.acceptShots,
                    ageMinToShow = returnedUser.ageMinToShow ?: 18,
                    ageMaxToShow = returnedUser.ageMaxToShow ?: 35
                )
                user = returnedUser
            }
            Log.d("FilterViewModel", "user: $user")
        }
    }

    fun updateShowMe(showMe: ShowMe) {
        _uiState.value = _uiState.value.copy(showMe = showMe)
    }

    fun updateShowUsers(showUsers: Distance) {
        _uiState.value = _uiState.value.copy(showUsers = showUsers)
    }
    fun updateAcceptShots(acceptShots: Distance) {
        _uiState.value = _uiState.value.copy(acceptShots = acceptShots)
    }

    fun updateAgeMinToShow(ageMinToShow: Int) {
        _uiState.value = _uiState.value.copy(ageMinToShow = ageMinToShow)
    }

    fun updateAgeMaxToShow(ageMaxToShow: Int) {
        _uiState.value = _uiState.value.copy(ageMaxToShow = ageMaxToShow)
    }

    fun saveAndStoreFilters(context: Context) {
        viewModelScope.launch(dispatcher) {

            userRepository.getCurrentUser().collect {
                user = it
            }

            Log.d("FilterViewModel", "Inside saveAndStoreFilters - ShowMe Value = ${_uiState.value.showMe}")

            val userData: MutableMap<String, Any> = mutableMapOf()
            val mediaItems: MutableMap<String, Uri> = mutableMapOf()

            userData["showMe"] =
                when (_uiState.value.showMe) {
                    ShowMe.MEN -> "MEN"
                    ShowMe.WOMEN -> "WOMEN"
                    ShowMe.ANYONE -> "ANYONE"
                    else -> "UNKNOWN"
                }

            userData["showUsers"] =
                when (_uiState.value.showUsers) {
                    Distance.TEN -> "TEN"
                    Distance.TWENTY -> "TWENTY"
                    Distance.THIRTY -> "THIRTY"
                    Distance.FORTY -> "FORTY"
                    Distance.FIFTY -> "FIFTY"
                    Distance.SIXTY -> "SIXTY"
                    Distance.SEVENTY -> "SEVENTY"
                    Distance.EIGHTY -> "EIGHTY"
                    Distance.NINETY -> "NINETY"
                    Distance.ONE_HUNDRED -> "ONE_HUNDRED"
                    Distance.ANYWHERE -> "ANYWHERE"
                }

            userData["acceptShots"] =
                when (_uiState.value.acceptShots) {
                    Distance.TEN -> "TEN"
                    Distance.TWENTY -> "TWENTY"
                    Distance.THIRTY -> "THIRTY"
                    Distance.FORTY -> "FORTY"
                    Distance.FIFTY -> "FIFTY"
                    Distance.SIXTY -> "SIXTY"
                    Distance.SEVENTY -> "SEVENTY"
                    Distance.EIGHTY -> "EIGHTY"
                    Distance.NINETY -> "NINETY"
                    Distance.ONE_HUNDRED -> "ONE_HUNDRED"
                    Distance.ANYWHERE -> "ANYWHERE"
                }

            userData["ageMinToShow"] = _uiState.value.ageMinToShow
            userData["ageMaxToShow"] = _uiState.value.ageMaxToShow

            Log.d("FilterViewModel", "userId = ${user?.id}")

            userRepository.saveUserData(
                user?.id ?: "",
                userData, mediaItems, context
            )

            loadFilters()
        }
    }


}