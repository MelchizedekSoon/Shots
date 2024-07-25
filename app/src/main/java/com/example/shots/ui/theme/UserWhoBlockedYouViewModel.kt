package com.example.shots.ui.theme

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shots.data.UserWhoBlockedYou
import com.example.shots.data.UserWhoBlockedYouRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class UserWhoBlockedYouUiState(
    val usersWhoBlockedYou: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class UserWhoBlockedYouViewModel @Inject constructor(
    private val userWhoBlockedYouRepository: UserWhoBlockedYouRepository
) : ViewModel() {


    fun getYourUserId(): String {
        return userWhoBlockedYouRepository.getYourUserId()
    }

    private val _uiState = MutableStateFlow(UserWhoBlockedYouUiState())
    val uiState: StateFlow<UserWhoBlockedYouUiState> = _uiState.asStateFlow()

//    val userWhoBlockedYouDao = RoomModule.provideUserWhoBlockedYouDao(appDatabase)

//    fun fetchUserWhoBlockedYouFromRoom(userWhoBlockedYouId: String): UserWhoBlockedYou {
//        return try {
//            val userWhoBlockedYou = userWhoBlockedYouDao.findById(userWhoBlockedYouId)
//            if (userWhoBlockedYou != null) {
//                userWhoBlockedYou
//            } else {
//                UserWhoBlockedYou(userWhoBlockedYouId, mutableListOf())
//            }
//        } catch (npe: NullPointerException) {
//            UserWhoBlockedYou(userWhoBlockedYouId, mutableListOf())
//        }
//    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                loadUsersWhoBlockedYou()
            } catch (npe: NullPointerException) {
                Log.d("UserWhoBlockedYouViewModel", "No UserWhoBlockedYou found!")
            }
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun loadUsersWhoBlockedYou() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                userWhoBlockedYouRepository.getUsersWhoBlockedYou()
                    .collect { returnedUsersWhoBlockedYou ->
                        _uiState.value =
                            UserWhoBlockedYouUiState(usersWhoBlockedYou = returnedUsersWhoBlockedYou)
                    }
            } catch (e: Exception) {
                _uiState.value = UserWhoBlockedYouUiState(errorMessage = e.message)
            }
        }
    }

    suspend fun fetchUpdatedUsersWhoBlockedYou(): Flow<List<String>> {
        return userWhoBlockedYouRepository.fetchUpdatedUsersWhoBlockedYou()
    }

    suspend fun storeUserWhoBlockedYou(userWhoBlockedYou: UserWhoBlockedYou) {
        return userWhoBlockedYouRepository.storeUserWhoBlockedYou(userWhoBlockedYou)
    }

    fun storeUserWhoBlockedYouInRoom() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val userWhoBlockedYou =
                        userWhoBlockedYouRepository.getUserWhoBlockedYou(userWhoBlockedYouRepository.getYourUserId())
                    userWhoBlockedYouRepository.storeUserWhoBlockedYou(userWhoBlockedYou)
                } catch (noSuchUserException: Exception) {
                    Log.d("UserWhoBlockedYouViewModel", "No UserWhoBlockedYou found!")
                }
            }
        }

//        viewModelScope.launch {
//            withContext(Dispatchers.IO) {
//                val yourUserId = firebaseAuth.currentUser?.displayName ?: ""
//                val usersWhoBlockedYouList = getUserWhoBlockedYouFromFirebase(userId)
//                Log.d("UserWhoBlockedYouViewModel", "usersWhoBlockedYouList: $usersWhoBlockedYouList")
//                try {
//                    var userWhoBlockedYou = userWhoBlockedYouDao.findById(yourUserId)
//                    userWhoBlockedYou = userWhoBlockedYou.copy(
//                        userWhoBlockedYouId = yourUserId,
//                        usersWhoBlockedYou = usersWhoBlockedYouList.toMutableList()
//                    )
//                    userWhoBlockedYouDao.insert(userWhoBlockedYou)
//                } catch (npe: java.lang.NullPointerException) {
//                    val userWhoBlockedYou = UserWhoBlockedYou(yourUserId, usersWhoBlockedYouList.toMutableList())
//                    Log.d("blockedUserViewModel", "blockedUser was stored!")
//                    try {
//                        userWhoBlockedYouDao.insert(userWhoBlockedYou)
//                    } catch(e: Exception) {
//                        Log.d("blockedUserViewModel", "blockedUser failed to be stored!")
//                    }
//                }
//            }
//        }
    }

    suspend fun saveAndStoreUser(
        blockedUserId: String,
        blockedUserData: MutableMap<String, Any>
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                userWhoBlockedYouRepository.saveAndStoreUserWhoBlockedYou(
                    blockedUserId,
                    blockedUserData
                )
                loadUsersWhoBlockedYou()
            }
        }
    }

//
//    suspend fun removeUserWhoBlockedYouFromFirebase(
//        blockedUserId: String
//    ) {
//        viewModelScope.launch {
//            val userId = firebaseAuth.currentUser?.displayName ?: ""
//            val success = firebaseRepository.deleteUserWhoBlockedYouFromFirebase(blockedUserId)
//            if (success) {
//                Log.d("blockedUserViewModel", "blockedUser deleted!")
//                storeUserWhoBlockedYouInRoom(userId)
//            } else {
//                Log.d("blockedUserViewModel", "blockedUser failed to be deleted!")
//            }
//        }
//    }

    fun getUserWhoBlockedYouFromFirebase(blockedUserId: String): List<String> {
        return userWhoBlockedYouRepository.getUserWhoBlockedYou(userWhoBlockedYouRepository.getYourUserId()).usersWhoBlockedYou
    }

    fun deleteUserWhoBlockedYou(userWhoBlockedYouId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                userWhoBlockedYouRepository.deleteUserWhoBlockedYou(userWhoBlockedYouId)
                loadUsersWhoBlockedYou()
            }
        }
    }


}