package com.example.shots.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class IfSeenReceivedShotRemoteDataSourceImpl @Inject
constructor(
    val firebaseAuth: FirebaseAuth,
    val firebaseFirestore: FirebaseFirestore,
    val firebaseStorage: FirebaseStorage
) : IfSeenReceivedShotRemoteDataSource {

    override fun getYourUserId(): String {
        return firebaseAuth.currentUser?.displayName ?: ""
    }

    override suspend fun writeIfSeenReceivedShotToFirebase(
        userId: String,
        ifSeenReceivedShotData: MutableMap<String, Boolean>
    ): Boolean {
        Log.d("IfSeenReceivedShotRemoteDataSourceImpl", "userId = $userId")
        return try {
            val userRef = firebaseFirestore.collection("users").document(userId)
            val receivedShotsRef = userRef.collection("receivedShots")
                .document(userId) // User's receivedShots document
            val ifSeenReceivedShotsRef = receivedShotsRef.collection("ifSeenReceivedShots")
                .document(userId)
                .set(ifSeenReceivedShotData, SetOptions.merge())
                .await()
            Log.d("IfSeenReceivedShotRemoteDataSource", "ifSeenReceivedShot added successfully")
            true
        } catch (e: Exception) {
            Log.d(
                "IfSeenReceivedShotRemoteDataSource",
                "Error adding ifSeenReceivedShot: ${e.message}"
            )
            // Handle any potential errors here
            false
        }
    }


    override suspend fun getIfSeenReceivedShotsFromFirebase(): List<String> {
        val ifSeenReceivedShotsList = mutableListOf<String>()

        try {
            val receivedShotRef = firebaseFirestore
                .collection("users")
                .document(getYourUserId())
                .collection("receivedShots")
                .document(getYourUserId())
                .collection("ifSeenReceivedShots")

            val collection = receivedShotRef.get().await()

            for (document in collection.documents) {
                for ((key, value) in document.data.orEmpty()) {
                    if (key.startsWith("ifSeenReceivedShot-")) {
                        val fieldUserId = key.removePrefix("ifSeenReceivedShot-")
                        Log.d(
                            "IfSeenReceivedShotRemoteDataSource",
                            "Field key: $key, Field value: $value"
                        )
                        ifSeenReceivedShotsList.add("$fieldUserId-$value")
                    }
                }
            }

            return ifSeenReceivedShotsList

        } catch (e: Exception) {
            Log.d(
                "IfSeenReceivedShotRemoteDataSource",
                "Something went wrong fetching receivedShots: ${e.message}"
            )
        }

        return emptyList()
    }


    override suspend fun removeIfSeenReceivedShotFromFirebase(userId: String): Boolean {
        if (userId == null) {
            Log.d("IfSeenReceivedShotRemoteDataSource", "Invalid userId: null")
            return false // Error
        }

        return try {
            val bookmarkRef = firebaseFirestore
                .collection("users")
                .document(userId)
                .collection("receivedShots")
                .document(userId)
                .collection("ifSeenReceivedShots")
                .document(userId)

            val updates = hashMapOf<String, Any>(
                "ifSeenReceivedShot-${getYourUserId()}" to FieldValue.delete()
            )

            bookmarkRef.update(updates).addOnSuccessListener {
                Log.d(
                    "IfSeenReceivedShotRemoteDataSource",
                    "Field 'ifSeenReceivedShot-${getYourUserId()}' deleted successfully"
                )
            }.addOnFailureListener {
                Log.d(
                    "IfSeenReceivedShotRemoteDataSource",
                    "Error deleting field 'ifSeenReceivedShot-${getYourUserId()}': ${it.message}"
                )
            }.await() // Await the completion of the update operation

            true
        } catch (e: Exception) {
            Log.d(
                "IfSeenReceivedShotRemoteDataSource",
                "Error deleting field 'ifSeenReceivedShot-${getYourUserId()}': ${e.message}"
            )
            false
        }
    }


}