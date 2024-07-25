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
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UserWhoBlockedYouRepositoryTest {

    private lateinit var context: Context
    private lateinit var roomDatabase: RoomDatabase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var userDao: UserDao
    private lateinit var userLocalDataSource: UserLocalDataSource
    private lateinit var userRemoteDataSource: UserRemoteDataSource
    private lateinit var userRepository: UserRepository
    private lateinit var userWhoBlockedYouDao: UserWhoBlockedYouDao
    private lateinit var userWhoBlockedYouLocalDataSource: UserWhoBlockedYouLocalDataSource
    private lateinit var userWhoBlockedYouRemoteDataSource: UserWhoBlockedYouRemoteDataSource
    private lateinit var userWhoBlockedYouRepository: UserWhoBlockedYouRepository

    @Before
    fun setUp() = runTest {
        context = InstrumentationRegistry.getInstrumentation().context

        roomDatabase = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java // Use your actual database class
        ).allowMainThreadQueries().build()

        userDao = (roomDatabase as AppDatabase).userDao()
        userWhoBlockedYouDao = (roomDatabase as AppDatabase).userWhoBlockedYouDao()

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

        userWhoBlockedYouLocalDataSource = UserWhoBlockedYouLocalDataSource(firebaseAuth, userWhoBlockedYouDao)
        userWhoBlockedYouRemoteDataSource = FakeUserWhoBlockedYouRemoteDataSourceImpl(firebaseAuth, firebaseFirestore)
        userWhoBlockedYouRepository = UserWhoBlockedYouRepositoryImpl(userWhoBlockedYouLocalDataSource, userWhoBlockedYouRemoteDataSource)

        val userData = mutableMapOf<String, Any>()
        val mediaItems = mutableMapOf<String, Uri>()
        userData["id"] = "John"
        userData["displayName"] = "John"
        userData["userName"] = "John"
        userRepository.saveUserData("John", userData, mediaItems, context)
    }

    @Test
    fun getYourUserId() {
        assertTrue(userWhoBlockedYouRepository.getYourUserId() == "John")
    }

    @Test
    fun saveAndStoreUserWhoBlockedYou() = runTest {
        val userWhoBlockedYouData = mutableMapOf<String, Any>()
        userWhoBlockedYouData["userWhoBlockedYou-John"] = "John"
        userWhoBlockedYouData["userWhoBlockedYou-Von"] = "Von"
        userWhoBlockedYouData["userWhoBlockedYou-Khan"] = "Khan"
        userWhoBlockedYouRepository.saveAndStoreUserWhoBlockedYou("John", userWhoBlockedYouData)
    }

    @Test
    fun storeUserWhoBlockedYou() = runTest {
        userWhoBlockedYouRepository.storeUserWhoBlockedYou(UserWhoBlockedYou("John", mutableListOf("John", "Von")))
        userWhoBlockedYouRepository.getUsersWhoBlockedYou().collect {
            assertTrue(it.contains("John"))
        }
    }

    @Test
    fun getUserWhoBlockedYou() = runTest {
        userWhoBlockedYouRepository.storeUserWhoBlockedYou(UserWhoBlockedYou("John", mutableListOf("John", "Von")))
        userWhoBlockedYouRepository.getUsersWhoBlockedYou().collect {
            assertTrue(it.contains("John"))
        }
    }

    @Test
    fun getUsersWhoBlockedYou() = runTest {
        var usersWhoBlockedYou: List<String> = mutableListOf()
        userWhoBlockedYouRepository.storeUserWhoBlockedYou(UserWhoBlockedYou("John", mutableListOf("John", "Von")))
        userWhoBlockedYouRepository.getUsersWhoBlockedYou().collect {
            usersWhoBlockedYou = it.toMutableList()
        }
        assertTrue(usersWhoBlockedYou.isNotEmpty())
    }

    @Test
    fun fetchUpdatedUsersWhoBlockedYou() = runTest {
        var usersWhoBlockedYou: List<String> = mutableListOf()
        userWhoBlockedYouRepository.fetchUpdatedUsersWhoBlockedYou().collect {
            usersWhoBlockedYou = it.toMutableList()
        }
        assertTrue(usersWhoBlockedYou.isNotEmpty())
    }

    @Test
    fun deleteUserWhoBlockedYou() = runTest {
        var usersWhoBlockedYou: List<String> = mutableListOf()
        userWhoBlockedYouRepository.deleteUserWhoBlockedYou("John")
        userWhoBlockedYouRepository.fetchUpdatedUsersWhoBlockedYou().collect {
            usersWhoBlockedYou = it.toMutableList()
        }
        assertTrue(!usersWhoBlockedYou.contains("John"))
    }
}