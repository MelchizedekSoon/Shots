package com.example.shots.data

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FakeUserRepositoryImpl @Inject constructor(
    private val userLocalDataSource: UserLocalDataSource,
    private val userRemoteDataSource: UserRemoteDataSource
) : UserRepository {

    override fun getYourUserId(): String {
        return "John"
    }

    override suspend fun saveUserData(
        userId: String,
        userData: MutableMap<String, Any>,
        mediaItems: MutableMap<String, Uri>,
        context: Context
    ): Boolean {
        Log.d("FakeUserRepositoryImpl", "User data to be added for userId - ${userId}")
        Log.d(
            "FakeUserRepositoryImpl",
            "User data to be added for ageMinToShow - ${userData["ageMinToShow"]}"
        )
        Log.d(
            "FakeUserRepositoryImpl",
            "User data to be added for ageMaxToShow - ${userData["ageMaxToShow"]}"
        )
        Log.d("FakeUserRepositoryImpl", "User data to be added for ShowMe - ${userData["showMe"]}")
        val success =
            userRemoteDataSource.writeUserDataToFirebase(userId, userData, mediaItems, context)

        if (success) {
            Log.d(
                "FakeUserRepositoryImpl",
                "User data successfully added at the time the userData includes $userData" +
                        "and the mediaItems include $mediaItems"
            )
            return storeAfterSavingUserDataToFirebase(userId)
        } else {
            Log.d("FakeUserRepositoryImpl", "User data not added")
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
            "FakeUserRepositoryImpl",
            "Value of returnedUser before adding to ROOM DB - $retrievedUser"
        )

        if (retrievedUser != null) {
            try {
                userLocalDataSource.upsert(retrievedUser)
                val returnedUser = userLocalDataSource.findById(retrievedUser.id)
                Log.d("FakeUserRepositoryImpl", "Updated user in Room DB: $returnedUser")
                return true
            } catch (e: Exception) {
                Log.e("FakeUserRepositoryImpl", "Error updating user in Room DB: ${e.message}", e)
                try {
                    userLocalDataSource.upsert(retrievedUser)
                    Log.d("FakeUserRepositoryImpl", "Upserted user in Room DB: $retrievedUser")
                    return true
                } catch (e: Exception) {
                    Log.e(
                        "FakeUserRepositoryImpl",
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
        return flow { emit(userLocalDataSource.getAll()) }
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
        Log.d("FakeUserRepositoryImpl", "Value of returnedUser: $retrievedUser")
        if (retrievedUser != null) {
            userLocalDataSource.upsert(retrievedUser)
        }
        return flow {
            if (retrievedUser != null) {
                userRemoteDataSource.getUserData(getYourUserId())?.let { emit(it) }
            }
        }
    }

    override suspend fun getCurrentUser(): Flow<User> {
        var user = userLocalDataSource.findByYourId()
        if (user == null) {
            user = userRemoteDataSource.getUserData(getYourUserId()) ?: User()
            Log.d("FakeUserRepositoryImpl", "Value of returnedUser: $user")
            userLocalDataSource.upsert(user)
        }
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