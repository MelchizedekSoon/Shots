package com.example.shots.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface UserWhoBlockedYouRemoteDataSource {

    fun getYourUserId(): String

    suspend fun getUsersWhoBlockedYouFromFirebase(): List<String>

    suspend fun writeUserWhoBlockedYouToFirebase(
        userId: String,
        userWhoBlockedYouData: MutableMap<String, Any>
    ): Boolean

    suspend fun deleteUserWhoBlockedYouFromFirebase(userId: String?): Boolean

}