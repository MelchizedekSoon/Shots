package com.example.shots.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FakeBlockedUserRemoteDataSourceImpl @Inject
constructor(
    val firebaseAuth: FirebaseAuth,
    val firebaseFirestore: FirebaseFirestore
) : BlockedUserRemoteDataSource {

    override fun getYourUserId(): String {
        return "John"
    }

    override suspend fun writeBlockedUserToFirebase(
        userId: String,
        blockedUserData: MutableMap<String, Any>,
    ): Boolean {
        return try {
            val userRef = firebaseFirestore.collection("users").document(getYourUserId())
            val blockedUserRef = userRef.collection("blockedUsers").document(getYourUserId())
                .set(blockedUserData, SetOptions.merge()).await()
            Log.d("BlockedUserRemoteDataSource", "blockedUsers added successfully")
            true
        } catch (e: Exception) {
            Log.d("BlockedUserRepositoryImpl", "Error adding blockedUsers: ${e.message}")
            // Handle any potential errors here
            false
        }
    }


    override suspend fun getBlockedUsersFromFirebase(): List<String> {

        val blockedUserList = mutableListOf<String>()

        Log.d(
            "BlockedUserRemoteDataSource",
            "In blockedUserRemoteDataSource your user id is = ${getYourUserId()}"
        )

        try {
            val blockedUserRef = firebaseFirestore
                .collection("users")
                .document(getYourUserId())
                .collection("blockedUsers")

            val collection = blockedUserRef.get().await()

            for (document in collection.documents) {
                for (field in document.data?.keys.orEmpty()) {
                    Log.d("BlockedUserRepositoryImpl", "field = $field")
                    if (field.startsWith("blockedUser-")) {
//                        val blockedUserValue = document.getString(field)?.removePrefix("blockedUser-")
                        val blockedUserValue = field.removePrefix("blockedUser-")
                        Log.d("BlockedUserRepositoryImpl", "blockedUserValue = $blockedUserValue")
                        blockedUserValue.let {
                            blockedUserList.add(it)
                        }
                    }
                }
            }
            Log.d("BlockedUserRepositoryImpl", "Finished blockedUserList - $blockedUserList")
            return blockedUserList
        } catch (e: Exception) {
            Log.d(
                "BlockedUserRepositoryImpl",
                "Something went wrong fetching blockedUsers: ${e.message}"
            )
        }

        return emptyList()
    }


    override suspend fun deleteBlockedUserFromFirebase(userId: String?): Boolean {
        if (userId == null) {
            Log.d("BlockedUserRepositoryImpl", "Invalid userId: null")
            return false // Error
        }

        return try {
            val blockedUserRef = firebaseFirestore
                .collection("users")
                .document(getYourUserId())
                .collection("blockedUsers")
                .document(getYourUserId())

            val updates = hashMapOf<String, Any>(
                "blockedUser-$userId" to FieldValue.delete()
            )

            blockedUserRef.update(updates).addOnSuccessListener {
                Log.d(
                    "BlockedUserRepositoryImpl",
                    "Field 'blockedUser-$userId' deleted successfully"
                )
            }.addOnFailureListener {
                Log.d(
                    "BlockedUserRepositoryImpl",
                    "Error deleting field 'blockedUser-$userId': ${it.message}"
                )
            }.await() // Await the completion of the update operation

            true
        } catch (e: Exception) {
            Log.d(
                "BlockedUserRepositoryImpl",
                "Error deleting field 'blockedUser-$userId': ${e.message}"
            )
            false
        }
    }

}