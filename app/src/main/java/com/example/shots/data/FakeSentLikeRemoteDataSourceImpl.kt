package com.example.shots.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FakeSentLikeRemoteDataSourceImpl @Inject constructor(
    val firebaseAuth: FirebaseAuth,
    val firebaseFirestore: FirebaseFirestore,
    val sentLikeDao: SentLikeDao
) : SentLikeRemoteDataSource {

    override fun getYourUserId(): String {
        return "John"
    }

    override suspend fun writeSentLikeToFirebase(
        userId: String,
        sentLikeData: MutableMap<String, Any>
    ): Boolean {
        return try {
            val userRef = firebaseFirestore.collection("users").document(getYourUserId())
            val sentLikeRef = userRef.collection("sentLikes").document(getYourUserId())
                .set(sentLikeData, SetOptions.merge()).await()
            Log.d("SentLikeRemoteDataSource", "sentLikes added successfully")
            true
        } catch (e: Exception) {
            Log.d("SentLikeRemoteDataSource", "Error adding sentLikes: ${e.message}")
            // Handle any potential errors here
            false
        }
    }


    override suspend fun getSentLikesFromFirebase(userId: String): List<String> {
        if (userId.isBlank()) {
            return emptyList()
        }
        val sentLikeList = mutableListOf<String>()

        try {
            val sentLikeRef = firebaseFirestore
                .collection("users")
                .document(getYourUserId())
                .collection("sentLikes")

            val collection = sentLikeRef.get().await()

            for (document in collection.documents) {
                for (field in document.data?.keys.orEmpty()) {
                    Log.d("SentLikeRemoteDataSource", "field = $field")
                    if (field.startsWith("sentLike-")) {
//                        val sentLikeValue = document.getString(field)?.removePrefix("sentLike-")
                        val sentLikeValue = field.removePrefix("sentLike-")
                        Log.d("SentLikeRemoteDataSource", "sentLikeValue = $sentLikeValue")
                        sentLikeValue.let {
                            sentLikeList.add(it)
                        }
                    }
                }
            }
            Log.d("SentLikeRemoteDataSource", "Finished sentLikeList - $sentLikeList")
            return sentLikeList
        } catch (e: Exception) {
            Log.d("SentLikeRemoteDataSource", "Something went wrong fetching sentLikes: ${e.message}")
        }

        return emptyList()
    }


    override suspend fun removeSentLikeFromFirebase(userId: String?): Boolean {
        if (userId == null) {
            Log.d("SentLikeRemoteDataSource", "Invalid userId: null")
            return false // Error
        }

        return try {
            val sentLikeRef = firebaseFirestore
                .collection("users")
                .document(getYourUserId())
                .collection("sentLikes")
                .document(getYourUserId())

            val updates = hashMapOf<String, Any>(
                "sentLike-$userId" to FieldValue.delete()
            )

            sentLikeRef.update(updates).addOnSuccessListener {
                Log.d("SentLikeRemoteDataSource", "Field 'sentLike-$userId' deleted successfully")
            }.addOnFailureListener {
                Log.d(
                    "SentLikeRemoteDataSource",
                    "Error deleting field 'sentLike-$userId': ${it.message}"
                )
            }.await() // Await the completion of the update operation

            true
        } catch (e: Exception) {
            Log.d("SentLikeRemoteDataSource", "Error deleting field 'sentLike-$userId': ${e.message}")
            false
        }
    }

}