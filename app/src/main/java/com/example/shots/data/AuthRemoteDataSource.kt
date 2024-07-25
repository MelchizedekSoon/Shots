package com.example.shots.data

import android.content.ContentValues
import android.util.Log
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRemoteDataSource @Inject
constructor(
    val firebaseAuth: FirebaseAuth,
    val firebaseFirestore: FirebaseFirestore
) {
    suspend fun signInWithEmailAndPassword(email: String, password: String) : AuthResult? {
        return firebaseAuth.signInWithEmailAndPassword(email, password).await()
    }
    suspend fun createUserWithEmailAndPassword(email: String, password: String): AuthResult? {
        return firebaseAuth.createUserWithEmailAndPassword(email, password).await()
    }

}