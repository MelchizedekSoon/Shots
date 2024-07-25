package com.example.shots.data

interface IfSeenReceivedShotRemoteDataSource {

    fun getYourUserId(): String

    suspend fun writeIfSeenReceivedShotToFirebase(
        userId: String,
        ifSeenReceivedShotData: MutableMap<String, Boolean>
    ): Boolean


    suspend fun getIfSeenReceivedShotsFromFirebase(): List<String>


    suspend fun removeIfSeenReceivedShotFromFirebase(userId: String): Boolean
}