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
import com.example.shots.data.FakeIfSeenReceivedShotRemoteDataSourceImpl
import com.example.shots.data.FakeIfSeenReceivedShotRepositoryImpl
import com.example.shots.data.FakeUserLocalDataSourceImpl
import com.example.shots.data.FakeUserRemoteDataSourceImpl
import com.example.shots.data.FakeUserRepositoryImpl
import com.example.shots.data.FirebaseRepository
import com.example.shots.data.IfSeenReceivedShotDao
import com.example.shots.data.IfSeenReceivedShotLocalDataSource
import com.example.shots.data.IfSeenReceivedShotRemoteDataSource
import com.example.shots.data.IfSeenReceivedShotRepository
import com.example.shots.data.UserDao
import com.example.shots.data.UserLocalDataSource
import com.example.shots.data.UserRemoteDataSource
import com.example.shots.data.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
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

class IfSeenReceivedShotViewModelTest {

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
    private lateinit var ifSeenReceivedShotLocalDataSource: IfSeenReceivedShotLocalDataSource
    private lateinit var ifSeenReceivedShotRemoteDataSource: IfSeenReceivedShotRemoteDataSource
    private lateinit var ifSeenReceivedShotRepository: IfSeenReceivedShotRepository
    private lateinit var ifSeenReceivedShotViewModel: IfSeenReceivedShotViewModel
    private lateinit var ifSeenReceivedShotDao: IfSeenReceivedShotDao
    private lateinit var testDispatcher: TestDispatcher

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() = runTest {
        context = InstrumentationRegistry.getInstrumentation().context

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

        firebaseStorage = FirebaseModule.provideStorage()
        firebaseStorage.useEmulator("10.0.2.2", 9199)

        userDao = (roomDatabase as AppDatabase).userDao()
        bookmarkDao = (roomDatabase as AppDatabase).bookmarkDao()
        ifSeenReceivedShotDao = (roomDatabase as AppDatabase).ifSeenReceivedShotDao()

        userLocalDataSource = FakeUserLocalDataSourceImpl(firebaseAuth, userDao)
        userRemoteDataSource =
            FakeUserRemoteDataSourceImpl(firebaseAuth, firebaseFirestore, firebaseStorage)

        bookmarkLocalDataSource = BookmarkLocalDataSource(firebaseAuth, bookmarkDao)
        bookmarkRemoteDataSource = FakeBookmarkRemoteDataSourceImpl(firebaseAuth, firebaseFirestore)

        userRepository = FakeUserRepositoryImpl(userLocalDataSource, userRemoteDataSource)

        testDispatcher = StandardTestDispatcher()

        Dispatchers.setMain(testDispatcher)

        bookmarkRepository =
            FakeBookmarkRepositoryImpl(
                bookmarkLocalDataSource,
                bookmarkRemoteDataSource,
                testDispatcher
            )

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

        testDispatcher = StandardTestDispatcher()

        Dispatchers.setMain(testDispatcher)

        bookmarkViewModel =
            BookmarkViewModel(
                firebaseRepository, bookmarkRepository, userRepository, testDispatcher
            )

        val bookmark = Bookmark("John", mutableListOf("Zeko", "Marc"))

        bookmarkLocalDataSource.bookmarkDao.upsert(bookmark)

        ifSeenReceivedShotLocalDataSource =
            IfSeenReceivedShotLocalDataSource(firebaseAuth, ifSeenReceivedShotDao)

        ifSeenReceivedShotRemoteDataSource = FakeIfSeenReceivedShotRemoteDataSourceImpl(
            firebaseAuth, firebaseFirestore,
            firebaseStorage
        )

        ifSeenReceivedShotRepository =
            FakeIfSeenReceivedShotRepositoryImpl(
                ifSeenReceivedShotLocalDataSource,
                ifSeenReceivedShotRemoteDataSource
            )

        testDispatcher = StandardTestDispatcher()

        ifSeenReceivedShotViewModel = IfSeenReceivedShotViewModel(
            ifSeenReceivedShotRepository,
            firebaseRepository, testDispatcher
        )

    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getIfSeenReceivedShotUiState() = runTest {
        val ifSeenReceivedShotData = mutableMapOf<String, Boolean>()

        ifSeenReceivedShotData["ifSeenReceivedShot-John"] = false

        ifSeenReceivedShotRemoteDataSource.writeIfSeenReceivedShotToFirebase(
            "John",
            ifSeenReceivedShotData
        )

        advanceUntilIdle()

        ifSeenReceivedShotViewModel.loadIfSeenReceivedShots()

        val ifSeenReceivedShotUiState = ifSeenReceivedShotViewModel.ifSeenReceivedShotUiState.value

        Log.d(
            "IfSeenReceivedShotViewModelTest",
            "ifSeenReceivedShotUiState = $ifSeenReceivedShotUiState"
        )

        assertTrue(ifSeenReceivedShotUiState.ifSeenReceivedShots.contains("John-false"))
    }

    @Test
    fun getYourUserId() {
        assertTrue(ifSeenReceivedShotViewModel.getYourUserId() == "John")
    }

    @Test
    fun loadIfSeenReceivedShots() = runTest {
        val ifSeenReceivedShotData = mutableMapOf<String, Boolean>()
        ifSeenReceivedShotData["ifSeenReceivedShot-John"] = false
        ifSeenReceivedShotViewModel.saveIfSeenReceivedShot("John", ifSeenReceivedShotData)
        testDispatcher.scheduler.advanceUntilIdle()
        ifSeenReceivedShotViewModel.fetchUpdatedIfSeenReceivedShots().collect {
            //nothing is collected, this just seems like the only way to get these to pass
        }
        testDispatcher.scheduler.advanceUntilIdle()
        Log.d("IfSeenReceivedShotViewModelTest", "ifSeenReceivedShots = ${ifSeenReceivedShotViewModel.ifSeenReceivedShotUiState.value.ifSeenReceivedShots}")
        assertTrue(
            ifSeenReceivedShotViewModel.ifSeenReceivedShotUiState.value.ifSeenReceivedShots.contains("John-false")
        )
        ifSeenReceivedShotViewModel.removeIfSeenReceivedShot("John")
    }

    @Test
    fun saveIfSeenReceivedShot() = runTest {
        val ifSeenReceivedShotData = mutableMapOf<String, Boolean>()
        ifSeenReceivedShotData["ifSeenReceivedShot-John"] = false
        ifSeenReceivedShotViewModel.saveIfSeenReceivedShot("John", ifSeenReceivedShotData)
        testDispatcher.scheduler.advanceUntilIdle()
        ifSeenReceivedShotViewModel.fetchUpdatedIfSeenReceivedShots().collect {
            //nothing is collected, this just seems like the only way to get these to pass
        }
        testDispatcher.scheduler.advanceUntilIdle()
        Log.d("IfSeenReceivedShotViewModelTest", "ifSeenReceivedShots = ${ifSeenReceivedShotViewModel.ifSeenReceivedShotUiState.value.ifSeenReceivedShots}")
        assertTrue(
            ifSeenReceivedShotViewModel.ifSeenReceivedShotUiState.value.ifSeenReceivedShots.contains("John-false")
        )
    }

    @Test
    fun removeIfSeenReceivedShot() = runTest {
        val ifSeenReceivedShotData = mutableMapOf<String, Boolean>()
        ifSeenReceivedShotData["ifSeenReceivedShot-John"] = false
        ifSeenReceivedShotViewModel.saveIfSeenReceivedShot("John", ifSeenReceivedShotData)

        testDispatcher.scheduler.advanceUntilIdle()
        ifSeenReceivedShotViewModel.removeIfSeenReceivedShot("John")

        testDispatcher.scheduler.advanceUntilIdle()
        Log.d("IfSeenReceivedShotViewModelTest", "ifSeenReceivedShots = ${ifSeenReceivedShotViewModel.ifSeenReceivedShotUiState.value.ifSeenReceivedShots}")
        assertTrue(
            !ifSeenReceivedShotViewModel.ifSeenReceivedShotUiState.value.ifSeenReceivedShots.contains("John-false")
        )
    }

    @Test
    fun storeReceivedShot() {
    }

    @Test
    fun storeIfSeenReceivedShotInRoom() {
    }

    @Test
    fun fetchIfSeenReceivedShotFromRoom() {
    }
}