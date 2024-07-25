package com.example.shots.data

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class ReceivedShotRepositoryTest {

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
    private lateinit var receivedShotRemoteDataSource: ReceivedShotRemoteDataSource
    private lateinit var receivedShotRepository: ReceivedShotRepository

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

        receivedShotRemoteDataSource =
            FakeReceivedShotRemoteDataSourceImpl(
                firebaseAuth,
                firebaseFirestore,
                firebaseStorage
            )

        receivedShotRepository = FakeReceivedShotRepositoryImpl(
            receivedShotLocalDataSource,
            receivedShotRemoteDataSource
        )
    }

    @Test
    fun getYourUserId() {
        assertTrue(receivedShotRepository.getYourUserId() == "John")
    }

    @Test
    fun saveReceivedShot() = runTest {
        var receivedShots = mutableListOf<String>()
        val receivedShotData = mutableMapOf<String, Uri>()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val tempFile = File.createTempFile("temp_video", ".mp4", context.cacheDir)
        val file = tempFile.toUri()

        receivedShotData["receivedShot-John"] = file
        receivedShotRepository.saveReceivedShot("John", receivedShotData, context)
        receivedShotRepository.fetchUpdatedReceivedShots().collect {
            receivedShots = it.toMutableList()
        }
        Log.d("ReceivedShotRepositoryTest", "receivedShots = $receivedShots")
        assertTrue(receivedShots.size > 0)
        receivedShotRepository.removeTheirReceivedShotFromFirebase("John")
    }

    @Test
    fun storeReceivedShot() = runTest {
        var receivedShots = mutableListOf<String>()
        val receivedShotData = mutableMapOf<String, Uri>()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val tempFile = File.createTempFile("temp_video", ".mp4", context.cacheDir)
        val file = tempFile.toUri()

        receivedShotData["receivedShot-John"] = file
        receivedShotRepository.saveReceivedShot("John", receivedShotData, context)
        receivedShotRepository.getUpdatedReceivedShots().collect {
            receivedShots = it.toMutableList()
        }
        val receivedShot = ReceivedShot("John", receivedShots)
        receivedShotRepository.storeReceivedShot(receivedShot)
        val returnedReceivedShot = receivedShotRepository.getReceivedShot("John")
        receivedShotRepository.getUpdatedReceivedShots().collect {
            receivedShots = it.toMutableList()
        }
        Log.d("ReceivedShotRepositoryTest", "receivedShots = $receivedShots")
        assertTrue(returnedReceivedShot.receivedShots.size > 0)
        receivedShotRepository.removeTheirReceivedShotFromFirebase("John")
    }

    @Test
    fun getReceivedShot() = runTest {
        val receivedShotData = mutableMapOf<String, Uri>()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val tempFile = File.createTempFile("temp_video", ".mp4", context.cacheDir)
        val file = tempFile.toUri()

        receivedShotData["receivedShot-John"] = file
        receivedShotRepository.saveReceivedShot("John", receivedShotData, context)
        assertTrue(
            receivedShotRepository.getReceivedShot("John").receivedShots.size > 0
        )
    }

    @Test
    fun removeReceivedShot() = runTest {
        var receivedShots = mutableListOf<String>()
        val receivedShotData = mutableMapOf<String, Uri>()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val tempFile = File.createTempFile("temp_video", ".mp4", context.cacheDir)
        val file = tempFile.toUri()

        receivedShotData["receivedShot-John"] = file
        receivedShotRepository.saveReceivedShot("John", receivedShotData, context)
        receivedShotRepository.removeReceivedShot("John")
        receivedShotRepository.fetchUpdatedReceivedShots().collect {
            receivedShots = it.toMutableList()
        }
        assertTrue(
            !receivedShots.contains("receivedShot-John")
        )
    }

    @Test
    fun removeTheirReceivedShotFromFirebase() = runTest {
        var receivedShots = mutableListOf<String>()
        val receivedShotData = mutableMapOf<String, Uri>()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val tempFile = File.createTempFile("temp_video", ".mp4", context.cacheDir)
        val file = tempFile.toUri()

        receivedShotData["receivedShot-John"] = file
        receivedShotRepository.saveReceivedShot("John", receivedShotData, context)
        receivedShotRepository.removeTheirReceivedShotFromFirebase("John")
        receivedShotRepository.fetchUpdatedReceivedShots().collect {
            receivedShots = it.toMutableList()
        }
        assertTrue(
            !receivedShots.contains("receivedShot-John")
        )
    }

    @Test
    fun getUpdatedReceivedShots() = runTest {
        var receivedShots = mutableListOf<String>()
        val receivedShotData = mutableMapOf<String, Uri>()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val tempFile = File.createTempFile("temp_video", ".mp4", context.cacheDir)
        val file = tempFile.toUri()

        receivedShotData["receivedShot-John"] = file
        receivedShotRepository.saveReceivedShot("John", receivedShotData, context)
        receivedShotRepository.getUpdatedReceivedShots().collect {
            receivedShots = it.toMutableList()
        }
        assertTrue(
            receivedShots[0].isNotEmpty()
        )
        receivedShotRepository.removeTheirReceivedShotFromFirebase("John")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun fetchUpdatedReceivedShots() = runTest {
        var receivedShots = mutableListOf<String>()
        val receivedShotData = mutableMapOf<String, Uri>()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val tempFile = File.createTempFile("temp_video", ".mp4", context.cacheDir)
        val file = tempFile.toUri()

        receivedShotData["receivedShot-John"] = file
        receivedShotRepository.saveReceivedShot("John", receivedShotData, context)
        advanceUntilIdle()
        receivedShotRepository.removeTheirReceivedShotFromFirebase("John")
        advanceUntilIdle()
        receivedShotRepository.fetchUpdatedReceivedShots().collect {
            receivedShots = it.toMutableList()
        }
        for (receivedShot in receivedShots) {
            Log.d("ReceivedShotRepositoryTest", "receivedShot = $receivedShot")
        }
        Log.d("ReceivedShotRepositoryTest", "receivedShots = ${receivedShots[0]}")
        assertTrue(
            receivedShots[0].isEmpty()
        )
        receivedShotRepository.removeTheirReceivedShotFromFirebase("John")
    }
}