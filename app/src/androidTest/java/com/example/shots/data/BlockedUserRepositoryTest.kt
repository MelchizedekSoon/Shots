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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BlockedUserRepositoryTest {

    private lateinit var context: Context
    private lateinit var roomDatabase: RoomDatabase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var userRepository: UserRepository
    private lateinit var userLocalDataSource: UserLocalDataSource
    private lateinit var userRemoteDataSource: UserRemoteDataSource
    private lateinit var userDao: UserDao
    private lateinit var blockedUserRepository: BlockedUserRepository
    private lateinit var blockedUserLocalDataSource: BlockedUserLocalDataSource
    private lateinit var blockedUserRemoteDataSource: BlockedUserRemoteDataSource
    private lateinit var blockedUserDao: BlockedUserDao

    @Before
    fun setUp() = runTest {

        context = InstrumentationRegistry.getInstrumentation().context

        roomDatabase = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java // Use your actual database class
        ).allowMainThreadQueries().build()

        blockedUserDao = (roomDatabase as AppDatabase).blockedUserDao()
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
        blockedUserLocalDataSource = BlockedUserLocalDataSource(firebaseAuth, blockedUserDao)
        blockedUserRemoteDataSource =
            FakeBlockedUserRemoteDataSourceImpl(firebaseAuth, firebaseFirestore)
        blockedUserRepository =
            FakeBlockedUserRepositoryImpl(blockedUserLocalDataSource, blockedUserRemoteDataSource)

        val userData = mutableMapOf<String, Any>()
        val mediaItems = mutableMapOf<String, Uri>()
        userData["id"] = "John"
        userData["displayName"] = "John"
        userData["userName"] = "John"
        userRepository.saveUserData("John", userData, mediaItems, context)
    }

    @Test
    fun getYourUserId() {
        assertEquals("John", blockedUserRepository.getYourUserId())
    }

    @Test
    fun saveAndStoreBlockedUser() = runTest {
        val blockedUserData = mutableMapOf<String, Any>()
        blockedUserData["blockedUser-John"] = "John"
        blockedUserRepository.saveAndStoreBlockedUser("John", blockedUserData)
        var blockedUsers = mutableListOf<String>()
        blockedUserRepository.getBlockedUsers().collect {
            blockedUsers = it.toMutableList()
        }
        assertTrue(blockedUsers.contains("John"))
    }

    @Test
    fun storeBlockedUser() = runTest {
        blockedUserRepository.storeBlockedUser("John")
        assertTrue(blockedUserLocalDataSource.findById("John") != null)
    }

    @Test
    fun storeBlockedUserObject() = runTest {
        val blockedUser = BlockedUser("John", mutableListOf())
        blockedUserRepository.storeBlockedUserObject(blockedUser)
        assertTrue(blockedUserLocalDataSource.findById("John") != null)
    }

    @Test
    fun getBlockedUserObject() = runTest {
        val blockedUser = BlockedUser("John", mutableListOf())
        blockedUserRepository.storeBlockedUserObject(blockedUser)
        assertTrue(blockedUserLocalDataSource.findById("John") != null)
    }

    @Test
    fun getBlockedUsers() = runTest {
        var blockedUsers = mutableListOf<String>()
        blockedUserRepository.getBlockedUsers().collect {
            blockedUsers = it.toMutableList()
        }
        assertTrue(blockedUsers.size > 0)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun fetchUpdatedBlockedUsers() = runTest {
        val blockedUsers = mutableMapOf<String, Any>()
        blockedUsers["blockedUser-John"] = "John"
        blockedUsers["blockedUser-Ron"] = "Ron"
        blockedUsers["blockedUser-Sean"] = "Sean"
        blockedUserRepository.saveAndStoreBlockedUser("John", blockedUsers)
        advanceUntilIdle()
        blockedUsers["blockedUser-Woody"] = "Woody"
        blockedUserRepository.saveAndStoreBlockedUser("John", blockedUsers)
        var returnedBlockedUsers = mutableListOf<String>()
        blockedUserRepository.fetchUpdatedBlockedUsers().collect {
            returnedBlockedUsers = it.toMutableList()
        }
        assertTrue(returnedBlockedUsers.contains("Woody"))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteBlockedUser() = runTest {
        val blockedUsers = mutableMapOf<String, Any>()
        blockedUsers["blockedUser-John"] = "John"
        blockedUsers["blockedUser-Ron"] = "Ron"
        blockedUsers["blockedUser-Sean"] = "Sean"
        blockedUserRepository.saveAndStoreBlockedUser("John", blockedUsers)
        advanceUntilIdle()
        blockedUserRepository.deleteBlockedUser("Sean")
        advanceUntilIdle()
        var returnedBlockedUsers = mutableListOf<String>()
        blockedUserRepository.fetchUpdatedBlockedUsers().collect {
            returnedBlockedUsers = it.toMutableList()
        }
        assertTrue(!returnedBlockedUsers.contains("Sean"))
    }
}