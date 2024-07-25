package com.example.shots.data

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    suspend fun createUserWithEmailAndPassword(
        email: String,
        password: String,
    ): FirebaseUser?

    suspend fun signInWithEmailPassword(email: String, password: String): AuthResult?
}