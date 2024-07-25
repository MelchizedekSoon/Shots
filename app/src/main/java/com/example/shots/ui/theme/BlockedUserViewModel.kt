package com.example.shots.ui.theme

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shots.data.BlockedUser
import com.example.shots.data.BlockedUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class BlockedUserUiState(
    val blockedUsers: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class BlockedUserViewModel @Inject constructor(
    private val blockedUserRepository: BlockedUserRepository,
    private val dispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(BlockedUserUiState())
    val uiState: StateFlow<BlockedUserUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(dispatcher) {
            try {
                loadBlockedUsers()
            } catch (npe: NullPointerException) {
                Log.d("blockedUserViewModel", "blockedUsers list is empty!")
            }
        }
    }

    val scope = CoroutineScope(Dispatchers.IO)

    fun loadBlockedUsers() {
        viewModelScope.launch(dispatcher) {
            try {
                blockedUserRepository.fetchUpdatedBlockedUsers().collect { blockedUsers ->
                    _uiState.value = BlockedUserUiState(blockedUsers = blockedUsers)
                }
            } catch (e: Exception) {
                _uiState.value = BlockedUserUiState(errorMessage = e.localizedMessage)
            }
        }
    }

    fun fetchBlockedUserObject(): BlockedUser {
        return blockedUserRepository.getBlockedUserObject()
//        return try {
//            val blockedUser = blockedUserDao.findById(blockedUserId)
//            blockedUser
//        } catch (npe: NullPointerException) {
//            BlockedUser(blockedUserId, mutableListOf())
//        }
    }

    suspend fun getBlockedUsers(): Flow<List<String>> {
        return blockedUserRepository.getBlockedUsers()
    }

    suspend fun fetchUpdatedBlockedUsers(): Flow<List<String>> {
        return blockedUserRepository.fetchUpdatedBlockedUsers()
    }

    fun storeBlockedUser(userId: String) {
        viewModelScope.launch(dispatcher){
            blockedUserRepository.storeBlockedUser(userId)
        }
    }

    fun storeBlockedUserObject(blockedUser: BlockedUser) {
        viewModelScope.launch(dispatcher) {
                blockedUserRepository.storeBlockedUserObject(blockedUser)
        }
    }


    fun saveAndStoreBlockedUser(
        blockedUserId: String,
        blockedUserData: MutableMap<String, Any>
    ) {
        viewModelScope.launch(dispatcher) {
                blockedUserRepository
                    .saveAndStoreBlockedUser(blockedUserId, blockedUserData)
                loadBlockedUsers()
        }
    }

    fun deleteBlockedUser(blockedUserId: String) {
        viewModelScope.launch(dispatcher) {
            Log.d("blockedUserViewModel", "Inside deleteBlockedUser where blockedUserId = $blockedUserId")
                blockedUserRepository.deleteBlockedUser(blockedUserId)
                loadBlockedUsers()
        }
    }

//    fun storeBlockedUserInRoom(userId: String) {
//        viewModelScope.launch {
//            withContext(Dispatchers.IO) {
//
////                val yourUserId = firebaseAuth.currentUser?.displayName ?: ""
//                val blockedUsersList = blockedUserRepository.getBlockedUser(userId).blockedUsers
////                val blockedUsersList = getBlockedUsersFromFirebase(userId)
//                Log.d("blockedUserViewModel", "blockedUsersList: $blockedUsersList")
//                try {
//                    var blockedUser = blockedUserRepository.getBlockedUser(userId)
////                    var blockedUser = blockedUserDao.findById(userId)
//                    blockedUser = blockedUser.copy(
//                        blockedUserId = userId,
//                        blockedUsers = blockedUsersList
//                    )
//                    blockedUserDao.insert(blockedUser)
//                } catch (npe: java.lang.NullPointerException) {
//                    val blockedUser = BlockedUser(userId, blockedUsersList.toMutableList())
//                    Log.d("blockedUserViewModel", "blockedUser was stored!")
//                    try {
//                        blockedUserDao.insert(blockedUser)
//                    } catch (e: Exception) {
//                        Log.d("blockedUserViewModel", "blockedUser failed to be stored!")
//                    }
//                }
//            }
//        }
//    }

//    suspend fun saveBlockedUserToFirebase(
//        blockedUserId: String,
//        blockedUserData: MutableMap<String, Any>
//    ) {
//        viewModelScope.launch {
//            val userId = firebaseAuth.currentUser?.displayName ?: ""
//            val success =
//                firebaseRepository.writeBlockedUserToFirebase(blockedUserId, blockedUserData)
//            if (success) {
//                Log.d("blockedUserViewModel", "blockedUser saved!")
//                storeBlockedUserInRoom(userId)
//            } else {
//                Log.d("blockedUserViewModel", "blockedUser failed to be saved!")
//            }
//        }
//    }

//    suspend fun removeBlockedUserFromFirebase(
//        blockedUserId: String
//    ) {
//        viewModelScope.launch {
//            val userId = firebaseAuth.currentUser?.displayName ?: ""
//            val success = firebaseRepository.deleteBlockedUserFromFirebase(blockedUserId)
//            if (success) {
//                Log.d("blockedUserViewModel", "blockedUser deleted!")
//                storeBlockedUserInRoom(userId)
//            } else {
//                Log.d("blockedUserViewModel", "blockedUser failed to be deleted!")
//            }
//        }
//    }

//    suspend fun getBlockedUsersFromFirebase(blockedUserId: String): List<String> {
//        return firebaseRepository.getBlockedUsersFromFirebase(blockedUserId)
//    }


}