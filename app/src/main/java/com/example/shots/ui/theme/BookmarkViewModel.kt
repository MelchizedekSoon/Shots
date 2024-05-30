package com.example.shots.ui.theme

import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shots.RoomModule
import com.example.shots.ViewModelModule
import com.example.shots.data.AppDatabase
import com.example.shots.data.Bookmark
import com.example.shots.data.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BookmarkViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val firebaseAuth: FirebaseAuth,
    private val appDatabase: AppDatabase,
) : ViewModel() {

    private val usersViewModel =
        ViewModelModule.provideUsersViewModel(firebaseRepository, firebaseAuth, appDatabase)
    private val userDao = RoomModule.provideUserDao(appDatabase)
    val bookmarkDao = RoomModule.provideBookmarkDao(appDatabase)


//    suspend fun fetchListOfBookmarksFromRepo(bookmarkId: String) {
//        firebaseRepository.fetchListOfBookmarks(bookmarkId)
//    }

//    fun fetchListOfBookmarksFromRoomDB(bookmarkId: String): String {
//        return bookmarkDao.findById(bookmarkId).bookmarks
//    }

//    fun saveBookmarksToRoomDB(bookmarks: String) : String {
//        bookmarkDao.insert(bookmarks)
//    }

    suspend fun fetchBookmarkFromRoom(bookmarkId: String): Bookmark {
        return withContext(Dispatchers.IO) {
            try {
                val bookmark = bookmarkDao.findById(bookmarkId)
                if (bookmark != null) {
                    Log.d("bookmarkViewModel", "fetch returns $bookmark")
                    bookmark
                } else {
                    // Handle case when bookmark is null in the database
                    val emptyBookmark = Bookmark(bookmarkId, mutableListOf())
                    emptyBookmark
                }
            } catch (npe: NullPointerException) {
                try {
                    val bookmarkList = getBookmarksFromFirebase(bookmarkId)
                    if (bookmarkList.isNotEmpty()) {
                        Bookmark(bookmarkId, bookmarkList.toMutableList())
                    } else {
                        // Handle case when no data is available from Firebase
                        // For example, you can return the existing bookmark object without modification
                        val bookmark = Bookmark(bookmarkId, mutableListOf())
                        bookmark
                    }
                } catch (npe: NullPointerException) {
                    val bookmark = Bookmark(bookmarkId, mutableListOf())
                    bookmark
                }
            }
        }
    }

    fun storeBookmarkInRoom(userId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val bookmarkList = getBookmarksFromFirebase(userId)
                Log.d("BookmarkViewModel", "bookmarkList is $bookmarkList")
                try {
                    val bookmark = Bookmark(userId, bookmarkList.toMutableList())
                    Log.d("BookmarkViewModel", "bookmark is $bookmark")
                    bookmarkDao.update(bookmark)
                } catch (npe: NullPointerException) {
                    val bookmark = Bookmark(userId, bookmarkList.toMutableList())
                    bookmarkDao.insert(bookmark)
                }
            }
        }
    }

    fun saveUserDataToFirebase(
        userId: String,
        userData: MutableMap<String, Any>,
        mediaItems: MutableMap<String, Uri>,
        context: Context
    ) {
        viewModelScope.launch {
            val success =
                firebaseRepository.writeUserDataToFirebase(userId, userData, mediaItems, context)
            if (success) {
                Log.d(
                    "BookmarkViewModel",
                    "User data successfully added at the time the userData includes $userData" +
                            "and the mediaItems include $mediaItems"
                )
//                 Handle successful data save
            } else {
                Log.d("BookmarkViewModel", "User data not added")
//                 Handle data save error
            }
        }
    }

    suspend fun saveBookmarkToFirebase(
        bookmarkId: String,
        bookmarkData: MutableMap<String, Any>,
        context: Context
    ): Boolean {
        val success = firebaseRepository.writeBookmarksToFirebase(bookmarkId, bookmarkData)
        if (success) {
            val yourUserId = firebaseAuth.currentUser?.displayName ?: ""
            val receivingUser = usersViewModel.fetchUserFromRoom(bookmarkId ?: "")

            val userData: MutableMap<String, Any> = mutableMapOf()
            val mediaItems: MutableMap<String, Uri> = mutableMapOf()

            receivingUser.timesBookmarkedCount = receivingUser.timesBookmarkedCount?.plus(1)

            userData["timesBookmarkedCount"] = receivingUser.timesBookmarkedCount ?: 0

            usersViewModel.saveUserDataToFirebase(bookmarkId, userData, mediaItems, context) {
                usersViewModel.storeUserInRoom(yourUserId)
            }

            Log.d(TAG, "Bookmark added!")
        } else {
            Log.d(TAG, "Bookmark failed to be added!")
        }
        return success
    }

    suspend fun removeBookmarkFromFirebase(
        bookmarkId: String,
        context: Context
    ): Boolean {
        val success = firebaseRepository.deleteBookmarkFromFirebase(bookmarkId)
        if (success) {
            val yourUserId = firebaseAuth.currentUser?.displayName ?: ""
            val receivingUser = usersViewModel.fetchUserFromRoom(bookmarkId ?: "")

            val userData: MutableMap<String, Any> = mutableMapOf()
            val mediaItems: MutableMap<String, Uri> = mutableMapOf()

            receivingUser.timesBookmarkedCount = receivingUser.timesBookmarkedCount?.minus(1)

            if (receivingUser.timesBookmarkedCount!! < 0) {
                receivingUser.timesBookmarkedCount = 0
            }

            userData["timesBookmarkedCount"] = receivingUser.timesBookmarkedCount ?: 0

            usersViewModel.saveUserDataToFirebase(bookmarkId, userData, mediaItems, context) {
                usersViewModel.storeUserInRoom(yourUserId)
            }

            Log.d(TAG, "Bookmark deleted!")
        } else {
            Log.d(TAG, "Bookmark failed to be deleted!")
        }
        return success
    }

    suspend fun getBookmarksFromFirebase(bookmarkId: String): List<String> {
        Log.d(TAG, "Going through BookmarkViewModel to get the Bookmark!")
        return firebaseRepository.getBookmarksFromFirebase(bookmarkId)
    }

    fun convertBookmarksToList(bookmarks: String): List<String> {
        return bookmarks.split(",")
    }
}