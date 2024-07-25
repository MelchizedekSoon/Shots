package com.example.shots.ui.theme

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.test.platform.app.InstrumentationRegistry
import com.example.shots.FirebaseModule
import com.example.shots.data.AppDatabase
import com.example.shots.data.AuthRemoteDataSource
import com.example.shots.data.AuthRepository
import com.example.shots.data.AuthRepositoryImpl
import com.example.shots.data.BlockedUserDao
import com.example.shots.data.BlockedUserLocalDataSource
import com.example.shots.data.BlockedUserRemoteDataSource
import com.example.shots.data.BlockedUserRepository
import com.example.shots.data.FakeBlockedUserRemoteDataSourceImpl
import com.example.shots.data.FakeBlockedUserRepositoryImpl
import com.example.shots.data.FakeFirebaseRepositoryImpl
import com.example.shots.data.FakeReceivedShotRemoteDataSourceImpl
import com.example.shots.data.FakeReceivedShotRepositoryImpl
import com.example.shots.data.FakeUserLocalDataSourceImpl
import com.example.shots.data.FakeUserRemoteDataSourceImpl
import com.example.shots.data.FakeUserRepositoryImpl
import com.example.shots.data.FakeUserWhoBlockedYouRemoteDataSourceImpl
import com.example.shots.data.FakeUserWhoBlockedYouRepositoryImpl
import com.example.shots.data.FirebaseRepository
import com.example.shots.data.ReceivedShotDao
import com.example.shots.data.ReceivedShotLocalDataSource
import com.example.shots.data.ReceivedShotRemoteDataSource
import com.example.shots.data.ReceivedShotRepository
import com.example.shots.data.User
import com.example.shots.data.UserDao
import com.example.shots.data.UserLocalDataSource
import com.example.shots.data.UserRemoteDataSource
import com.example.shots.data.UserRepository
import com.example.shots.data.UserWhoBlockedYouDao
import com.example.shots.data.UserWhoBlockedYouLocalDataSource
import com.example.shots.data.UserWhoBlockedYouRemoteDataSource
import com.example.shots.data.UserWhoBlockedYouRepository
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
import java.io.File

class ReceivedShotViewModelTest {

    private lateinit var roomDatabase: RoomDatabase
    private lateinit var context: Context
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var userDao: UserDao
    private lateinit var firebaseRepository: FirebaseRepository
    private lateinit var authRemoteDataSource: AuthRemoteDataSource
    private lateinit var userLocalDataSource: UserLocalDataSource
    private lateinit var userRemoteDataSource: UserRemoteDataSource
    private lateinit var userRepository: UserRepository
    private lateinit var userWhoBlockedYouDao: UserWhoBlockedYouDao
    private lateinit var userWhoBlockedYouLocalDataSource: UserWhoBlockedYouLocalDataSource
    private lateinit var userWhoBlockedYouRemoteDataSource: UserWhoBlockedYouRemoteDataSource
    private lateinit var userWhoBlockedYouRepository: UserWhoBlockedYouRepository
    private lateinit var blockedUserDao: BlockedUserDao
    private lateinit var blockedUserLocalDataSource: BlockedUserLocalDataSource
    private lateinit var blockedUserRemoteDataSource: BlockedUserRemoteDataSource
    private lateinit var blockedUserRepository: BlockedUserRepository
    private lateinit var locationViewModel: LocationViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var authRepository: AuthRepository
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var receivedShotDao: ReceivedShotDao
    private lateinit var receivedShotLocalDataSource: ReceivedShotLocalDataSource
    private lateinit var receivedShotRemoteDataSource: ReceivedShotRemoteDataSource
    private lateinit var receivedShotRepository: ReceivedShotRepository
    private lateinit var receivedShotViewModel: ReceivedShotViewModel
    private lateinit var user: User
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

        receivedShotDao = (roomDatabase as AppDatabase).receivedShotDao()
        userWhoBlockedYouDao = (roomDatabase as AppDatabase).userWhoBlockedYouDao()
        blockedUserDao = (roomDatabase as AppDatabase).blockedUserDao()
        userDao = (roomDatabase as AppDatabase).userDao()

        userLocalDataSource = FakeUserLocalDataSourceImpl(firebaseAuth, userDao)
        userRemoteDataSource =
            FakeUserRemoteDataSourceImpl(firebaseAuth, firebaseFirestore, firebaseStorage)

        authRemoteDataSource = AuthRemoteDataSource(firebaseAuth, firebaseFirestore)

        authRepository = AuthRepositoryImpl(authRemoteDataSource)

        userRepository = FakeUserRepositoryImpl(userLocalDataSource, userRemoteDataSource)

        testDispatcher = StandardTestDispatcher()

        Dispatchers.setMain(testDispatcher)

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

        locationViewModel = LocationViewModel(firebaseRepository, userRepository, testDispatcher)

