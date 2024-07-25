package com.example.shots.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class BookmarkRemoteDataSourceImpl @Inject
constructor(
    val firebaseAuth: FirebaseAuth,
    val firebaseFirestore: FirebaseFirestore
) : BookmarkRemoteDataSource {

    override fun getYourUserId(): String {
        return firebaseAuth.currentUser?.displayName ?: ""
    }

    override suspend fun writeBookmarkToFirebase(
        userId: String,
        bookmarkData: MutableMap<String, Any>,
    ): Boolean {
        return try {
            val userRef = firebaseFirestore.collection("users").document(getYourUserId())
            val bookmarkRef = userRef.collection("bookmarks").document(getYourUserId())
                .set(bookmarkData, SetOptions.merge()).await()
            Log.d("BookmarkRemoteDataSource", "bookmarks added successfully")
            true
        } catch (e: Exception) {
            Log.d("BookmarkRemoteDataSource", "Error adding bookmarks: ${e.message}")
            // Handle any potential errors here
            false
        }
    }


    override suspend fun getBookmarksFromFirebase(): List<String> {
        val bookmarkList = mutableListOf<String>()

        Log.d(
            "BookmarkRemoteDataSource",
            "firebaseAuth.currentUser.displayName = ${firebaseAuth.currentUser?.displayName}"
        )
        Log.d(
            "BookmarkRemoteDataSource",
            "In bookmarkRemoteDataSource your user id is = ${getYourUserId()}"
        )

        try {
            val bookmarkRef = firebaseFirestore
                .collection("users")
                .document(getYourUserId())
                .collection("bookmarks")

            val collection = bookmarkRef.get().result

            for (document in collection.documents) {
                for (field in document.data?.keys.orEmpty()) {
                    Log.d("BookmarkRemoteDataSource", "field = $field")
                    if (field.startsWith("bookmark-")) {
//                        val bookmarkValue = document.getString(field)?.removePrefix("bookmark-")
                        val bookmarkValue = field.removePrefix("bookmark-")
                        Log.d("BookmarkRemoteDataSource", "bookmarkValue = $bookmarkValue")
                        bookmarkValue.let {
                            bookmarkList.add(it)
                        }
                    }
                }
            }
            Log.d("BookmarkRemoteDataSource", "Finished bookmarkList - $bookmarkList")
            return bookmarkList
        } catch (e: Exception) {
            Log.d(
                "BookmarkRemoteDataSource",
                "Something went wrong fetching bookmarks: ${e.message}"
            )
        }

        return emptyList()
    }


    override suspend fun deleteBookmarkFromFirebase(userId: String?): Boolean {
        if (userId == null) {
            Log.d("BookmarkRemoteDataSource", "Invalid userId: null")
            return false // Error
        }

        return try {
            val bookmarkRef = firebaseFirestore
                .collection("users")
                .document(getYourUserId())
                .collection("bookmarks")
                .document(getYourUserId())

            val updates = hashMapOf<String, Any>(
                "bookmark-$userId" to FieldValue.delete()
            )

            bookmarkRef.update(updates).addOnSuccessListener {
                Log.d("BookmarkRemoteDataSource", "Field 'bookmark-$userId' deleted successfully")
            }.addOnFailureListener {
                Log.d(
                    "BookmarkRemoteDataSource",
                    "Error deleting field 'bookmark-$userId': ${it.message}"
                )
            }.await() // Await the completion of the update operation

            true
        } catch (e: Exception) {
            Log.d(
                "BookmarkRemoteDataSource",
                "Error deleting field 'bookmark-$userId': ${e.message}"
            )
            false
        }
    }

}