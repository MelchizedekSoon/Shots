package com.example.shots.data

import kotlinx.coroutines.flow.Flow

interface IfSeenReceivedShotRepository {

    fun getYourUserId(): String

    suspend fun saveIfSeenReceivedShot(
        ifSeenReceivedShotId: String,
        ifSeenReceivedShotData: MutableMap<String, Boolean>
    )

    suspend fun removeIfSeenReceivedShot(userId: String)

    suspend fun storeIfSeenReceivedShot(ifSeenReceivedShot: IfSeenReceivedShot)

    fun getIfSeenReceivedShot(userId: String): IfSeenReceivedShot

    suspend fun getUpdatedIfSeenReceivedShots(): Flow<List<String>>

    suspend fun fetchUpdatedIfSeenReceivedShots(): Flow<List<String>>
}