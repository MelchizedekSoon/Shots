package com.example.shots.data

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.test.platform.app.InstrumentationRegistry
import com.example.shots.FirebaseModule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.*
import org.junit.Before

import org.junit.Test

class BookmarkLocalDataSourceTest {

    private lateinit var context: Context
    private lateinit var roomDatabase: RoomDatabase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var userRepository: UserRepository
    private lateinit var userLocalDataSource: UserLocalDataSource
    private lateinit var userRemoteDataSource: UserRemoteDataSource
    private lateinit var userDao: UserDao
    private lateinit var bookmarkLocalDataSource: BookmarkLocalDataSource
    private lateinit var bookmarkRemoteDataSource: BookmarkRemoteDataSource
    private lateinit var bookmarkRepository: BookmarkRepository
    private lateinit var bookmarkDao: BookmarkDao
    private lateinit var testDispatcher: TestDispatcher

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() = runTest {

        testDispatcher = StandardTestDispatcher()

        Dispatchers.setMain(testDispatcher)

        context = InstrumentationRegistry.getInstrumentation().context

        roomDatabase = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java // Use your actual database class
        ).allowMainThreadQueries().build()

        bookmarkDao = (roomDatabase as AppDatabase).bookmarkDao()
        userDao = (roomDatabase as AppDatabase).userDao()

        firebaseAuth = FirebaseModule.provideFirebaseAuth()
        firebaseAuth.useEmulator("10.0.2.2", 9099)

        firebaseFirestore = FirebaseModule.provideFirestore()
        try {
            firebaseFirestore.useEmulator("10.0.2.2", 8080)
        } catch (_: IllegalStateException) {
        }

        firebaseStorage = FirebaseModule.provideStorage()
        firebaseStorage.useEmulator("10.0.2.2", 9199)

        userLocalDataSource = FakeUserLocalDataSourceImpl(firebaseAuth, userDao)
        userRemoteDataSource =
            FakeUserRemoteDataSourceImpl(firebaseAuth, firebaseFirestore, firebaseStorage)
        userRepository = FakeUserRepositoryImpl(userLocalDataSource, userRemoteDataSource)

        bookmarkLocalDataSource = BookmarkLocalDataSource(firebaseAuth, bookmarkDao)
        bookmarkRemoteDataSource = FakeBookmarkRemoteDataSourceImpl(firebaseAuth, firebaseFirestore)
        bookmarkRepository =
            FakeBookmarkRepositoryImpl(bookmarkLocalDataSource, bookmarkRemoteDataSource, testDispatcher)


        val userData = mutableMapOf<String, Any>()
        val mediaItems = mutableMapOf<String, Uri>()
        userData["id"] = "John"
        userData["displayName"] = "John"
        userData["userName"] = "John"
        userRepository.saveUserData("John", userData, mediaItems, context)
    }

    @Test
    fun findById() {
        assertEquals("John",bookmarkRepository.getYourUserId())
    }

    @Test
    fun upsert() {
        val bookmark = Bookmark("John", mutableListOf("John", "Ron", "Sean"))
        bookmarkLocalDataSource.upsert(bookmark)
        assertTrue(bookmarkLocalDataSource.findById("John").bookmarks.size == 3)
    }

    @Test
    fun update() = runTest {
        var originalBookmarksSize = 0
        var bookmarks = mutableListOf<String>()
        bookmarkLocalDataSource.upsert(Bookmark("John", mutableListOf("John", "Ron", "Gohan")))
        bookmarkRepository.getBookmarks().collect {
            originalBookmarksSize = it.size
            bookmarks = it.toMutableList()
        }
        bookmarks.add("Von")
        bookmarkLocalDataSource.update(Bookmark("John", bookmarks))
        var returnedBookmarksSize = 0
        var returnedBookmarks = mutableListOf<String>()
        bookmarkRepository.getBookmarks().collect {
            returnedBookmarks = it.toMutableList()
            returnedBookmarksSize = it.size
        }
        Log.d("BookmarkLocalDataSourceTest", "originalBookmarksSize = $originalBookmarksSize")
        Log.d("BookmarkLocalDataSourceTest", "returnedBookmarksSize = $returnedBookmarksSize")
        assertTrue(returnedBookmarks.contains("Von"))
    }

    @Test
    fun delete() = runTest {
        var originalNotEmpty = false
        var returnedIsEmpty = false
        var originalBookmarksSize = 0
        var bookmarks = mutableListOf<String>()
        bookmarkLocalDataSource.upsert(Bookmark("John", mutableListOf("John", "Ron", "Gohan")))
        originalNotEmpty = bookmarkLocalDataSource.findById("John") != null
        bookmarkLocalDataSource.delete(Bookmark("John", bookmarks))
        returnedIsEmpty = bookmarkLocalDataSource.findById("John") == null
        Log.d("BookmarkLocalDataSourceTest", "originalNotEmpty = $originalNotEmpty")
        Log.d("BookmarkLocalDataSourceTest", "returnedIsEmpty = $returnedIsEmpty")
        assertTrue(originalNotEmpty == true && returnedIsEmpty == true)
    }
}