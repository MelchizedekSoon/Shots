package com.example.shots.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface UserWhoBlockedYouRepository {

    fun getYourUserId(): String

    suspend fun saveAndStoreUserWhoBlockedYou(
        userId: String,
        userData: MutableMap<String, Any>
    )

    suspend fun storeUserWhoBlockedYou(userWhoBlockedYou: UserWhoBlockedYou)

    fun getUserWhoBlockedYou(userId: String): UserWhoBlockedYou

    suspend fun getUsersWhoBlockedYou(): Flow<List<String>>

    suspend fun fetchUpdatedUsersWhoBlockedYou(): Flow<List<String>>

    suspend fun deleteUserWhoBlockedYou(userId: String)


}