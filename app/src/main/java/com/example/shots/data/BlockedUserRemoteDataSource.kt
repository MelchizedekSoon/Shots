package com.example.shots.data

interface BlockedUserRemoteDataSource {

        fun getYourUserId(): String

        suspend fun writeBlockedUserToFirebase(
            userId: String,
            blockedUserData: MutableMap<String, Any>,
        ): Boolean

        suspend fun getBlockedUsersFromFirebase(): List<String>

        suspend fun deleteBlockedUserFromFirebase(userId: String?): Boolean

}