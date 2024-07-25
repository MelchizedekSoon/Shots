package com.example.shots.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface ReceivedShotRepository {

    fun getYourUserId(): String

    suspend fun saveReceivedShot(
        receivedShotId: String,
        receivedShotData: MutableMap<String, Uri>,
        context: Context
    )

    suspend fun storeReceivedShot(receivedShot: ReceivedShot)

    fun getReceivedShot(userId: String): ReceivedShot

    suspend fun removeReceivedShot(userId: String): Boolean

    suspend fun removeTheirReceivedShotFromFirebase(userId: String?): Boolean

    suspend fun getUpdatedReceivedShots(): Flow<List<String>>

    suspend fun fetchUpdatedReceivedShots(): Flow<List<String>>
}