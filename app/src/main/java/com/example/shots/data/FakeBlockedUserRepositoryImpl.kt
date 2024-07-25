package com.example.shots.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FakeBlockedUserRepositoryImpl @Inject
constructor(
    private val blockedUserLocalDataSource: BlockedUserLocalDataSource,
    private val blockedUserRemoteDataSource: BlockedUserRemoteDataSource
) : BlockedUserRepository {

    override fun getYourUserId(): String {
        return "John"
    }

    override suspend fun saveAndStoreBlockedUser(
        blockedUserId: String,
        blockedUserData: MutableMap<String, Any>
    ) {
        val success =
            blockedUserRemoteDataSource.writeBlockedUserToFirebase(
                blockedUserId,
                blockedUserData
            )
        if (success) {
            val blockedUserList =
                blockedUserRemoteDataSource.getBlockedUsersFromFirebase()
                    .toMutableList()
            try {
//                var blockedUser = blockedUserLocalDataSource.findById(getYourUserId())
//                blockedUser = blockedUser.copy(
//                    blockedUserId = getYourUserId(),
//                    blockedUsers = blockedUserList
//                )
                blockedUserLocalDataSource.upsert(BlockedUser(getYourUserId(), blockedUserList))
            } catch (npe: java.lang.NullPointerException) {
                val blockedUser =
                    BlockedUser(getYourUserId(), blockedUserList.toMutableList())
                Log.d("BlockedUserRepositoryImpl", "blockedUser was stored!")
                try {
                    blockedUserLocalDataSource.upsert(blockedUser)
                } catch (e: Exception) {
                    Log.d("BlockedUserRepositoryImpl", "blockedUser failed to be stored!")
                }
            }
        }
    }

    override suspend fun storeBlockedUser(userId: String) {
        var blockedUser = blockedUserLocalDataSource.findById(getYourUserId())
        if(blockedUser == null) {
            blockedUser = BlockedUser(getYourUserId(), mutableListOf())
        }
        val blockedUsers = blockedUser.blockedUsers
        blockedUsers.add(userId)
        blockedUserLocalDataSource.upsert(blockedUser)
    }

    override suspend fun storeBlockedUserObject(blockedUser: BlockedUser) {
        blockedUserLocalDataSource.upsert(blockedUser)
    }

    override fun getBlockedUserObject(): BlockedUser {
        return blockedUserLocalDataSource.findById(getYourUserId())
    }

    override suspend fun getBlockedUsers(): Flow<List<String>> {
        var blockedUser = blockedUserLocalDataSource.findById(getYourUserId())
        Log.d("FakeBlockedUserRepositoryImpl", "blockedUser: $blockedUser")
        if(blockedUser == null) {
            blockedUser = BlockedUser(getYourUserId(), blockedUserRemoteDataSource.getBlockedUsersFromFirebase().toMutableList())
        }
        blockedUserLocalDataSource.upsert(blockedUser)
        val blockedUsersList =
            blockedUserLocalDataSource.findById(getYourUserId()).blockedUsers.toMutableList()
        return flow { emit(blockedUsersList) }
    }

    override suspend fun fetchUpdatedBlockedUsers(): Flow<List<String>> {
        val blockedUsersList =
            blockedUserRemoteDataSource.getBlockedUsersFromFirebase()
        return flow { emit(blockedUsersList) }
    }

    override suspend fun deleteBlockedUser(userId: String) {
        try {
            blockedUserRemoteDataSource.deleteBlockedUserFromFirebase(userId)
            val blockedUsers = blockedUserRemoteDataSource.getBlockedUsersFromFirebase()
            val blockedUser = BlockedUser(getYourUserId(), blockedUsers.toMutableList())
            blockedUserLocalDataSource.upsert(blockedUser)
        } catch (e: Exception) {
            Log.d("BlockedUserRepositoryImpl", "blockedUser failed to be deleted! May not exist.")
        }
    }

}