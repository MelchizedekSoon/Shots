package com.example.shots.ui.theme

import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shots.data.Bookmark
import com.example.shots.data.BookmarkRepository
import com.example.shots.data.FirebaseRepository
import com.example.shots.data.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


data class BookmarkUiState(
    val bookmarks: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)


//sealed interface UsersUiState {
//    object Loading : UsersUiState
//    data class Success(val users: List<User>) : UsersUiState
//
////    data class Error(val errorMessage: ErrorMessage) : UsersUiState
//
//    data class Error(val errorMessage: String) : UsersUiState
//
//}

@HiltViewModel
class BookmarkViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val userRepository: UserRepository,
    private val dispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookmarkUiState())
    val uiState: StateFlow<BookmarkUiState> = _uiState.asStateFlow()

    init {
        try {
            loadBookmarks()
        } catch (npe: NullPointerException) {
            Log.d("BookmarkViewModel", "bookmarks: $npe")
        }
    }

//    private val userDao = RoomModule.provideUserDao(appDatabase)
//    val bookmarkDao = RoomModule.provideBookmarkDao(appDatabase)


//    suspend fun fetchListOfBookmarksFromRepo(bookmarkId: String) {
//        firebaseRepository.fetchListOfBookmarks(bookmarkId)
//    }

//    fun fetchListOfBookmarksFromRoomDB(bookmarkId: String): String {
//        return bookmarkDao.findById(bookmarkId).bookmarks
//    }

//    fun saveBookmarksToRoomDB(bookmarks: String) : String {
//        bookmarkDao.insert(bookmarks)
//    }


    fun loadBookmarks() {
        viewModelScope.launch(dispatcher) {
            try {
                bookmarkRepository.fetchUpdatedBookmarks().collect { returnedBookmarks ->
                    Log.d(
                        "BookmarkViewModel",
                        "inside loadBookmarks where the returnedBookmarks = $returnedBookmarks"
                    )
                    _uiState.value = BookmarkUiState().copy(bookmarks = returnedBookmarks)
                }
            } catch (e: Exception) {
                Log.d("BookmarkViewModel", "Error: ${e.message}")
                _uiState.value =
                    BookmarkUiState().copy(errorMessage = e.message ?: "Unknown error")
            }
        }
    }

    fun fetchBookmarkObject(): Bookmark {
        return bookmarkRepository.getBookmark(bookmarkRepository.getYourUserId())
    }

    fun fetchBookmarks(): Flow<List<String>> {
        return bookmarkRepository.fetchBookmarks()
    }

    fun fetchUpdatedBookmarks(): Flow<List<String>> {
        return bookmarkRepository.fetchUpdatedBookmarks()
    }


//    suspend fun fetchBookmarkFromRoom(bookmarkId: String): Bookmark {
//        return withContext(dispatcher) {
//            try {
//                val bookmark = bookmarkDao.findById(bookmarkId)
//                if (bookmark != null) {
//                    Log.d("bookmarkViewModel", "fetch returns $bookmark")
//                    bookmark
//                } else {
//                    // Handle case when bookmark is null in the database
//                    val emptyBookmark = Bookmark(bookmarkId, mutableListOf())
//                    emptyBookmark
//                }
//            } catch (npe: NullPointerException) {
//                try {
//                    val bookmarkList = getBookmarksFromFirebase(bookmarkId)
//                    if (bookmarkList.isNotEmpty()) {
//                        Bookmark(bookmarkId, bookmarkList.toMutableList())
//                    } else {
//                        // Handle case when no data is available from Firebase
//                        // For example, you can return the existing bookmark object without modification
//                        val bookmark = Bookmark(bookmarkId, mutableListOf())
//                        bookmark
//                    }
//                } catch (npe: NullPointerException) {
//                    val bookmark = Bookmark(bookmarkId, mutableListOf())
//                    bookmark
//                }
//            }
//        }
//    }


    fun storeBookmark(userId: String) {
        viewModelScope.launch {
            bookmarkRepository.storeBookmark(userId)
        }
    }

    fun storeBookmarkObject(bookmark: Bookmark) {
        viewModelScope.launch {
            bookmarkRepository.storeBookmarkObject(bookmark)
        }
    }

    fun storeBookmarkInRoom(bookmark: Bookmark) {
        viewModelScope.launch(dispatcher) {
            bookmarkRepository.storeBookmarkObject(bookmark)
        }
    }

    fun saveUserDataToFirebase(
        userId: String,
        userData: MutableMap<String, Any>,
        mediaItems: MutableMap<String, Uri>,
        context: Context
    ) {
        viewModelScope.launch(dispatcher) {
            val success =
                firebaseRepository.writeUserDataToFirebase(
                    userId,
                    userData,
                    mediaItems,
                    context
                )
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
            val receivingUser = userRepository.getUser(bookmarkId)

            val userData: MutableMap<String, Any> = mutableMapOf()
            val mediaItems: MutableMap<String, Uri> = mutableMapOf()

            receivingUser.timesBookmarkedCount = receivingUser.timesBookmarkedCount?.plus(1)

            userData["timesBookmarkedCount"] = receivingUser.timesBookmarkedCount ?: 0

//            userViewModel.saveUserDataToFirebase(bookmarkId, userData, mediaItems, context) {
//                userViewModel.storeUserInRoom(yourUserId)
//            }

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
            val receivingUser = userRepository.getUser(bookmarkId)

            val userData: MutableMap<String, Any> = mutableMapOf()
            val mediaItems: MutableMap<String, Uri> = mutableMapOf()

            receivingUser.timesBookmarkedCount = receivingUser.timesBookmarkedCount?.minus(1)

            if (receivingUser.timesBookmarkedCount!! < 0) {
                receivingUser.timesBookmarkedCount = 0
            }

            userData["timesBookmarkedCount"] = receivingUser.timesBookmarkedCount ?: 0

//            userViewModel.saveUserDataToFirebase(bookmarkId, userData, mediaItems, context) {
//                userViewModel.storeUserInRoom(yourUserId)
//            }

            Log.d(TAG, "Bookmark deleted!")
        } else {
            Log.d(TAG, "Bookmark failed to be deleted!")
        }
        return success
    }

    fun getBookmarks(): Flow<List<String>> {
        return bookmarkRepository.getBookmarks()
    }

    suspend fun getBookmarksFromFirebase(bookmarkId: String): List<String> {
        Log.d(TAG, "Going through BookmarkViewModel to get the Bookmark!")
        return firebaseRepository.getBookmarksFromFirebase(bookmarkId)
    }

    fun convertBookmarksToList(bookmarks: String): List<String> {
        return bookmarks.split(",")
    }

    fun saveAndStoreBookmark(
        bookmarkId: String,
        bookmarkData: MutableMap<String, Any>
    ) {
        viewModelScope.launch(dispatcher) {
            Log.d("BookmarkViewModel", "inside save and store where bookmarkId is $bookmarkId")
            Log.d(
                "BookmarkViewModel",
                "inside save and store where bookmarkData is $bookmarkData"
            )
            bookmarkRepository.saveAndStoreBookmark(bookmarkId, bookmarkData)
            loadBookmarks()
        }
    }

    fun removeBookmark(bookmarkId: String) {
        viewModelScope.launch(dispatcher) {
            bookmarkRepository.removeBookmark(bookmarkId)
            loadBookmarks()
        }
    }

}