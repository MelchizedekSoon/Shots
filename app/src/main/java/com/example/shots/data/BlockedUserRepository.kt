package com.example.shots.data

import kotlinx.coroutines.flow.Flow

interface BlockedUserRepository {

    fun getYourUserId(): String
    suspend fun saveAndStoreBlockedUser(
        blockedUserId: String,
        blockedUserData: MutableMap<String, Any>
    )

    suspend fun storeBlockedUser(userId: String)
    suspend fun storeBlockedUserObject(blockedUser: BlockedUser)
    fun getBlockedUserObject(): BlockedUser
    suspend fun getBlockedUsers(): Flow<List<String>>
    suspend fun fetchUpdatedBlockedUsers(): Flow<List<String>>
    suspend fun deleteBlockedUser(userId: String)

}