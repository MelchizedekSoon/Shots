package com.example.shots.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FakeBookmarkRemoteDataSourceImpl @Inject
constructor(
    val firebaseAuth: FirebaseAuth,
    val firebaseFirestore: FirebaseFirestore
) : BookmarkRemoteDataSource {

    override fun getYourUserId(): String {
        return "John"
    }

    override suspend fun writeBookmarkToFirebase(
        userId: String,
        bookmarkData: MutableMap<String, Any>,
    ): Boolean {
        return try {
            Log.d("FakeBookmarkRemoteDataSourceImpl", "inside FakeBookmarkRemoteDataSourceImpl with " +
                    "getYourUserId() == ${getYourUserId()} and firebaseFirestore = $firebaseFirestore and " +
                    "bookmarkData = ${bookmarkData}")
            val userRef = firebaseFirestore.collection("users").document(getYourUserId())
            val bookmarkRef = userRef.collection("bookmarks").document(getYourUserId())
                .set(bookmarkData, SetOptions.merge()).await()
            Log.d("FakeBookmarkRemoteDataSourceImpl", "bookmarks added successfully")
            true
        } catch (e: Exception) {
            Log.d("FakeBookmarkRemoteDataSourceImpl", "Error adding bookmarks: ${e.message}")
            // Handle any potential errors here
            false
        }
    }


    override suspend fun getBookmarksFromFirebase(): List<String> {

        val bookmarkList = mutableListOf<String>()

        Log.d("FakeBookmarkRemoteDataSourceImpl",
            "firebaseAuth.currentUser.displayName = ${firebaseAuth.currentUser?.displayName}")
        Log.d("FakeBookmarkRemoteDataSourceImpl", "In bookmarkRemoteDataSource your user id is = ${getYourUserId()}")

        try {
            Log.d("FakeBookmarkRemoteDataSourceImpl", "Right before bookmarkRef")

            val bookmarkRef = firebaseFirestore
                .collection("users")
                .document(getYourUserId())
                .collection("bookmarks")

            val collection = bookmarkRef.get().await()

            Log.d("FakeBookmarkRemoteDataSourceImpl", "Right after bookmarkRef")

            for (document in collection.documents) {
                for (field in document.data?.keys.orEmpty()) {
                    Log.d("FakeBookmarkRemoteDataSourceImpl", "field = $field")
                    if (field.startsWith("bookmark-")) {
//                        val bookmarkValue = document.getString(field)?.removePrefix("bookmark-")
                        val bookmarkValue = field.removePrefix("bookmark-")
                        Log.d("FakeBookmarkRemoteDataSourceImpl", "bookmarkValue = $bookmarkValue")
                        bookmarkValue.let {
                            bookmarkList.add(it)
                        }
                    }
                }
            }
            Log.d("FakeBookmarkRemoteDataSourceImpl", "Finished bookmarkList - $bookmarkList")
            return bookmarkList
        } catch (e: Exception) {
            Log.d("FakeBookmarkRemoteDataSourceImpl", "Something went wrong fetching bookmarks: ${e.message}")
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
            Log.d("BookmarkRemoteDataSource", "Error deleting field 'bookmark-$userId': ${e.message}")
            false
        }
    }

}