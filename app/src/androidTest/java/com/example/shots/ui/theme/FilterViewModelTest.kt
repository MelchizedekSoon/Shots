package com.example.shots.ui.theme

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.test.platform.app.InstrumentationRegistry
import com.example.shots.FirebaseModule
import com.example.shots.data.AppDatabase
import com.example.shots.data.BookmarkDao
import com.example.shots.data.Distance
import com.example.shots.data.FakeFirebaseRepositoryImpl
import com.example.shots.data.FakeUserLocalDataSourceImpl
import com.example.shots.data.FakeUserRemoteDataSourceImpl
import com.example.shots.data.FakeUserRepositoryImpl
import com.example.shots.data.FirebaseRepository
import com.example.shots.data.ShowMe
import com.example.shots.data.User
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FilterViewModelTest {

    private lateinit var roomDatabase: RoomDatabase
    private lateinit var context: Context
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseRepository: FirebaseRepository
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var userRepository: UserRepository
    private lateinit var filterViewModel: FilterViewModel
    private lateinit var userLocalDataSource: UserLocalDataSource
    private lateinit var userRemoteDataSource: UserRemoteDataSource
    private lateinit var userDao: UserDao
    private lateinit var bookmarkDao: BookmarkDao
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

        userLocalDataSource = FakeUserLocalDataSourceImpl(firebaseAuth, userDao)
        userRemoteDataSource =
            FakeUserRemoteDataSourceImpl(firebaseAuth, firebaseFirestore, firebaseStorage)

        firebaseRepository =
            FakeFirebaseRepositoryImpl(firebaseAuth, firebaseFirestore, firebaseStorage)

        userRepository = FakeUserRepositoryImpl(userLocalDataSource, userRemoteDataSource)

        testDispatcher = StandardTestDispatcher()

        Dispatchers.setMain(testDispatcher)

        filterViewModel = FilterViewModel(firebaseRepository, userRepository, testDispatcher)

        val userData = mutableMapOf<String, Any>()
        val mediaItems = mutableMapOf<String, Uri>()

        userData["id"] = "John"
        userData["displayName"] = "John"
        userData["userName"] = "John"

        userRepository.saveUserData("John", userData, mediaItems, context)

        testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun getUser() {
    }

    @Test
    fun setUser() {
    }

    @Test
    fun getUiState() {
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun loadFilters() = runTest {

        val userData = mutableMapOf<String, Any>()
        val mediaItems = mutableMapOf<String, Uri>()

        userData["showMe"] = ShowMe.MEN
        userData["showUsers"] = Distance.ONE_HUNDRED
        userData["acceptShots"] = Distance.TEN
        userData["ageMinToShow"] = 35
        userData["ageMaxToShow"] = 99

        userRepository.saveUserData("John", userData, mediaItems, context)

        filterViewModel.updateShowMe(ShowMe.WOMEN)

        filterViewModel.saveAndStoreFilters(context)

        testDispatcher.scheduler.advanceUntilIdle()

        filterViewModel.loadFilters()

        advanceUntilIdle()

        testDispatcher.scheduler.advanceUntilIdle()

        Log.d("FilterViewModelTest", "showMe = ${filterViewModel.uiState.value.showMe}")

        assertTrue(filterViewModel.uiState.value.showMe == ShowMe.WOMEN)
    }

    @Test
    fun updateShowMe() {
        filterViewModel.updateShowMe(ShowMe.WOMEN)
        assertTrue(filterViewModel.uiState.value.showMe == ShowMe.WOMEN)
    }

    @Test
    fun updateShowUsers() {
        filterViewModel.updateShowUsers(Distance.SEVENTY)
        assertTrue(filterViewModel.uiState.value.showUsers == Distance.SEVENTY)
    }

    @Test
    fun updateAcceptShots() {
        filterViewModel.updateAcceptShots(Distance.SIXTY)
        assertTrue(filterViewModel.uiState.value.acceptShots == Distance.SIXTY)
    }

    @Test
    fun updateAgeMinToShow() {
        filterViewModel.updateAgeMinToShow(30)
        assertTrue(filterViewModel.uiState.value.ageMinToShow == 30)
    }

    @Test
    fun updateAgeMaxToShow() {
        filterViewModel.updateAgeMaxToShow(80)
        assertTrue(filterViewModel.uiState.value.ageMaxToShow == 80)
    }

    @Test
    fun saveAndStoreFilters() = runTest {

        filterViewModel.updateShowMe(ShowMe.WOMEN)

        filterViewModel.updateAgeMinToShow(23)

        filterViewModel.updateAgeMaxToShow(45)

        filterViewModel.saveAndStoreFilters(context)

        testDispatcher.scheduler.advanceUntilIdle()

        var user = User()

        userRepository.fetchUpdatedCurrentUser().collect { updatedUser ->
            Log.d("FilterViewModelTest", "updatedUser: ${updatedUser.showMe}")
            user = updatedUser
        }

        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(user.showMe == ShowMe.WOMEN)
    }
}