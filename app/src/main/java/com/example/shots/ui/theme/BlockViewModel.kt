package com.example.shots.ui.theme

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shots.data.BlockedUserRepository
import com.example.shots.data.FirebaseRepository
import com.example.shots.data.UserWhoBlockedYouRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BlockViewModel @Inject constructor(
    firebaseRepository: FirebaseRepository,
    private val blockedUserRepository: BlockedUserRepository,
    private val userWhoBlockedYouRepository: UserWhoBlockedYouRepository,
) : ViewModel() {


    fun block(userId: String?) {
        viewModelScope.launch(Dispatchers.IO) {

            val blockedUserData: MutableMap<String, Any> = mutableMapOf()
            blockedUserData["blockedUser-${userId}"] = userId ?: ""
            blockedUserRepository.saveAndStoreBlockedUser(userId ?: "", blockedUserData)

            val userWhoBlockedYouData: MutableMap<String, Any> = mutableMapOf()
            userWhoBlockedYouData["userWhoBlockedYou-${userWhoBlockedYouRepository.getYourUserId()}"] =
                userWhoBlockedYouRepository.getYourUserId()
            userWhoBlockedYouRepository.saveAndStoreUserWhoBlockedYou(
                userId ?: "",
                userWhoBlockedYouData
            )

        }
    }

//    fun storeBlockedInRoom(userId: String) {
//        viewModelScope.launch {
//            withContext(Dispatchers.IO) {
//                val blockedUsersList = firebaseRepository.getBlockedUsersFromFirebase(userId)
//                val usersWhoBlockedYouList =
//                    firebaseRepository.getUsersWhoBlockedYouFromFirebase(userId)
//                Log.d("blockedUserViewModel", "blockedUsersList: $blockedUsersList")
//                try {
//                    var blockedUser = blockedUserDao.findById(userId)
//                    blockedUser = blockedUser.copy(
//                        blockedUserId = userId,
//                        blockedUsers = blockedUsersList.toMutableList()
//                    )
//                    blockedUserDao.upsert(blockedUser)
//                } catch (npe: java.lang.NullPointerException) {
//                    val blockedUser = BlockedUser(userId, blockedUsersList.toMutableList())
//                    Log.d("BlockViewModel", "blockedUser was stored!")
//                    try {
//                        blockedUserDao.upsert(blockedUser)
//                    } catch (e: Exception) {
//                        Log.d("BlockViewModel", "blockedUser failed to be stored!")
//                    }
//                }
//
//                try {
//                    var userWhoBlockedYou = userWhoBlockedYouDao.findById(userId)
//                    userWhoBlockedYou = userWhoBlockedYou.copy(
//                        userWhoBlockedYouId = userId,
//                        usersWhoBlockedYou = blockedUsersList.toMutableList()
//                    )
//                    userWhoBlockedYouDao.upsert(userWhoBlockedYou)
//                } catch (npe: java.lang.NullPointerException) {
//                    val userWhoBlockedYou =
//                        UserWhoBlockedYou(userId, usersWhoBlockedYouList.toMutableList())
//                    Log.d("BlockViewModel", "userWhoBlockedYou was stored!")
//                    try {
//                        userWhoBlockedYouDao.upsert(userWhoBlockedYou)
//                    } catch (e: Exception) {
//                        Log.d("BlockViewModel", "userWhoBlockedYou failed to be stored!")
//                    }
//                }
//            }
//        }
//    }

    fun unblock(blockedUserId: String?) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val blockedUserData: MutableMap<String, Any> = mutableMapOf()
                val mediaItems: MutableMap<String, Uri> = mutableMapOf()

                blockedUserRepository.deleteBlockedUser(blockedUserId ?: "")

                userWhoBlockedYouRepository.deleteUserWhoBlockedYou(blockedUserId ?: "")

//                val success = firebaseRepository.deleteBlockedUserFromFirebase(blockedUserId)
//                if (success) {
//                    Log.d("blockedUserViewModel", "blockedUser deleted!")
//                    storeBlockedInRoom(userId)
//                } else {
//                    Log.d("blockedUserViewModel", "blockedUser failed to be deleted!")
//                }

            }
        }
    }
}