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
import org.junit.Assert.*

import org.junit.Before
import org.junit.Test
import java.io.File

class SentShotRepositoryTest {

    private lateinit var context: Context
    private lateinit var roomDatabase: RoomDatabase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var userRepository: UserRepository
    private lateinit var userLocalDataSource: UserLocalDataSource
    private lateinit var userRemoteDataSource: UserRemoteDataSource
    private lateinit var userDao: UserDao
    private lateinit var sentShotDao: SentShotDao
    private lateinit var sentShotLocalDataSource: SentShotLocalDataSource
    private lateinit var sentShotRemoteDataSource: SentShotRemoteDataSource
    private lateinit var sentShotRepository: SentShotRepository

    @Before
    fun setUp() = runTest {
        context = InstrumentationRegistry.getInstrumentation().context

        roomDatabase = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java // Use your actual database class
        ).allowMainThreadQueries().build()

        sentShotDao = (roomDatabase as AppDatabase).sentShotDao()
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

        sentShotLocalDataSource =
            SentShotLocalDataSource(firebaseAuth, sentShotDao)

        sentShotRemoteDataSource =
            FakeSentShotRemoteDataSourceImpl(
                firebaseAuth,
                firebaseFirestore,
                firebaseStorage
            )

        sentShotRepository = FakeSentShotRepositoryImpl(
            sentShotLocalDataSource,
            sentShotRemoteDataSource
        )
    }

    @Test
    fun getYourUserId() {
        assertTrue(sentShotRepository.getYourUserId() == "John")
    }

    @Test
    fun saveSentShot() = runTest {
        var sentShots = mutableListOf<String>()
        val sentShotData = mutableMapOf<String, Uri>()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val tempFile = File.createTempFile("temp_video", ".mp4", context.cacheDir)
        val file = tempFile.toUri()

        sentShotData["sentShot-John"] = file
        sentShotRepository.saveSentShot("John", sentShotData, context)
        sentShotRepository.fetchUpdatedSentShots().collect {
            sentShots = it.toMutableList()
        }
        Log.d("SentShotRepositoryTest", "sentShots = $sentShots")
        assertTrue(sentShots.size > 0)
        sentShotRepository.removeSentShot("John")
    }

    @Test
    fun storeSentShot() = runTest {
        var sentShots = mutableListOf<String>()
        val sentShotData = mutableMapOf<String, Uri>()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val tempFile = File.createTempFile("temp_video", ".mp4", context.cacheDir)
        val file = tempFile.toUri()

        sentShotData["sentShot-John"] = file
        sentShotRepository.saveSentShot("John", sentShotData, context)
        sentShotRepository.getUpdatedSentShots().collect {
            sentShots = it.toMutableList()
        }
        val sentShot = SentShot("John", sentShots)
        sentShotRepository.storeSentShot(sentShot)
        val returnedSentShot = sentShotRepository.getSentShot("John")
        sentShotRepository.getUpdatedSentShots().collect {
            sentShots = it.toMutableList()
        }
        Log.d("SentShotRepositoryTest", "sentShots = $sentShots")
        assertTrue(returnedSentShot.sentShots.size > 0)
        sentShotRepository.removeSentShot("John")
    }

    @Test
    fun getSentShot() = runTest {
        val sentShotData = mutableMapOf<String, Uri>()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val tempFile = File.createTempFile("temp_video", ".mp4", context.cacheDir)
        val file = tempFile.toUri()

        sentShotData["sentShot-John"] = file
        sentShotRepository.saveSentShot("John", sentShotData, context)
        assertTrue(
            sentShotRepository.getSentShot("John").sentShots.size > 0
        )
    }

    @Test
    fun removeSentShot() = runTest {
        var sentShots = mutableListOf<String>()
        val sentShotData = mutableMapOf<String, Uri>()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val tempFile = File.createTempFile("temp_video", ".mp4", context.cacheDir)
        val file = tempFile.toUri()

        sentShotData["sentShot-John"] = file
        sentShotRepository.saveSentShot("John", sentShotData, context)
        sentShotRepository.removeSentShot("John")
        sentShotRepository.fetchUpdatedSentShots().collect {
            sentShots = it.toMutableList()
        }
        assertTrue(
            !sentShots.contains("sentShot-John")
        )
    }

    @Test
    fun getUpdatedSentShots() = runTest {
        var sentShots = mutableListOf<String>()
        val sentShotData = mutableMapOf<String, Uri>()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val tempFile = File.createTempFile("temp_video", ".mp4", context.cacheDir)
        val file = tempFile.toUri()

        sentShotData["sentShot-John"] = file
        sentShotRepository.saveSentShot("John", sentShotData, context)
        sentShotRepository.getUpdatedSentShots().collect {
            sentShots = it.toMutableList()
        }
        assertTrue(
            sentShots[0].isNotEmpty()
        )
        sentShotRepository.removeSentShot("John")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun fetchUpdatedSentShots() = runTest {
        var sentShots = mutableListOf<String>()
        val sentShotData = mutableMapOf<String, Uri>()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val tempFile = File.createTempFile("temp_video", ".mp4", context.cacheDir)
        val file = tempFile.toUri()

        sentShotData["sentShot-John"] = file
        sentShotRepository.saveSentShot("John", sentShotData, context)
        advanceUntilIdle()
        sentShotRepository.fetchUpdatedSentShots().collect {
            sentShots = it.toMutableList()
        }
        for (sentShot in sentShots) {
            Log.d("SentShotRepositoryTest", "sentShot = $sentShot")
        }
        Log.d("SentShotRepositoryTest", "sentShots = ${sentShots[0]}")
        assertTrue(
            sentShots[0].contains("sentShot-John")
        )
        sentShotRepository.removeSentShot("John")
    }
}