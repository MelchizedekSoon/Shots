package com.example.shots.ui.theme

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.test.platform.app.InstrumentationRegistry
import com.example.shots.FirebaseModule
import com.example.shots.data.AppDatabase
import com.example.shots.data.BlockedUserDao
import com.example.shots.data.BlockedUserLocalDataSource
import com.example.shots.data.FakeBlockedUserRemoteDataSourceImpl
import com.example.shots.data.FakeBlockedUserRepositoryImpl
import com.example.shots.data.FakeUserLocalDataSourceImpl
import com.example.shots.data.FakeUserRemoteDataSourceImpl
import com.example.shots.data.FakeUserRepositoryImpl
import com.example.shots.data.UserDao
import com.example.shots.data.UserLocalDataSource
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
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions
import javax.inject.Inject

class BlockedUserViewModelTest {

    var yourUserId = "John"

    lateinit var context: Context

    private lateinit var blockedUserRepository: FakeBlockedUserRepositoryImpl

    private lateinit var userRepository: FakeUserRepositoryImpl

    private lateinit var blockedUserViewModel: BlockedUserViewModel

    private lateinit var roomDatabase: RoomDatabase

    private lateinit var blockedUserLocalDataSource: BlockedUserLocalDataSource

    private lateinit var blockedUserRemoteDataSource: FakeBlockedUserRemoteDataSourceImpl

    private lateinit var userLocalDataSource: UserLocalDataSource

    private lateinit var userRemoteDataSource: FakeUserRemoteDataSourceImpl

    lateinit var firebaseAuth: FirebaseAuth

    lateinit var firebaseFirestore: FirebaseFirestore

    lateinit var firebaseStorage: FirebaseStorage

    private lateinit var blockedUserDao: BlockedUserDao

    private lateinit var userDao: UserDao

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

        blockedUserDao = (roomDatabase as AppDatabase).blockedUserDao()

        blockedUserLocalDataSource = BlockedUserLocalDataSource(firebaseAuth, blockedUserDao)

        blockedUserRemoteDataSource =
            FakeBlockedUserRemoteDataSourceImpl(firebaseAuth, firebaseFirestore)

        blockedUserRepository =
            FakeBlockedUserRepositoryImpl(blockedUserLocalDataSource, blockedUserRemoteDataSource)

        testDispatcher = StandardTestDispatcher()

        Dispatchers.setMain(testDispatcher)

        blockedUserViewModel = BlockedUserViewModel(blockedUserRepository, testDispatcher)

        userDao = (roomDatabase as AppDatabase).userDao()

        userLocalDataSource = FakeUserLocalDataSourceImpl(firebaseAuth, userDao)

        userRemoteDataSource =
            FakeUserRemoteDataSourceImpl(firebaseAuth, firebaseFirestore, firebaseStorage)

        userRepository = FakeUserRepositoryImpl(userLocalDataSource, userRemoteDataSource)

        val userData: MutableMap<String, Any> = mutableMapOf()
        val mediaItems: MutableMap<String, Uri> = mutableMapOf()
        userData["id"] = "John"
        userData["displayName"] = "John"
        userData["userName"] = "John"

        userRepository.saveUserData("John", userData, mediaItems, context)

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getBlockedUsers() = runTest {

        val blockedUserData = mutableMapOf<String, Any>()

        blockedUserData["blockedUser-Jhan"] = "Jhan"

        blockedUserViewModel.saveAndStoreBlockedUser("John", blockedUserData)

        advanceUntilIdle()

        var blockedUsers = mutableListOf<String>()

        blockedUserViewModel.fetchUpdatedBlockedUsers().collect {
            blockedUsers = it.toMutableList()
        }

        advanceUntilIdle()

        Assertions.assertTrue(blockedUsers.contains("Jhan"))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun fetchUpdatedBlockedUsers() = runTest {
        var blockedUsers = mutableListOf<String>()

        val blockedUserData = mutableMapOf<String, Any>()

        blockedUserData["blockedUser-Masika"] = "Masika"

        blockedUserViewModel.saveAndStoreBlockedUser("John", blockedUserData)

        advanceUntilIdle()

        blockedUserViewModel.fetchUpdatedBlockedUsers().collect {
            blockedUsers = it.toMutableList()
        }

        advanceUntilIdle()

        assertTrue(blockedUsers.contains("Masika"))
    }

    @Test
    fun storeBlockedUser() = runTest {

        var blockedUsers = mutableListOf<String>()

        blockedUserViewModel.storeBlockedUser("Sean")
        blockedUserViewModel.storeBlockedUser("Ron")

        testDispatcher.scheduler.advanceUntilIdle()

        blockedUserViewModel.getBlockedUsers().collect {
            blockedUsers = it.toMutableList()
        }

        testDispatcher.scheduler.advanceUntilIdle()

        Assertions.assertTrue(blockedUsers.contains("Sean") && blockedUsers.contains("Ron"))
    }

    @Test
    fun saveAndStoreBlockedUser() = runTest {

        val blockedUserData: MutableMap<String, Any> =
            mutableMapOf()

        blockedUserData["blockedUser-Pikachu"] = "Pikachu"

        blockedUserData["blockedUser-Eevee"] = "Eevee"

        blockedUserViewModel.saveAndStoreBlockedUser(yourUserId, blockedUserData)

        var blockedUsers = mutableListOf<String>()

        testDispatcher.scheduler.advanceUntilIdle()

        blockedUserViewModel.getBlockedUsers().collect {
            blockedUsers = it.toMutableList()
        }

        testDispatcher.scheduler.advanceUntilIdle()

        Assertions.assertTrue(blockedUsers.contains("Pikachu") && blockedUsers.contains("Eevee"))
    }

    @Test
    fun deleteBlockedUser() = runTest {

        var blockedUsers = mutableListOf<String>()

        val blockedUserData: MutableMap<String, Any> =
            mutableMapOf()

        blockedUserData["blockedUser-Sean"] = "Sean"
        blockedUserData["blockedUser-Ron"] = "Ron"

        blockedUserViewModel.saveAndStoreBlockedUser(yourUserId, blockedUserData)

        testDispatcher.scheduler.advanceUntilIdle()

        blockedUserViewModel.deleteBlockedUser("Sean")

        testDispatcher.scheduler.advanceUntilIdle()

        blockedUserViewModel.fetchUpdatedBlockedUsers().collect {
            blockedUsers = it.toMutableList()
        }

        testDispatcher.scheduler.advanceUntilIdle()

        Assertions.assertFalse(blockedUsers.contains("Sean"))

    }

    @Test
    fun loadBlockedUsers() = runTest {
        val blockedUserData = mutableMapOf<String, Any>()
        blockedUserData["blockedUser-John"] = "John"
        blockedUserData["blockedUser-Ron"] = "Ron"
        blockedUserData["blockedUser-Von"] = "Von"
        blockedUserViewModel.saveAndStoreBlockedUser("John", blockedUserData)
        val blockedUsers = blockedUserViewModel.uiState.value.blockedUsers
        Log.d("BlockedUserViewModelTest", "blockedUsers = $blockedUsers")
        assertTrue(blockedUsers.isNotEmpty())
    }

}