        userWhoBlockedYouLocalDataSource =
            UserWhoBlockedYouLocalDataSource(firebaseAuth, userWhoBlockedYouDao)

        userWhoBlockedYouRemoteDataSource =
            FakeUserWhoBlockedYouRemoteDataSourceImpl(firebaseAuth, firebaseFirestore)

        userWhoBlockedYouRepository = FakeUserWhoBlockedYouRepositoryImpl(
            userWhoBlockedYouLocalDataSource,
            userWhoBlockedYouRemoteDataSource
        )

        blockedUserLocalDataSource = BlockedUserLocalDataSource(firebaseAuth, blockedUserDao)

        blockedUserRemoteDataSource =
            FakeBlockedUserRemoteDataSourceImpl(firebaseAuth, firebaseFirestore)

        blockedUserRepository =
            FakeBlockedUserRepositoryImpl(blockedUserLocalDataSource, blockedUserRemoteDataSource)

        userViewModel = UserViewModel(
            firebaseRepository,
            userRepository,
            userWhoBlockedYouRepository,
            blockedUserRepository,
            testDispatcher
        )

        locationViewModel.saveAndStoreLocation("John", 3.4, 4.5, context, userViewModel)

        testDispatcher.scheduler.advanceUntilIdle()

        userViewModel.getCurrentUser("John").collect {
            user = it
            Log.d("LocationViewModelTest", "user = $user")
        }

        loginViewModel = LoginViewModel(authRepository)

        receivedShotLocalDataSource = ReceivedShotLocalDataSource(firebaseAuth, receivedShotDao)

        receivedShotRemoteDataSource =
            FakeReceivedShotRemoteDataSourceImpl(firebaseAuth, firebaseFirestore, firebaseStorage)

        receivedShotRepository = FakeReceivedShotRepositoryImpl(
            receivedShotLocalDataSource,
            receivedShotRemoteDataSource
        )

        receivedShotViewModel = ReceivedShotViewModel(
            receivedShotRepository,
            firebaseRepository,
            testDispatcher
        )

        var receivedShots = mutableListOf<String>()
        val receivedShotData = mutableMapOf<String, Uri>()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val tempFile = File.createTempFile("temp_video", ".mp4", context.cacheDir)
        val file = tempFile.toUri()

        receivedShotData["receivedShot-John"] = file
        receivedShotViewModel.saveReceivedShot("John", receivedShotData, context)

//        receivedShotData["receivedShot-Von"] = file
//        receivedShotViewModel.saveReceivedShot("John", receivedShotData, context)


        receivedShotViewModel.fetchUpdatedReceivedShots().collect {

        }

        advanceUntilIdle()

    }

    @Test
    fun getScope() {
    }

    @Test
    fun getReceivedShotUiState() {
    }

    @Test
    fun getYourUserId() {
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun loadReceivedShots() = runTest {
        var receivedShots = mutableListOf<String>()
        val receivedShotData = mutableMapOf<String, Uri>()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val tempFile = File.createTempFile("temp_video", ".mp4", context.cacheDir)
        val file = tempFile.toUri()


//        receivedShotData["receivedShot-Von"] = file
//        receivedShotViewModel.saveReceivedShot("John", receivedShotData, context)

        receivedShotData["receivedShot-John"] = file
        receivedShotViewModel.saveReceivedShot("John", receivedShotData, context)

        receivedShotViewModel.fetchUpdatedReceivedShots().collect {

        }

        advanceUntilIdle()

        Log.d("ReceivedShotViewModelTest", "receivedShots = ${receivedShotViewModel.receivedShotUiState.value.receivedShots}")
        assertTrue(receivedShotViewModel.receivedShotUiState.value.receivedShots.isNotEmpty())
    }

    @Test
    fun saveReceivedShot() {

    }

    @Test
    fun removeReceivedShot() {
    }

    @Test
    fun removeTheirReceivedShot() {
    }

    @Test
    fun storeReceivedShot() {
    }

    @Test
    fun storeReceivedShotInRoom() {
    }

    @Test
    fun fetchReceivedShotFromRoom() {

    }

    @Test
    fun fetchUpdatedReceivedShots() = runTest {
        val receivedShotData = mutableMapOf<String, Uri>()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val tempFile = File.createTempFile("temp_video", ".mp4", context.cacheDir)
        val file = tempFile.toUri()

//        receivedShotData["receivedShot-Von"] = file
//        receivedShotViewModel.saveReceivedShot("John", receivedShotData, context)

        receivedShotData["receivedShot-John"] = file
        receivedShotViewModel.saveReceivedShot("John", receivedShotData, context)

        var receivedShots = mutableListOf<String>()
        receivedShotViewModel.fetchUpdatedReceivedShots().collect {
            receivedShots = it.toMutableList()
        }
        Log.d("ReceivedShotViewModelTest", "receivedShots = $receivedShots")
        assertTrue(receivedShots.size > 0)
    }
}