package com.example.shots.ui.theme

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shots.RoomModule
import com.example.shots.ViewModelModule
import com.example.shots.data.AppDatabase
import com.example.shots.data.BlockedUser
import com.example.shots.data.FirebaseRepository
import com.example.shots.data.UserWhoBlockedYou
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BlockViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val firebaseAuth: FirebaseAuth,
    private val appDatabase: AppDatabase
) : ViewModel() {

    val userId = firebaseAuth.currentUser?.displayName ?: ""


    private val usersViewModel =
        ViewModelModule.provideUsersViewModel(firebaseRepository, firebaseAuth, appDatabase)
    private val receivedLikeViewModel =
        ViewModelModule.provideReceivedLikeViewModel(firebaseRepository, firebaseAuth, appDatabase)
    private val sentLikeViewModel =
        ViewModelModule.provideSentLikeViewModel(firebaseRepository, firebaseAuth, appDatabase)
    private val blockedUserViewModel =
        ViewModelModule.provideBlockedUserViewModel(firebaseRepository, firebaseAuth, appDatabase)
    private val userWhoBlockedYouViewModel =
        ViewModelModule.provideUserWhoBlockedYouViewModel(
            firebaseRepository,
            firebaseAuth,
            appDatabase
        )
    private val userDao = RoomModule.provideUserDao(appDatabase)
    private val blockedUserDao = RoomModule.provideBlockedUserDao(appDatabase)
    private val userWhoBlockedYouDao = RoomModule.provideUserWhoBlockedYouDao(appDatabase)
    private val receivedLikeDao = RoomModule.provideReceiveLikeDao(appDatabase)
    private val sentLikeDao = RoomModule.provideSentLikeDao(appDatabase)

    fun block(userId: String?) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val blockedUserData: MutableMap<String, Any> = mutableMapOf()
                val mediaItems: MutableMap<String, Uri> = mutableMapOf()
                blockedUserData["blockedUser-${userId}"] = userId ?: ""
                val yourUserId = firebaseAuth.currentUser?.displayName ?: ""
                val success = firebaseRepository.writeBlockedUserToFirebase(userId ?: "", blockedUserData)
                if (success) {
                    Log.d("blockedUserViewModel", "blockedUser saved!")
                    storeBlockedInRoom(yourUserId ?: "")
                } else {
                    Log.d("blockedUserViewModel", "blockedUser failed to be saved!")
                }
            }
        }
    }

    fun storeBlockedInRoom(userId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val blockedUsersList = firebaseRepository.getBlockedUsersFromFirebase(userId)
                val usersWhoBlockedYouList =
                    firebaseRepository.getUsersWhoBlockedYouFromFirebase(userId)
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
                    Log.d("BlockViewModel", "blockedUser was stored!")
                    try {
                        blockedUserDao.insert(blockedUser)
                    } catch (e: Exception) {
                        Log.d("BlockViewModel", "blockedUser failed to be stored!")
                    }
                }

                try {
                    var userWhoBlockedYou = userWhoBlockedYouDao.findById(userId)
                    userWhoBlockedYou = userWhoBlockedYou.copy(
                        userWhoBlockedYouId = userId,
                        usersWhoBlockedYou = blockedUsersList.toMutableList()
                    )
                    userWhoBlockedYouDao.insert(userWhoBlockedYou)
                } catch (npe: java.lang.NullPointerException) {
                    val userWhoBlockedYou =
                        UserWhoBlockedYou(userId, usersWhoBlockedYouList.toMutableList())
                    Log.d("BlockViewModel", "userWhoBlockedYou was stored!")
                    try {
                        userWhoBlockedYouDao.insert(userWhoBlockedYou)
                    } catch (e: Exception) {
                        Log.d("BlockViewModel", "userWhoBlockedYou failed to be stored!")
                    }
                }
            }
        }
    }

    fun unblock(blockedUserId: String?) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val blockedUserData: MutableMap<String, Any> = mutableMapOf()
                val mediaItems: MutableMap<String, Uri> = mutableMapOf()

                val userId = firebaseAuth.currentUser?.displayName ?: ""
                val success = firebaseRepository.deleteBlockedUserFromFirebase(blockedUserId)
                if (success) {
                    Log.d("blockedUserViewModel", "blockedUser deleted!")
                    storeBlockedInRoom(userId)
                } else {
                    Log.d("blockedUserViewModel", "blockedUser failed to be deleted!")
                }

            }
        }
    }
}