package com.example.shots.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.AuthResult
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await


// "FirebaseRepository".kt
interface FirebaseRepository {

    fun getYourUserId(): String

    fun logOut()

    suspend fun signInWithEmailPassword(email: String, password: String): AuthResult?

    suspend fun createUserWithEmailAndPassword(email: String, password: String): AuthResult?

    suspend fun getMetadataFromStorage(mediaIdentifier: String): String

    suspend fun getUsers(): List<User>


//    suspend fun fetchListOfBookmarks(userId: String): String? {
//        val docRef = firebaseFirestore.collection("users").document(userId)
//        try {
//            val document = docRef.get().await()
////            Log.d("FirebaseRepository", "Here is the value for document - $document")
//            if (document.exists()) {
//                val userData = document.data
//                if (userData != null) {
//                    val bookmarksValue = userData["bookmarks"] as? String
//                    val bookmarks = bookmarksValue
//                    if (bookmarksValue == null) {
//                        Log.e("FirebaseRepository", "Invalid bookmarks value: $bookmarksValue")
//                    }
//                    return bookmarks
//                }
//            }
//        } catch (e: Exception) {
//            Log.d("FirebaseRepository", "Bookmarks cannot be retrieved. ${e.message}")
//        }
//        return null
//    }

    suspend fun getUserData(userId: String): User?

    suspend fun fetchMediaUrls(userId: String): MutableMap<String, String>

//    suspend fun writeBookmarksToFirebase(
//        bookmarkId: String,
//        userData: MutableMap<String, Any>
//    ): Boolean {
//        try {
//            firebaseFirestore.collection("users").document(bookmarkId)
//                .set(userData, SetOptions.merge()).await()
//        } catch (e: Exception) {
//            Log.d("FirebaseRepository", "writing bookmarks to Firebase failed! - ${e.message}")
//            return false
//        }
//        return true
//    }

    //the methods for bookmark below

    suspend fun writeBookmarksToFirebase(
        userId: String,
        bookmarkData: MutableMap<String, Any>
    ): Boolean


    suspend fun getBookmarksFromFirebase(userId: String): List<String>


    suspend fun deleteBookmarkFromFirebase(userId: String?): Boolean


    //the methods for sentLike below

    suspend fun writeSentLikeToFirebase(
        userId: String,
        sentLikeData: MutableMap<String, Any>
    ): Boolean

    suspend fun getSentLikesFromFirebase(userId: String): List<String>

    suspend fun deleteSentLikeFromFirebase(userId: String?): Boolean

    //the methods for receivedLike below

    suspend fun writeReceivedLikeToFirebase(
        userId: String,
        receivedLikeData: MutableMap<String, Any>
    ): Boolean

    suspend fun getReceivedLikesFromFirebase(userId: String): List<String>

    suspend fun deleteReceivedLikeFromFirebase(userId: String?): Boolean

    //the methods for sentShot below

    suspend fun writeSentShotToFirebase(
        userId: String,
        sentShotData: MutableMap<String, Uri>,
        context: Context
    ): Boolean

    suspend fun deleteSentShotFromFirebase(userId: String): Boolean

    suspend fun uploadSentShotToStorage(
        userId: String,
        sentShotItems: MutableMap<String, Uri> = mutableMapOf(),
        context: Context
    ): Map<String, Uri?>


    suspend fun getSentShotsFromFirebase(userId: String): List<String>

    //the methods for receivedShot below

    suspend fun writeReceivedShotToFirebase(
        userId: String,
        receivedShotData: MutableMap<String, Uri>,
        context: Context
    ): Boolean

    suspend fun uploadReceivedShotToStorage(
        userId: String,
        receivedShotItems: MutableMap<String, Uri> = mutableMapOf(),
        context: Context
    ): Map<String, Uri?>

    suspend fun getReceivedShotsFromFirebase(userId: String): List<String>

    suspend fun deleteReceivedShotFromFirebase(userId: String): Boolean

    // write blockedUser to Firebase

    suspend fun writeBlockedUserToFirebase(
        userId: String,
        blockedUserData: MutableMap<String, Any>
    ): Boolean

    // write userWhoBlockedYou to Firebase

    suspend fun writeUserWhoBlockedYouToFirebase(
        userId: String,
        userWhoBlockedYouData: MutableMap<String, Any>
    ): Boolean

    // get blockedUsers from Firestore

    suspend fun getBlockedUsersFromFirebase(userId: String): List<String>

    // get usersWhoBlockedYou from Firestore

    suspend fun getUsersWhoBlockedYouFromFirebase(userId: String): List<String>

    // delete userWhoBlockedYou from firebase

    suspend fun deleteUserWhoBlockedYouFromFirebase(userId: String?): Boolean

    // delete blocked user from firebase

    suspend fun deleteBlockedUserFromFirebase(userId: String?): Boolean

    suspend fun writeUserDataToFirebase(
        userId: String, userData: MutableMap<String, Any>,
        mediaItems: MutableMap<String, Uri>,
        context: Context
    ): Boolean

    fun getTypeFromUri(context: Context, uri: Uri): String?

    suspend fun uploadMediaItemsToStorage(
        userId: String,
        mediaItems: MutableMap<String, Uri> = mutableMapOf(),
        userData: MutableMap<String, Any> = mutableMapOf(),
        context: Context
    ): Map<String, Uri?>

    suspend fun uploadImageToStorage(imageUri: Uri, mediaIdentifier: String): Boolean

    suspend fun deleteMediaFromFirebase(userId: String, mediaIdentifier: String)

    fun getImageFromStorage(imageUri: Uri, mediaIdentifier: String): Boolean

    fun uploadVideoToStorage(videoUri: Uri, mediaIdentifier: String): Boolean

    fun uploadProfileVideoToStorage(videoUri: Uri): Boolean

// Other Firebase-related methods...

}

