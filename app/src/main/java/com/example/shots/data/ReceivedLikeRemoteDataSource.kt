package com.example.shots.data

interface ReceivedLikeRemoteDataSource {

    fun getYourUserId(): String

    suspend fun writeReceivedLikeToFirebase(
        userId: String,
        receivedLikeData: MutableMap<String, Any>,
    ): Boolean

    suspend fun getReceivedLikesFromFirebase(): List<String>

    suspend fun removeReceivedLikeFromFirebase(userId: String?): Boolean

}