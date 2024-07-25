package com.example.shots.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream

interface SentShotRemoteDataSource {

    fun getYourUserId(): String

    suspend fun writeSentShotToFirebase(
        userId: String,
        sentShotData: MutableMap<String, Uri>,
        context: Context
    ): Boolean

    suspend fun uploadSentShotToStorage(
        userId: String,
        sentShotItems: MutableMap<String, Uri> = mutableMapOf(),
        context: Context
    ): Map<String, Uri?>

    suspend fun getSentShotsFromFirebase(): List<String>

    suspend fun removeSentShotFromFirebase(userId: String?): Boolean

}