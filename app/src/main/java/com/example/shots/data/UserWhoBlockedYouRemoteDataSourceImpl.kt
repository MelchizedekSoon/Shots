package com.example.shots.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserWhoBlockedYouRemoteDataSourceImpl @Inject constructor(
    val firebaseAuth: FirebaseAuth,
    val firebaseFirestore: FirebaseFirestore
) : UserWhoBlockedYouRemoteDataSource {

    override fun getYourUserId(): String {
        return firebaseAuth.currentUser?.displayName ?: ""
    }

    override suspend fun getUsersWhoBlockedYouFromFirebase(): List<String> {

        val usersWhoBlockedYouList = mutableListOf<String>()

        try {
            val usersWhoBlockedYouRef = firebaseFirestore
                .collection("users")
                .document(getYourUserId())
                .collection("usersWhoBlockedYou")

            val collection = usersWhoBlockedYouRef.get().await()

            for (document in collection.documents) {
                for (field in document.data?.keys.orEmpty()) {
                    Log.d("FirebaseRepository", "field = $field")
                    if (field.startsWith("userWhoBlockedYou-")) {
//                        val blockedUserValue = document.getString(field)?.removePrefix("blockedUser-")
                        val userWhoBlockedYouValue = field.removePrefix("userWhoBlockedYou-")
                        Log.d(
                            "FirebaseRepository",
                            "userWhoBlockedYouValue = $userWhoBlockedYouValue"
                        )
                        userWhoBlockedYouValue.let {
                            usersWhoBlockedYouList.add(it)
                        }
                    }
                }
            }
            Log.d("FirebaseRepository", "Finished usersWhoBlockedYouList - $usersWhoBlockedYouList")
            return usersWhoBlockedYouList
        } catch (e: Exception) {
            Log.d(
                "FirebaseRepository",
                "Something went wrong fetching usersWhoBlockedYouList: ${e.message}"
            )
        }

        return emptyList()
    }

    override suspend fun writeUserWhoBlockedYouToFirebase(
        userId: String,
        userWhoBlockedYouData: MutableMap<String, Any>
    ): Boolean {
        return try {
            val userRef = firebaseFirestore.collection("users").document(userId)
            val usersWhoBlockedYouRef = userRef.collection("usersWhoBlockedYou").document(userId)
                .set(userWhoBlockedYouData, SetOptions.merge()).await()
            Log.d("FirebaseRepository", "blockedUsers added successfully")

            true
        } catch (e: Exception) {
            Log.d("FirebaseRepository", "Error adding blockedUsers: ${e.message}")
            // Handle any potential errors here
            false
        }
    }

    override suspend fun deleteUserWhoBlockedYouFromFirebase(userId: String?): Boolean {

        if (userId == null) {
            Log.d("FirebaseRepository", "Invalid userId: null")
            return false // Error
        }

        return try {
            val usersWhoBlockedYouRef = firebaseFirestore
                .collection("users")
                .document(userId)
                .collection("usersWhoBlockedYou")
                .document(userId)

            val updates = hashMapOf<String, Any>(
                "userWhoBlockedYou-${getYourUserId()}" to FieldValue.delete()
            )

            usersWhoBlockedYouRef.update(updates).addOnSuccessListener {
                Log.d(
                    "FirebaseRepository",
                    "Field userWhoBlockedYou-$userId deleted successfully"
                )
            }.addOnFailureListener {
                Log.d(
                    "FirebaseRepository",
                    "Error deleting field userWhoBlockedYou-$userId: ${it.message}"
                )
            }.await() // Await the completion of the update operation

            true
        } catch (e: Exception) {
            Log.d(
                "FirebaseRepository",
                "Error deleting field userWhoBlockedYou-$userId: ${e.message}"
            )
            false
        }
    }


}