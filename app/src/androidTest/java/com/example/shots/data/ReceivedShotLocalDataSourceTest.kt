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

class ReceivedShotLocalDataSourceTest {

    private lateinit var context: Context
    private lateinit var roomDatabase: RoomDatabase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var userRepository: UserRepository
    private lateinit var userLocalDataSource: UserLocalDataSource
    private lateinit var userRemoteDataSource: UserRemoteDataSource
    private lateinit var userDao: UserDao
    private lateinit var receivedShotDao: ReceivedShotDao
    private lateinit var receivedShotLocalDataSource: ReceivedShotLocalDataSource

    @Before
    fun setUp() = runTest {

        context = InstrumentationRegistry.getInstrumentation().context

        roomDatabase = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java // Use your actual database class
        ).allowMainThreadQueries().build()

        receivedShotDao = (roomDatabase as AppDatabase).receivedShotDao()
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

        receivedShotLocalDataSource =
            ReceivedShotLocalDataSource(firebaseAuth, receivedShotDao)
    }

    @Test
    fun findById() {
        receivedShotLocalDataSource.upsert(ReceivedShot("John", mutableListOf("John")))
        assertTrue(receivedShotLocalDataSource.findById("John") != null)
    }

    @Test
    fun upsert() {
        receivedShotLocalDataSource.upsert(ReceivedShot("John", mutableListOf("Sean")))
        assertTrue(receivedShotLocalDataSource.findById("John").receivedShots.contains("Sean"))
    }

    @Test
    fun update() {
        receivedShotLocalDataSource.upsert(ReceivedShot("John", mutableListOf("John")))
        val originalReceivedShots: MutableList<String> = receivedShotLocalDataSource.findById("John").receivedShots
        receivedShotLocalDataSource.update(ReceivedShot("John", mutableListOf("John", "Sean")))
        val updatedReceivedShots: MutableList<String> = receivedShotLocalDataSource.findById("John").receivedShots
        assertTrue((!originalReceivedShots.contains("Sean")) && updatedReceivedShots.contains("Sean"))
    }

    @Test
    fun delete() {
        receivedShotLocalDataSource.upsert(ReceivedShot("John", mutableListOf("John")))
        receivedShotLocalDataSource.delete("John")
        assertTrue(receivedShotLocalDataSource.findById("John") == null)
    }
}