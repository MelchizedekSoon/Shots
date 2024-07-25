package com.example.shots.ui.theme

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shots.data.FirebaseRepository
import com.example.shots.data.User
import com.example.shots.data.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val mediaOne: String? = "",
    val displayName: String? = "",
    val userName: String? = "",
    val likesReceived: Int? = 0,
    val shotsReceived: Int? = 0,
    val timesBookmarked: Int? = 0
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    val userRepository: UserRepository,
    val firebaseRepository: FirebaseRepository
) : ViewModel() {

    private val _userUiState = MutableStateFlow(User())
    val userUiState: StateFlow<User> = _userUiState.asStateFlow()

    private val _profileUiState = MutableStateFlow(ProfileUiState())
    val profileUiState: StateFlow<ProfileUiState> = _profileUiState.asStateFlow()

    init {
        try {
            loadProfileFields()
        } catch(npe: NullPointerException) {
            Log.d("ProfileViewModel", "loadProfileFields: ${npe.message}")
        }
    }

    fun getYourUserId(): String {
        return firebaseRepository.getYourUserId()
    }

    fun loadProfileFields() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                userRepository.getCurrentUser().collect { returnedUser ->
                    _profileUiState.value = ProfileUiState(
                        mediaOne = returnedUser.mediaOne,
                        displayName = returnedUser.displayName,
                        userName = returnedUser.userName,
                        likesReceived = returnedUser.likesCount,
                        shotsReceived = returnedUser.shotsCount,
                        timesBookmarked = returnedUser.timesBookmarkedCount
                    )
                    Log.d("ProfileViewModel", "returnedUser = $returnedUser")
                }
            } catch(npe: NullPointerException) {
                Log.d("ProfileViewModel", "loadProfileFields: ${npe.message}")
            }
        }
    }


}