package com.example.shots.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface SentShotRepository {

    fun getYourUserId(): String

    suspend fun saveSentShot(
        sentShotId: String,
        sentShotData: MutableMap<String, Uri>,
        context: Context
    )

    suspend fun storeSentShot(sentShot: SentShot)

    fun getSentShot(userId: String): SentShot

    suspend fun removeSentShot(userId: String)

    suspend fun getUpdatedSentShots(): Flow<List<String>>

    suspend fun fetchUpdatedSentShots(): Flow<List<String>>

}