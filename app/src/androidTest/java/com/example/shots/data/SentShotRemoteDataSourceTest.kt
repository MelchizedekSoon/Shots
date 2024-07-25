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
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*

import org.junit.Before
import org.junit.Test
import java.io.File

class SentShotRemoteDataSourceTest {

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
    private lateinit var sentShotRemoteDataSource: SentShotRemoteDataSource
    private lateinit var downloadUrl: Map<String, Uri?>

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

        sentShotRemoteDataSource =
            FakeSentShotRemoteDataSourceImpl(
                firebaseAuth,
                firebaseFirestore,
                firebaseStorage
            )

        var sentShots = mutableListOf<String>()
        val sentShotData = mutableMapOf<String, Uri>()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val tempFile = File.createTempFile("temp_video", ".mp4", context.cacheDir)
        val file = tempFile.toUri()

        sentShotData["sentShot-John"] = file
        downloadUrl = sentShotRemoteDataSource.uploadSentShotToStorage("John", sentShotData, context)
    }

    @Test
    fun getYourUserId() {
        assertTrue(sentShotRemoteDataSource.getYourUserId() == "John")
    }

    @Test
    fun writeSentShotToFirebase() = runTest {
        var sentShots = mutableListOf<String>()
        val sentShotData = mutableMapOf<String, Uri>()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val tempFile = File.createTempFile("temp_video", ".mp4", context.cacheDir)
        val file = tempFile.toUri()

        sentShotData["sentShot-John"] = file
        sentShotRemoteDataSource.writeSentShotToFirebase("John", sentShotData, context)
        sentShots = sentShotRemoteDataSource.getSentShotsFromFirebase().toMutableList()
        Log.d("SentShotRepositoryTest", "sentShots = $sentShots")
        assertTrue(sentShots.size > 0)
        sentShotRemoteDataSource.removeSentShotFromFirebase("John")
    }

    @Test
    fun removeSentShotFromFirebase() = runTest {
        var sentShots = mutableListOf<String>()
        val sentShotData = mutableMapOf<String, Uri>()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val tempFile = File.createTempFile("temp_video", ".mp4", context.cacheDir)
        val file = tempFile.toUri()

        sentShotData["sentShot-John"] = file
        sentShotRemoteDataSource.writeSentShotToFirebase("John", sentShotData, context)
        sentShots = sentShotRemoteDataSource.getSentShotsFromFirebase().toMutableList()
        Log.d("SentShotRepositoryTest", "sentShots = $sentShots")

        assertTrue(sentShotRemoteDataSource.removeSentShotFromFirebase("John"))
    }

    @Test
    fun getSentShotsFromFirebase() = runTest {
        assertTrue(sentShotRemoteDataSource.removeSentShotFromFirebase("John"))
    }

    @Test
    fun uploadSentShotToStorage() = runTest {
        var sentShots = mutableListOf<String>()
        val sentShotData = mutableMapOf<String, Uri>()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val tempFile = File.createTempFile("temp_video", ".mp4", context.cacheDir)
        val file = tempFile.toUri()

        sentShotData["sentShot-John"] = file
        val returnedUrls = sentShotRemoteDataSource.uploadSentShotToStorage("John", sentShotData, context)
        assertTrue(returnedUrls["sentShot-John"].toString().contains("sentShot-John"))
    }
}