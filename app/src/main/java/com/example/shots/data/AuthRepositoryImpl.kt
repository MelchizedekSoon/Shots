package com.example.shots.data

import android.content.ContentValues
import android.util.Log
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    val authRemoteDataSource:
    AuthRemoteDataSource
) : AuthRepository {

    override suspend fun createUserWithEmailAndPassword(
        email: String,
        password: String,
    ): FirebaseUser? {
        return try {
            val authResult = authRemoteDataSource.createUserWithEmailAndPassword(email, password)
            if (authResult != null) {
                Log.d(ContentValues.TAG, "User creation successful: ${authResult.user}")
                authResult.user
            } else {
                Log.e(ContentValues.TAG, "User creation failed: Result is null")
                null
            }
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error creating user", e)
            null
        }
    }
    override suspend fun signInWithEmailPassword(email: String, password: String): AuthResult? {
        return authRemoteDataSource.signInWithEmailAndPassword(email, password)
    }

}