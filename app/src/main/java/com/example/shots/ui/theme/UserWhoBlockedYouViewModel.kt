
package com.example.shots.ui.theme

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shots.RoomModule
import com.example.shots.data.AppDatabase
import com.example.shots.data.FirebaseRepository
import com.example.shots.data.UserWhoBlockedYou
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class UserWhoBlockedYouViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val firebaseAuth: FirebaseAuth,
    private val appDatabase: AppDatabase
) : ViewModel() {

    val userWhoBlockedYouDao = RoomModule.provideUserWhoBlockedYouDao(appDatabase)

    fun fetchUserWhoBlockedYouFromRoom(userWhoBlockedYouId: String): UserWhoBlockedYou {
        return try {
            val userWhoBlockedYou = userWhoBlockedYouDao.findById(userWhoBlockedYouId)
            if (userWhoBlockedYou != null) {
                userWhoBlockedYou
            } else {
                UserWhoBlockedYou(userWhoBlockedYouId, mutableListOf())
            }
        } catch (npe: NullPointerException) {
            UserWhoBlockedYou(userWhoBlockedYouId, mutableListOf())
        }
    }

    fun storeUserWhoBlockedYouInRoom(userId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val yourUserId = firebaseAuth.currentUser?.displayName ?: ""
                val usersWhoBlockedYouList = getUserWhoBlockedYouFromFirebase(userId)
                Log.d("UserWhoBlockedYouViewModel", "usersWhoBlockedYouList: $usersWhoBlockedYouList")
                try {
                    var userWhoBlockedYou = userWhoBlockedYouDao.findById(yourUserId)
                    userWhoBlockedYou = userWhoBlockedYou.copy(
                        userWhoBlockedYouId = yourUserId,
                        usersWhoBlockedYou = usersWhoBlockedYouList.toMutableList()
                    )
                    userWhoBlockedYouDao.insert(userWhoBlockedYou)
                } catch (npe: java.lang.NullPointerException) {
                    val userWhoBlockedYou = UserWhoBlockedYou(userId, usersWhoBlockedYouList.toMutableList())
                    Log.d("blockedUserViewModel", "blockedUser was stored!")
                    try {
                        userWhoBlockedYouDao.insert(userWhoBlockedYou)
                    } catch(e: Exception) {
                        Log.d("blockedUserViewModel", "blockedUser failed to be stored!")
                    }
                }
            }
        }
    }

//    suspend fun saveUserWhoBlockedYouToFirebase(
//        blockedUserId: String,
//        blockedUserData: MutableMap<String, Any>
//    ) {
//        viewModelScope.launch {
//            val userId = firebaseAuth.currentUser?.displayName ?: ""
//            val success = firebaseRepository.writeuserWhoBlockedYouToFirebase(blockedUserId, blockedUserData)
//            if (success) {
//                Log.d("blockedUserViewModel", "blockedUser saved!")
//
//                storeuserWhoBlockedYouInRoom(userId)
//            } else {
//                Log.d("blockedUserViewModel", "blockedUser failed to be saved!")
//            }
//        }
//    }
//
//    suspend fun removeUserWhoBlockedYouFromFirebase(
//        blockedUserId: String
//    ) {
//        viewModelScope.launch {
//            val userId = firebaseAuth.currentUser?.displayName ?: ""
//            val success = firebaseRepository.deleteUserWhoBlockedYouFromFirebase(blockedUserId)
//            if (success) {
//                Log.d("blockedUserViewModel", "blockedUser deleted!")
//                storeuserWhoBlockedYouInRoom(userId)
//            } else {
//                Log.d("blockedUserViewModel", "blockedUser failed to be deleted!")
//            }
//        }
//    }

    suspend fun getUserWhoBlockedYouFromFirebase(blockedUserId: String): List<String> {
        return firebaseRepository.getUsersWhoBlockedYouFromFirebase(blockedUserId)
    }


}