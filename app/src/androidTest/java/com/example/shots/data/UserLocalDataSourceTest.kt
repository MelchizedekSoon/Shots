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

class UserLocalDataSourceTest {

    private lateinit var context: Context
    private lateinit var roomDatabase: RoomDatabase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var userRepository: UserRepository
    private lateinit var userRemoteDataSource: UserRemoteDataSource
    private lateinit var userDao: UserDao
    private lateinit var userLocalDataSource: UserLocalDataSource

    @Before
    fun setUp() = runTest {

        context = InstrumentationRegistry.getInstrumentation().context

        roomDatabase = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java // Use your actual database class
        ).allowMainThreadQueries().build()

        userDao = (roomDatabase as AppDatabase).userDao()
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

        val userData = mutableMapOf<String, Any>()
        val mediaItems = mutableMapOf<String, Uri>()
        userData["id"] = "John"
        userData["displayName"] = "John"
        userData["userName"] = "John"
        userRepository.saveUserData("John", userData, mediaItems, context)

        userLocalDataSource =
            FakeUserLocalDataSourceImpl(firebaseAuth, userDao)
    }

    @Test
    fun getAll() {
        assertTrue(userLocalDataSource.getAll().isNotEmpty())
    }

    @Test
    fun loadAllByIds() {
        //not being used currently
    }

    @Test
    fun findByName() {
        assertTrue(userLocalDataSource.findByName("John") != null)
    }

    @Test
    fun findByYourId() {
        var user = User()
        user = user.copy(id = "John", displayName = "John", userName = "John")
        userLocalDataSource.upsert(user)
        assertTrue(userLocalDataSource.findByYourId().id == "John")
    }

    @Test
    fun findById() {
        var user = User()
        user = user.copy(id = "John", displayName = "John", userName = "John")
        userLocalDataSource.upsert(user)
        assertTrue(userLocalDataSource.findById("John") != null)
    }

    @Test
    fun upsert() {
        var user = User()
        user = user.copy(id = "John", displayName = "John", userName = "John")
        userLocalDataSource.upsert(user)
        assertTrue(userLocalDataSource.findById("John") != null)
    }

    @Test
    fun upsertAll() {
        var user = User()
        user = user.copy(id = "John", displayName = "John", userName = "John")
        userLocalDataSource.upsertAll(mutableListOf(user))
        assertTrue(userLocalDataSource.findById("John") != null)
    }

    @Test
    fun update() {
        var user = User()
        user = user.copy(id = "John", displayName = "Johno", userName = "John")
        userLocalDataSource.upsert(user)
        user = user.copy(id = "John", displayName = "John", userName = "John")
        userLocalDataSource.upsert(user)
        assertTrue(userLocalDataSource.findById("John").displayName == "John")
    }

    @Test
    fun updateAll() {
        var user = User()
        user = user.copy(id = "John", displayName = "Johno", userName = "John")
        userLocalDataSource.upsert(user)
        user = user.copy(id = "John", displayName = "John", userName = "John")
        userLocalDataSource.upsert(user)
        assertTrue(userLocalDataSource.findById("John").displayName == "John")
    }

    @Test
    fun delete() {
        var user = User()
        user = user.copy(id = "John", displayName = "Johno", userName = "John")
        userLocalDataSource.upsert(user)
        userLocalDataSource.delete(user)
        assertTrue(userLocalDataSource.findById("John") == null)
    }
}