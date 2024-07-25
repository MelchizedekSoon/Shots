package com.example.shots.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ReceivedLikeRemoteDataSourceImpl @Inject
constructor(
    val firebaseAuth: FirebaseAuth,
    val firebaseFirestore: FirebaseFirestore
) : ReceivedLikeRemoteDataSource {

    override fun getYourUserId(): String {
        return firebaseAuth.currentUser?.displayName ?: ""
    }

    override suspend fun writeReceivedLikeToFirebase(
        userId: String,
        receivedLikeData: MutableMap<String, Any>,
    ): Boolean {
        return try {
            val userRef = firebaseFirestore.collection("users").document(userId)
            val receivedLikeRef = userRef.collection("receivedLikes").document(userId)
                .set(receivedLikeData, SetOptions.merge()).await()
            Log.d("ReceivedLikeRemoteDataSource", "receivedLikes added successfully")
            true
        } catch (e: Exception) {
            Log.d("ReceivedLikeRemoteDataSource", "Error adding receivedLikes: ${e.message}")
            // Handle any potential errors here
            false
        }
    }


    override suspend fun getReceivedLikesFromFirebase(): List<String> {

        val receivedLikeList = mutableListOf<String>()

        Log.d(
            "ReceivedLikeRemoteDataSource",
            "In receivedLikeRemoteDataSource your user id is = ${
                getYourUserId()
            }"
        )
        Log.d(
            "ReceivedLikeRemoteDataSource",
            "In receivedLikeRemoteDataSource your user id is = ${getYourUserId()}"
        )
        try {
            val receivedLikeRef = firebaseFirestore
                .collection("users")
                .document(getYourUserId())
                .collection("receivedLikes")

            val collection = receivedLikeRef.get().await()

            for (document in collection.documents) {
                for (field in document.data?.keys.orEmpty()) {
                    Log.d("ReceivedLikeRemoteDataSource", "field = $field")
                    if (field.startsWith("receivedLike-")) {
//                        val receivedLikeValue = document.getString(field)?.removePrefix("receivedLike-")
                        val receivedLikeValue = field.removePrefix("receivedLike-")
                        Log.d(
                            "ReceivedLikeRemoteDataSource",
                            "receivedLikeValue = $receivedLikeValue"
                        )
                        receivedLikeValue.let {
                            receivedLikeList.add(it)
                        }
                    }
                }
            }
            Log.d("ReceivedLikeRemoteDataSource", "Finished receivedLikeList - $receivedLikeList")
            return receivedLikeList
        } catch (e: Exception) {
            Log.d(
                "ReceivedLikeRemoteDataSource",
                "Something went wrong fetching receivedLikes: ${e.message}"
            )
        }

        return emptyList()
    }


    override suspend fun removeReceivedLikeFromFirebase(userId: String?): Boolean {
        if (userId == null) {
            Log.d("ReceivedLikeRemoteDataSource", "Invalid userId: null")
            return false // Error
        }

        return try {
            val receivedLikeRef = firebaseFirestore
                .collection("users")
                .document(userId)
                .collection("receivedLikes")
                .document(userId)

            val updates = hashMapOf<String, Any>(
                "receivedLike-${getYourUserId()}" to FieldValue.delete()
            )

            receivedLikeRef.update(updates).addOnSuccessListener {
                Log.d(
                    "ReceivedLikeRemoteDataSource",
                    "Field 'receivedLike-$userId' deleted successfully"
                )
            }.addOnFailureListener {
                Log.d(
                    "ReceivedLikeRemoteDataSource",
                    "Error deleting field 'receivedLike-$userId': ${it.message}"
                )
            }.await() // Await the completion of the update operation

            true
        } catch (e: Exception) {
            Log.d(
                "ReceivedLikeRemoteDataSource",
                "Error deleting field 'receivedLike-$userId': ${e.message}"
            )
            false
        }
    }

}