package com.example.shots.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UserWhoBlockedYouRepositoryImpl
@Inject constructor(
    private val userWhoBlockedYouLocalDataSource: UserWhoBlockedYouLocalDataSource,
    private val userWhoBlockedYouRemoteDataSource: UserWhoBlockedYouRemoteDataSource
) : UserWhoBlockedYouRepository {

    override fun getYourUserId(): String {
        return userWhoBlockedYouRemoteDataSource.getYourUserId()
    }

    override suspend fun saveAndStoreUserWhoBlockedYou(
        userId: String,
        userData: MutableMap<String, Any>
    ) {
        val success =
            userWhoBlockedYouRemoteDataSource.writeUserWhoBlockedYouToFirebase(userId, userData)
        if (success) {
            val usersWhoBlockedYouList =
                userWhoBlockedYouRemoteDataSource.getUsersWhoBlockedYouFromFirebase()
                    .toMutableList()
            try {
                var userWhoBlockedYou = userWhoBlockedYouLocalDataSource.findById(getYourUserId())
                userWhoBlockedYou = userWhoBlockedYou.copy(
                    userWhoBlockedYouId = getYourUserId(),
                    usersWhoBlockedYou = usersWhoBlockedYouList
                )
                userWhoBlockedYouLocalDataSource.upsert(userWhoBlockedYou)
            } catch (npe: java.lang.NullPointerException) {
                val userWhoBlockedYou =
                    UserWhoBlockedYou(getYourUserId(), usersWhoBlockedYouList.toMutableList())
                Log.d("blockedUserViewModel", "blockedUser was stored!")
                try {
                    userWhoBlockedYouLocalDataSource.upsert(userWhoBlockedYou)
                } catch (e: Exception) {
                    Log.d("blockedUserViewModel", "blockedUser failed to be stored!")
                }
            }
        }
    }

    override suspend fun storeUserWhoBlockedYou(userWhoBlockedYou: UserWhoBlockedYou) {
        userWhoBlockedYouLocalDataSource.upsert(userWhoBlockedYou)
    }

    override fun getUserWhoBlockedYou(userId: String): UserWhoBlockedYou {
        return userWhoBlockedYouLocalDataSource.findById(getYourUserId())
    }

    override suspend fun getUsersWhoBlockedYou(): Flow<List<String>> {
        val usersWhoBlockedYouList =
            userWhoBlockedYouLocalDataSource.findById(getYourUserId()).usersWhoBlockedYou.toMutableList()
        return flow { emit(usersWhoBlockedYouList) }
    }

    override suspend fun fetchUpdatedUsersWhoBlockedYou(): Flow<List<String>> {
        val usersWhoBlockedYouList =
            userWhoBlockedYouRemoteDataSource.getUsersWhoBlockedYouFromFirebase()
        return flow { emit(usersWhoBlockedYouList) }
    }

    override suspend fun deleteUserWhoBlockedYou(userId: String) {
        try {
            userWhoBlockedYouRemoteDataSource.deleteUserWhoBlockedYouFromFirebase(userId)
            val usersWhoBlockedYou =
                userWhoBlockedYouRemoteDataSource.getUsersWhoBlockedYouFromFirebase()
            val userWhoBlockedYou =
                UserWhoBlockedYou(getYourUserId(), usersWhoBlockedYou.toMutableList())
            userWhoBlockedYouLocalDataSource.upsert(userWhoBlockedYou)
        } catch (e: Exception) {
            Log.d(
                "UserWhoBlockedYouRepositoryImpl",
                "userWhoBlockedYou failed to be deleted! May not exist."
            )
        }
    }


}