package com.example.shots.ui.theme

import android.content.Context
import android.net.Uri
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
import com.google.firebase.auth.FirebaseUser
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
import org.junit.Before
import org.junit.Test

class AuthViewModelTest {

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
    private lateinit var authViewModel: AuthViewModel
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

        testDispatcher = StandardTestDispatcher()

        Dispatchers.setMain(testDispatcher)

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

        testDispatcher = StandardTestDispatcher()

        Dispatchers.setMain(testDispatcher)

        bookmarkViewModel =
            BookmarkViewModel(
                firebaseRepository, bookmarkRepository, userRepository, testDispatcher
            )

        val bookmark = Bookmark("John", mutableListOf("Zeko", "Marc"))

        bookmarkLocalDataSource.bookmarkDao.upsert(bookmark)

        authViewModel = AuthViewModel(firebaseRepository)

        authViewModel.createUserWithEmailAndPassword("John@gmail.com", "password")

    }

    @Test
    fun getEmailText() {
        //not currently being used
    }

    @Test
    fun setEmailText() {
        //not currently being used
    }

    @Test
    fun getPasswordText() {
        //not currently being used
    }

    @Test
    fun setPasswordText() {
        //not currently being used
    }

    @Test
    fun signIn() {
        //not currently being used
    }

    @Test
    fun onEmailTextChanged() {
        //not currently being used
    }

    @Test
    fun onPasswordTextChanged() {
        //not currently being used
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun signInWithEmail() = runTest {

        val firebaseUser = authViewModel.signInWithEmailAndPassword("John@gmail.com", "password")

        advanceUntilIdle()

        Assert.assertTrue(firebaseUser != null)

    }

    @Test
    fun createUserWithEmailAndPassword() = runTest {
        Assert.assertTrue(
            authViewModel.signInWithEmailAndPassword(
                "John@gmail.com",
                "password"
            ) != null
        )
    }
}