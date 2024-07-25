package com.example.shots.ui.theme

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.test.platform.app.InstrumentationRegistry
import com.example.shots.FirebaseModule
import com.example.shots.data.AppDatabase
import com.example.shots.data.Bookmark
import com.example.shots.data.BookmarkDao
import com.example.shots.data.BookmarkLocalDataSource
import com.example.shots.data.BookmarkRemoteDataSource
import com.example.shots.data.BookmarkRepository
import com.example.shots.data.FakeBookmarkRemoteDataSourceImpl
import com.example.shots.data.FakeBookmarkRepositoryImpl
import com.example.shots.data.FakeFirebaseRepositoryImpl
import com.example.shots.data.FakeUserLocalDataSourceImpl
import com.example.shots.data.FakeUserRemoteDataSourceImpl
import com.example.shots.data.FakeUserRepositoryImpl
import com.example.shots.data.FirebaseRepository
import com.example.shots.data.UserDao
import com.example.shots.data.UserLocalDataSource
import com.example.shots.data.UserRemoteDataSource
import com.example.shots.data.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BookmarkViewModelTest {

    private lateinit var roomDatabase: RoomDatabase
    private lateinit var context: Context
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var userDao: UserDao
    private lateinit var firebaseRepository: FirebaseRepository
    private lateinit var bookmarkDao: BookmarkDao
    private lateinit var bookmarkLocalDataSource: BookmarkLocalDataSource
    private lateinit var bookmarkRemoteDataSource: BookmarkRemoteDataSource
    private lateinit var bookmarkRepository: BookmarkRepository
    private lateinit var bookmarkViewModel: BookmarkViewModel
    private lateinit var userLocalDataSource: UserLocalDataSource
    private lateinit var userRemoteDataSource: UserRemoteDataSource
    private lateinit var userRepository: UserRepository
    private lateinit var testDispatcher: TestDispatcher

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() = runTest {
        context = InstrumentationRegistry.getInstrumentation().context

        testDispatcher = StandardTestDispatcher()

        Dispatchers.setMain(testDispatcher)

        roomDatabase = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java // Use your actual database class
        ).allowMainThreadQueries().build()

        firebaseAuth = FirebaseModule.provideFirebaseAuth()
        firebaseAuth.useEmulator("10.0.2.2", 9099)

        firebaseFirestore = FirebaseModule.provideFirestore()
        try {
            firebaseFirestore.useEmulator("10.0.2.2", 8080)
        } catch (_: IllegalStateException) {
        }
        val settings = FirebaseFirestoreSettings.Builder()
            .setHost("10.0.2.2:8080") // Use 10.0.2.2 for Android emulators
            .setSslEnabled(false).build()
        firebaseFirestore.firestoreSettings = settings

//        firebaseFirestore = FirebaseModule.provideFirestore()
//        try {
//            firebaseFirestore.useEmulator("192.168.1.102", 8080)
//        } catch (_: IllegalStateException) {
//        }
//        firebaseFirestore.firestoreSettings = firestoreSettings {}

        firebaseStorage = FirebaseModule.provideStorage()
        firebaseStorage.useEmulator("10.0.2.2", 9199)

        userDao = (roomDatabase as AppDatabase).userDao()
        bookmarkDao = (roomDatabase as AppDatabase).bookmarkDao()

        userLocalDataSource = FakeUserLocalDataSourceImpl(firebaseAuth, userDao)
        userRemoteDataSource =
            FakeUserRemoteDataSourceImpl(firebaseAuth, firebaseFirestore, firebaseStorage)

        bookmarkLocalDataSource = BookmarkLocalDataSource(firebaseAuth, bookmarkDao)
        bookmarkRemoteDataSource = FakeBookmarkRemoteDataSourceImpl(firebaseAuth, firebaseFirestore)

        userRepository = FakeUserRepositoryImpl(userLocalDataSource, userRemoteDataSource)

        bookmarkRepository =
            FakeBookmarkRepositoryImpl(bookmarkLocalDataSource, bookmarkRemoteDataSource, testDispatcher)

        firebaseRepository =
            FakeFirebaseRepositoryImpl(firebaseAuth, firebaseFirestore, firebaseStorage)

        // 10.0.2.2 is the special IP address to connect to the 'localhost' of
// the host computer from an Android emulator.

        val userData: MutableMap<String, Any> = mutableMapOf()
        val mediaItems: MutableMap<String, Uri> = mutableMapOf()

        userData["id"] = "John"
        userData["displayName"] = "John"
        userData["userName"] = "John"

        (userRepository as FakeUserRepositoryImpl).saveUserData(
            "John", userData, mediaItems, context
        )

        bookmarkViewModel =
            BookmarkViewModel(
                firebaseRepository, bookmarkRepository, userRepository, testDispatcher
            )

//        val bookmark = Bookmark("John", mutableListOf("Zeko", "Marc"))
//
//        bookmarkLocalDataSource.bookmarkDao.upsert(bookmark)

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun fetchBookmarks() = runTest {

        var bookmarks = mutableListOf<String>()

        val bookmarkData = mutableMapOf<String, Any>()

        bookmarkData["bookmark-Ron"] = "Ron"
        bookmarkData["bookmark-Von"] = "Von"

        bookmarkViewModel.saveAndStoreBookmark("John", bookmarkData)

        advanceUntilIdle()

        bookmarkViewModel.fetchBookmarks().collect {
            bookmarks = it.toMutableList()
            Log.d("BookmarkViewModelTest", "bookmarks inside fetchBookmarks are $bookmarks")
        }

        advanceUntilIdle()

        assertTrue(bookmarks.contains("Ron") && bookmarks.contains("Von"))

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun fetchUpdatedBookmarks() = runTest {

        var bookmarks = mutableListOf<String>()

        val bookmarkData = mutableMapOf<String, Any>()

        bookmarkData["bookmark-Crispus"] = "Crispus"
        bookmarkData["bookmark-Yao"] = "Yao"

        bookmarkViewModel.saveAndStoreBookmark("John", bookmarkData)

        advanceUntilIdle()

        bookmarkViewModel.fetchUpdatedBookmarks().collect {
            bookmarks = it.toMutableList()
        }

        assertTrue(bookmarks.contains("Crispus") && bookmarks.contains("Yao"))

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun loadBookmarks() = runTest {

        Log.d("BookmarkViewModelTest", "all users = ${userDao.getAll()}")

        val bookmarkData = mutableMapOf<String, Any>()

        bookmarkData["bookmark-Arizona"] = "Arizona"

        bookmarkViewModel.saveAndStoreBookmark("John", bookmarkData)

        testDispatcher.scheduler.advanceUntilIdle()

        bookmarkViewModel.fetchBookmarks().collect {
            Log.d("BookmarkViewModelTest", "bookmarks inside loadBookmarks are $it")
        }

        bookmarkViewModel.loadBookmarks()

        testDispatcher.scheduler.advanceUntilIdle()

        val bookmarks = bookmarkViewModel.uiState.value.bookmarks

        Log.d("BookmarkViewModelTest", "bookmarks inside loadBookmarks are ${bookmarkViewModel.uiState.value.bookmarks}")

        assertTrue(bookmarks.contains("Arizona"))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getBookmarks() = runTest {

        var bookmarks = mutableListOf<String>()

        val bookmarkData = mutableMapOf<String, Any>()

        bookmarkData["bookmark-Maison"] = "Maison"
        bookmarkData["bookmark-Margela"] = "Margela"
        bookmarkData["bookmark-Zion"] = "Zion"

        bookmarkViewModel.saveAndStoreBookmark("John", bookmarkData)

        testDispatcher.scheduler.advanceUntilIdle()

        bookmarkViewModel.getBookmarks().collect {
            bookmarks = it.toMutableList()
        }

        assertTrue(bookmarks.contains("Zion"))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun saveAndStoreBookmark() = runTest {
        var bookmarks = mutableListOf<String>()

        val bookmarkData = mutableMapOf<String, Any>()

        bookmarkData["bookmark-Rodericka"] = "Rodericka"
        bookmarkData["bookmark-Kwesiana"] = "Kwesiana"

        bookmarkViewModel.saveAndStoreBookmark("John", bookmarkData)

        bookmarkViewModel.fetchBookmarks().collect {
            bookmarks = it.toMutableList()
        }

        advanceUntilIdle()

        Log.d(
            "BookmarkViewModelTest",
            "bookmarks inside saveAndStoreBookmark in test are ${bookmarkViewModel.uiState.value.bookmarks}"
        )

        assertTrue(bookmarks.contains("Rodericka") && bookmarks.contains("Kwesiana"))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun removeBookmark() = runTest {

        var bookmarks = mutableListOf<String>()

        val bookmarkData = mutableMapOf<String, Any>()

        bookmarkData["bookmark-Rodericka"] = "Rodericka"

        bookmarkViewModel.saveAndStoreBookmark("John", bookmarkData)

        advanceUntilIdle()

        bookmarkViewModel.removeBookmark("Rodericka")

        advanceUntilIdle()

        bookmarkViewModel.getBookmarks().collect {
            bookmarks = it.toMutableList()
        }

        Log.d("BookmarkViewModelTest", "bookmarks inside removeBookmark in test are $bookmarks")

        advanceUntilIdle()

        assertTrue(!bookmarks.contains("Rodericka"))
    }


    @Test
    fun getUiState() = runTest {
        var bookmarks = mutableListOf<String>()

        val bookmarkData = mutableMapOf<String, Any>()

        bookmarkData["bookmark-Rodericka"] = "Rodericka"

        bookmarkViewModel.saveAndStoreBookmark("John", bookmarkData)

        testDispatcher.scheduler.advanceUntilIdle()
        bookmarkViewModel.fetchBookmarks().collect {
            bookmarks = it.toMutableList()
        }
        bookmarkViewModel.loadBookmarks()
        Log.d("BookmarkViewModelTest", "bookmarks = $bookmarks")
        assertTrue(bookmarkViewModel.uiState.value.bookmarks.isNotEmpty())
    }


    @Test
    fun fetchBookmarkObject() {
    }


    @Test
    fun storeBookmark() {
    }

    @Test
    fun storeBookmarkObject() {
    }

    @Test
    fun storeBookmarkInRoom() {
    }

    @Test
    fun saveUserDataToFirebase() {

    }

    @Test
    fun saveBookmarkToFirebase() = runTest {
        var bookmarks = mutableListOf<String>()

        val bookmarkData = mutableMapOf<String, Any>()

        bookmarkData["bookmark-Sandra"] = "Sandra"

        bookmarkViewModel.saveAndStoreBookmark("John", bookmarkData)

        testDispatcher.scheduler.advanceUntilIdle()
        bookmarkViewModel.fetchBookmarks().collect {
            bookmarks = it.toMutableList()
        }
        bookmarkViewModel.loadBookmarks()
        Log.d("BookmarkViewModelTest", "bookmarks = $bookmarks")
        assertTrue(bookmarkViewModel.uiState.value.bookmarks.contains("Sandra"))
    }

    @Test
    fun removeBookmarkFromFirebase() = runTest {
        var bookmarks = mutableListOf<String>()

        val bookmarkData = mutableMapOf<String, Any>()

        bookmarkData["bookmark-Sandra"] = "Sandra"

        bookmarkViewModel.saveAndStoreBookmark("John", bookmarkData)

        testDispatcher.scheduler.advanceUntilIdle()

        bookmarkViewModel.fetchBookmarks().collect {
            bookmarks = it.toMutableList()
        }

        bookmarkViewModel.loadBookmarks()

        Log.d("BookmarkViewModelTest", "bookmarks = $bookmarks")

        assertTrue(bookmarkViewModel.uiState.value.bookmarks.contains("Sandra"))
    }


    @Test
    fun getBookmarksFromFirebase() {
    }

    @Test
    fun convertBookmarksToList() {
    }

}