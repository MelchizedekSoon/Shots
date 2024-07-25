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

class UserWhoBlockedYouLocalDataSourceTest {

    private lateinit var context: Context
    private lateinit var roomDatabase: RoomDatabase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var userRepository: UserRepository
    private lateinit var userRemoteDataSource: UserRemoteDataSource
    private lateinit var userDao: UserDao
    private lateinit var userLocalDataSource: UserLocalDataSource
    private lateinit var userWhoBlockedYouDao: UserWhoBlockedYouDao
    private lateinit var userWhoBlockedYouLocalDataSource: UserWhoBlockedYouLocalDataSource

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

        userLocalDataSource =
            FakeUserLocalDataSourceImpl(firebaseAuth, userDao)

        userWhoBlockedYouLocalDataSource = UserWhoBlockedYouLocalDataSource(firebaseAuth, userWhoBlockedYouDao)

        val userWhoBlockedYouData = mutableMapOf<String, Any>()
        userWhoBlockedYouData["userWhoBlockedYou-John"] = "John"
        var userWhoBlockedYou = UserWhoBlockedYou()
        userWhoBlockedYou = userWhoBlockedYou.copy(
            userWhoBlockedYouId = "John",
            usersWhoBlockedYou = mutableListOf("John")
        )
        userWhoBlockedYouLocalDataSource.upsert(userWhoBlockedYou)
    }

    @Test
    fun getAll() {
        userWhoBlockedYouLocalDataSource.upsert()
        var usersWhoBlockedYou = userWhoBlockedYouLocalDataSource.getAll()
        assert(usersWhoBlockedYou.isNotEmpty())
    }

    @Test
    fun loadAllByIds() {
        //this isn't used currently
    }

    @Test
    fun findById() {
        assertTrue(userWhoBlockedYouLocalDataSource.findById("John") != null)
    }

    @Test
    fun upsert() {
        var userWhoBlockedYou = UserWhoBlockedYou()
        userWhoBlockedYou = userWhoBlockedYou.copy(
            userWhoBlockedYouId = "John",
            usersWhoBlockedYou = mutableListOf("John")
        )
        userWhoBlockedYouLocalDataSource.upsert(userWhoBlockedYou)
        assertTrue(userWhoBlockedYouLocalDataSource.findById("John").usersWhoBlockedYou.isNotEmpty())
    }

    @Test
    fun upsertAll() {
        var userWhoBlockedYou = UserWhoBlockedYou()
        userWhoBlockedYou = userWhoBlockedYou.copy(
            userWhoBlockedYouId = "John",
            usersWhoBlockedYou = mutableListOf("John")
        )
        val usersWhoBlockedYou = mutableListOf<UserWhoBlockedYou>()
        usersWhoBlockedYou.add(userWhoBlockedYou)
        userWhoBlockedYouLocalDataSource.upsertAll(usersWhoBlockedYou)
        assertTrue(userWhoBlockedYouLocalDataSource.findById("John").usersWhoBlockedYou.isNotEmpty())
    }

    @Test
    fun update() {
        var userWhoBlockedYou = UserWhoBlockedYou()
        userWhoBlockedYou = userWhoBlockedYou.copy(
            userWhoBlockedYouId = "John",
            usersWhoBlockedYou = mutableListOf("John")
        )
        userWhoBlockedYouLocalDataSource.upsert(userWhoBlockedYou)
        assertTrue(userWhoBlockedYouLocalDataSource.findById("John").usersWhoBlockedYou.isNotEmpty())
    }

    @Test
    fun delete() {
        var userWhoBlockedYou = UserWhoBlockedYou()
        userWhoBlockedYou = userWhoBlockedYou.copy(
            userWhoBlockedYouId = "John",
            usersWhoBlockedYou = mutableListOf("John")
        )
        userWhoBlockedYouLocalDataSource.upsert(userWhoBlockedYou)
        userWhoBlockedYouLocalDataSource.delete(userWhoBlockedYou)
        assertTrue(userWhoBlockedYouLocalDataSource.findById("John") == null)
    }
}