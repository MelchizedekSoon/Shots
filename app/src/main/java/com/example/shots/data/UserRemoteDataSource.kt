package com.example.shots.data

import android.content.Context
import android.net.Uri


interface UserRemoteDataSource {

    fun getYourUserId(): String

    suspend fun getUsers(): List<User>

    suspend fun getUserData(userId: String): User?

    suspend fun deleteMediaFromFirebase(userId: String, mediaIdentifier: String)

    suspend fun writeUserDataToFirebase(
        userId: String, userData: MutableMap<String, Any>,
        mediaItems: MutableMap<String, Uri>,
        context: Context
    ): Boolean


}