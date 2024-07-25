package com.example.shots.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    fun getYourUserId(): String

    //this methods saves to firebase and stores in room
    suspend fun saveUserData(
        userId: String,
        userData: MutableMap<String, Any>,
        mediaItems: MutableMap<String, Uri>,
        context: Context
    ): Boolean

    suspend fun upsertUser(user: User)

    suspend fun upsertUsers(users: List<User>)

    fun getAllUsers(): Flow<List<User>>

    suspend fun fetchUpdatedUsers(): Flow<List<User>>

    suspend fun getCurrentUser(): Flow<User>

    suspend fun fetchUpdatedCurrentUser(): Flow<User>

    suspend fun updateUsers()

    fun getUser(userId: String): User

    fun storeUser(user: User)

    fun storeUsers(users: List<User>)

}