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
import org.junit.Assert.*

import org.junit.Before
import org.junit.Test

class UserWhoBlockedYouRemoteDataSourceTest {

    private lateinit var context: Context
    private lateinit var roomDatabase: RoomDatabase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var userRepository: UserRepository
    private lateinit var userLocalDataSource: UserLocalDataSource
    private lateinit var userRemoteDataSource: UserRemoteDataSource
    private lateinit var userDao: UserDao
    private lateinit var userWhoBlockedYouDao: UserWhoBlockedYouDao
    private lateinit var userWhoBlockedYouRemoteDataSource: UserWhoBlockedYouRemoteDataSource

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

        val userData = mutableMapOf<String, Any>()
        val mediaItems = mutableMapOf<String, Uri>()
        userData["id"] = "John"
        userData["displayName"] = "John"
        userData["userName"] = "John"
        userRepository.saveUserData("John", userData, mediaItems, context)

        userWhoBlockedYouRemoteDataSource = FakeUserWhoBlockedYouRemoteDataSourceImpl(firebaseAuth, firebaseFirestore)

        val userWhoBlockedYouData = mutableMapOf<String, Any>()
        userWhoBlockedYouData["userWhoBlockedYou-John"] = "John"
        userWhoBlockedYouRemoteDataSource.writeUserWhoBlockedYouToFirebase("John", userWhoBlockedYouData)

    }

    @Test
    fun getYourUserId() {
        assertTrue(userWhoBlockedYouRemoteDataSource.getYourUserId() == "John")
    }

    @Test
    fun getUsersWhoBlockedYouFromFirebase() = runTest {
        assertTrue(userWhoBlockedYouRemoteDataSource.getUsersWhoBlockedYouFromFirebase().isNotEmpty())
    }

    @Test
    fun writeUserWhoBlockedYouToFirebase() = runTest {
        val userWhoBlockedYouData = mutableMapOf<String, Any>()
        userWhoBlockedYouData["userWhoBlockedYou-Von"] = "Von"
        userWhoBlockedYouRemoteDataSource.writeUserWhoBlockedYouToFirebase("John", userWhoBlockedYouData)
        assertTrue(userWhoBlockedYouRemoteDataSource.getUsersWhoBlockedYouFromFirebase().contains("Von"))
    }

    @Test
    fun deleteUserWhoBlockedYouFromFirebase() = runTest {
        val userWhoBlockedYouData = mutableMapOf<String, Any>()
        userWhoBlockedYouData["userWhoBlockedYou-Von"] = "Von"
        userWhoBlockedYouRemoteDataSource.writeUserWhoBlockedYouToFirebase("John", userWhoBlockedYouData)
        userWhoBlockedYouRemoteDataSource.deleteUserWhoBlockedYouFromFirebase("John")
        assertTrue(!userWhoBlockedYouRemoteDataSource.getUsersWhoBlockedYouFromFirebase().contains("John"))
    }
}