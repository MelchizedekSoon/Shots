package com.example.shots.data

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userLocalDataSource: UserLocalDataSource,
    private val userRemoteDataSource: UserRemoteDataSource
) : UserRepository {

    override fun getYourUserId(): String {
        return userRemoteDataSource.getYourUserId()
    }

    override suspend fun saveUserData(
        userId: String,
        userData: MutableMap<String, Any>,
        mediaItems: MutableMap<String, Uri>,
        context: Context
    ): Boolean {
        val success =
            userRemoteDataSource.writeUserDataToFirebase(userId, userData, mediaItems, context)

        if (success) {
            Log.d(
                "UsersViewModel",
                "User data successfully added at the time the userData includes $userData" +
                        "and the mediaItems include $mediaItems"
            )
            return storeAfterSavingUserDataToFirebase(userId)
        } else {
            Log.d("UsersViewModel", "User data not added")
            return false
        }

    }

    override suspend fun upsertUser(user: User) {
        userLocalDataSource.upsert(user)
    }

    override suspend fun upsertUsers(users: List<User>) {
        userLocalDataSource.upsertAll(users)
    }

     suspend fun storeAfterSavingUserDataToFirebase(userId: String): Boolean {

        val retrievedUser = userRemoteDataSource.getUserData(userId)

        Log.d(
            "UserRepositoryImpl",
            "Value of returnedUser before adding to ROOM DB - $retrievedUser"
        )

        if (retrievedUser != null) {
            try {
                userLocalDataSource.upsert(retrievedUser)
                val returnedUser = userLocalDataSource.findById(retrievedUser.id)
                Log.d(ContentValues.TAG, "Updated user in Room DB: $returnedUser")
                return true
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error updating user in Room DB: ${e.message}", e)
                try {
                    userLocalDataSource.upsert(retrievedUser)
                    Log.d(ContentValues.TAG, "Upserted user in Room DB: $retrievedUser")
                    return true
                } catch (e: Exception) {
                    Log.e(
                        ContentValues.TAG,
                        "Error upserting user in Room DB: ${e.message}",
                        e
                    )
                    return false
                }
            }
        }
        return false
    }


    override fun getAllUsers(): Flow<List<User>> {
        val users = userLocalDataSource.getAll()
        return flow { emit(users) }
    }

    //add onto fetchupdatedusers, include  storeupdatedUsers for all


    override suspend fun fetchUpdatedUsers(): Flow<List<User>> {
        // Move the database operation to a background thread
        withContext(Dispatchers.IO) {
            userLocalDataSource.upsertAll(userRemoteDataSource.getUsers())
        }

        return flow { emit(userLocalDataSource.getAll()) }
    }

    override suspend fun fetchUpdatedCurrentUser(): Flow<User> {
        val retrievedUser = userRemoteDataSource.getUserData(getYourUserId())
        if (retrievedUser != null)
            userLocalDataSource.upsert(retrievedUser)
        return flow {
            if (retrievedUser != null) {
                emit(retrievedUser)
            }
        }
    }

    override suspend fun getCurrentUser(): Flow<User> {
        return flow { emit(userLocalDataSource.findByYourId()) }
    }

    override fun getUser(userId: String): User {
        return userLocalDataSource.findById(userId)
    }

    override fun storeUser(user: User) {
        userLocalDataSource.upsert(user)
    }

    override fun storeUsers(users: List<User>) {
        for (user in users)
            userLocalDataSource.upsert(user)
    }

    override suspend fun updateUsers() {
        userLocalDataSource.upsertAll(userRemoteDataSource.getUsers())
    }

}