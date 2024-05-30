
package com.example.shots.ui.theme

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shots.RoomModule
import com.example.shots.data.AppDatabase
import com.example.shots.data.BlockedUser
import com.example.shots.data.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BlockedUserViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val firebaseAuth: FirebaseAuth,
    private val appDatabase: AppDatabase
) : ViewModel() {

    val blockedUserDao = RoomModule.provideBlockedUserDao(appDatabase)
    val userWhoBlockedYouDao = RoomModule.provideUserWhoBlockedYouDao(appDatabase)
    val scope = CoroutineScope(Dispatchers.IO)

    fun fetchBlockedUserFromRoom(blockedUserId: String): BlockedUser {
        return try {
            val blockedUser = blockedUserDao.findById(blockedUserId)
            blockedUser
        } catch (npe: NullPointerException) {
            BlockedUser(blockedUserId, mutableListOf())
        }
    }

    fun storeBlockedUserInRoom(userId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val yourUserId = firebaseAuth.currentUser?.displayName ?: ""
                val blockedUsersList = getBlockedUsersFromFirebase(userId)
                Log.d("blockedUserViewModel", "blockedUsersList: $blockedUsersList")
                try {
                    var blockedUser = blockedUserDao.findById(userId)
                    blockedUser = blockedUser.copy(
                        blockedUserId = userId,
                        blockedUsers = blockedUsersList.toMutableList()
                    )
                    blockedUserDao.insert(blockedUser)
                } catch (npe: java.lang.NullPointerException) {
                    val blockedUser = BlockedUser(userId, blockedUsersList.toMutableList())
                    Log.d("blockedUserViewModel", "blockedUser was stored!")
                    try {
                        blockedUserDao.insert(blockedUser)
                    } catch(e: Exception) {
                        Log.d("blockedUserViewModel", "blockedUser failed to be stored!")
                    }
                }
            }
        }
    }

    suspend fun saveBlockedUserToFirebase(
        blockedUserId: String,
        blockedUserData: MutableMap<String, Any>
    ) {
        viewModelScope.launch {
            val userId = firebaseAuth.currentUser?.displayName ?: ""
            val success = firebaseRepository.writeBlockedUserToFirebase(blockedUserId, blockedUserData)
            if (success) {
                Log.d("blockedUserViewModel", "blockedUser saved!")
                storeBlockedUserInRoom(userId)
            } else {
                Log.d("blockedUserViewModel", "blockedUser failed to be saved!")
            }
        }
    }

    suspend fun removeBlockedUserFromFirebase(
        blockedUserId: String
    ) {
        viewModelScope.launch {
            val userId = firebaseAuth.currentUser?.displayName ?: ""
            val success = firebaseRepository.deleteBlockedUserFromFirebase(blockedUserId)
            if (success) {
                Log.d("blockedUserViewModel", "blockedUser deleted!")
                storeBlockedUserInRoom(userId)
            } else {
                Log.d("blockedUserViewModel", "blockedUser failed to be deleted!")
            }
        }
    }

    suspend fun getBlockedUsersFromFirebase(blockedUserId: String): List<String> {
        return firebaseRepository.getBlockedUsersFromFirebase(blockedUserId)
    }


}