package com.example.shots.data

import android.content.Context
import android.net.Uri
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BookmarkRepositoryTest {

    private lateinit var context: Context
    private lateinit var roomDatabase: RoomDatabase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var userRepository: UserRepository
    private lateinit var userLocalDataSource: UserLocalDataSource
    private lateinit var userRemoteDataSource: UserRemoteDataSource
    private lateinit var userDao: UserDao
    private lateinit var bookmarkRepository: BookmarkRepository
    private lateinit var bookmarkLocalDataSource: BookmarkLocalDataSource
    private lateinit var bookmarkRemoteDataSource: BookmarkRemoteDataSource
    private lateinit var bookmarkDao: BookmarkDao
    private lateinit var testDispatcher: TestDispatcher

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() = runTest {

        context = InstrumentationRegistry.getInstrumentation().context

        roomDatabase = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java // Use your actual database class
        ).allowMainThreadQueries().build()

        bookmarkDao = (roomDatabase as AppDatabase).bookmarkDao()
        userDao = (roomDatabase as AppDatabase).userDao()

        testDispatcher = StandardTestDispatcher()

        Dispatchers.setMain(testDispatcher)

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
        bookmarkRemoteDataSource =
            FakeBookmarkRemoteDataSourceImpl(firebaseAuth, firebaseFirestore)
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
    fun getYourUserId() {
        assertEquals("John", bookmarkRepository.getYourUserId())
    }

    @Test
    fun saveAndStoreBookmark() = runTest {
        val bookmarkData = mutableMapOf<String, Any>()
        bookmarkData["bookmark-Zion"] = "Zion"
        bookmarkRepository.saveAndStoreBookmark("John", bookmarkData)
        var bookmarks = mutableListOf<String>()
        bookmarkRepository.getBookmarks().collect {
            bookmarks = it.toMutableList()
        }
        assertTrue(bookmarks.contains("Zion"))
    }

    @Test
    fun removeBookmark() = runTest {
        val bookmarkData = mutableMapOf<String, Any>()
        bookmarkData["bookmark-Orion"] = "Orion"
        bookmarkRepository.saveAndStoreBookmark("John", bookmarkData)
        bookmarkRepository.removeBookmark("Orion")
        var bookmarks = mutableListOf<String>()
        bookmarkRepository.getBookmarks().collect {
            bookmarks = it.toMutableList()
        }
        assertFalse(bookmarks.contains("Orion"))
    }

    @Test
    fun storeBookmarkObject() = runTest {
        val bookmark = Bookmark("John", mutableListOf())
        bookmarkRepository.storeBookmarkObject(bookmark)
        assertTrue(bookmarkLocalDataSource.findById("John") != null)
    }

    @Test
    fun storeBookmark() = runTest {
        val bookmark = Bookmark("John", mutableListOf())
        bookmarkRepository.storeBookmarkObject(bookmark)
        assertTrue(bookmarkLocalDataSource.findById("John") != null)
    }

    @Test
    fun getBookmark() = runTest {
        val bookmarkData = mutableMapOf<String, Any>()
        bookmarkData["bookmark-Ron"] = "Ron"
        bookmarkRepository.saveAndStoreBookmark("John", bookmarkData)
        val bookmark = bookmarkRepository.getBookmark("John")
        assertTrue(bookmark.bookmarkId == "John")
    }

    @Test
    fun getBookmarks() = runTest {
        var bookmarks = mutableListOf<String>()
        val bookmarkData = mutableMapOf<String, Any>()
        bookmarkData["bookmark-John"] = "John"
        bookmarkData["bookmark-Ron"] = "Ron"
        bookmarkRepository.saveAndStoreBookmark("John", bookmarkData)
        bookmarkRepository.getBookmarks().collect {
            bookmarks = it.toMutableList()
        }
        assertTrue(bookmarks.size > 0)
    }

    @Test
    fun fetchUpdatedBookmarks() = runTest {
        var bookmarks = mutableListOf<String>()
        val bookmarkData = mutableMapOf<String, Any>()
        bookmarkData["bookmark-Sharon"] = "Sharon"
        bookmarkData["bookmark-Caron"] = "Caron"
        bookmarkRepository.saveAndStoreBookmark("John", bookmarkData)
        bookmarkRepository.fetchUpdatedBookmarks().collect {
            bookmarks = it.toMutableList()
        }
        assertTrue(bookmarks.size > 0)
    }

    @Test
    fun fetchBookmarks() = runTest {
        var bookmarks = mutableListOf<String>()
        val bookmarkData = mutableMapOf<String, Any>()
        bookmarkData["bookmark-Sharon"] = "Sharon"
        bookmarkData["bookmark-Caron"] = "Caron"
        bookmarkRepository.saveAndStoreBookmark("John", bookmarkData)
        bookmarkRepository.fetchBookmarks().collect {
            bookmarks = it.toMutableList()
        }
        assertTrue(bookmarks.size > 0)
    }
}