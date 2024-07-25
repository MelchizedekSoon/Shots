package com.example.shots.data

import android.content.Context
import android.net.Uri

interface ReceivedShotRemoteDataSource {

    fun getYourUserId(): String

    suspend fun writeReceivedShotToFirebase(
        userId: String,
        receivedShotData: MutableMap<String, Uri>,
        context: Context
    ): Boolean

    suspend fun uploadReceivedShotToStorage(
        userId: String,
        receivedShotItems: MutableMap<String, Uri> = mutableMapOf(),
        context: Context
    ): Map<String, Uri?>

    suspend fun getReceivedShotsFromFirebase(): List<String>

    suspend fun getIfSeenReceivedShotsFromFirebase(): List<String>

    suspend fun removeReceivedShotFromFirebase(userId: String?): Boolean

    suspend fun removeTheirReceivedShotFromFirebase(userId: String?): Boolean

}