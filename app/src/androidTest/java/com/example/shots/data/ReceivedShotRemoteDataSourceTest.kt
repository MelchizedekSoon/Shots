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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class ReceivedShotRemoteDataSourceTest {

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
    private lateinit var receivedShotRemoteDataSource: ReceivedShotRemoteDataSource
    private lateinit var downloadUrl: Map<String, Uri?>

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

        receivedShotRemoteDataSource =
            FakeReceivedShotRemoteDataSourceImpl(
                firebaseAuth,
                firebaseFirestore,
                firebaseStorage
            )

        var receivedShots = mutableListOf<String>()
        val receivedShotData = mutableMapOf<String, Uri>()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val tempFile = File.createTempFile("temp_video", ".mp4", context.cacheDir)
        val file = tempFile.toUri()

        receivedShotData["receivedShot-John"] = file
        downloadUrl = receivedShotRemoteDataSource.uploadReceivedShotToStorage("John", receivedShotData, context)
    }

    @Test
    fun getYourUserId() {
        assertTrue(receivedShotRemoteDataSource.getYourUserId() == "John")
    }

    @Test
    fun writeReceivedShotToFirebase() = runTest {
        var receivedShots = mutableListOf<String>()
        val receivedShotData = mutableMapOf<String, Uri>()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val tempFile = File.createTempFile("temp_video", ".mp4", context.cacheDir)
        val file = tempFile.toUri()

        receivedShotData["receivedShot-John"] = file
        receivedShotRemoteDataSource.writeReceivedShotToFirebase("John", receivedShotData, context)
        receivedShots = receivedShotRemoteDataSource.getReceivedShotsFromFirebase().toMutableList()
        Log.d("ReceivedShotRepositoryTest", "receivedShots = $receivedShots")
        assertTrue(receivedShots.size > 0)
    }

    @Test
    fun uploadReceivedShotToStorage() = runTest {
        var receivedShots = mutableListOf<String>()
        val receivedShotData = mutableMapOf<String, Uri>()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val tempFile = File.createTempFile("temp_video", ".mp4", context.cacheDir)
        val file = tempFile.toUri()

        receivedShotData["receivedShot-John"] = file
        val downloadUrl = receivedShotRemoteDataSource.uploadReceivedShotToStorage("John", receivedShotData, context)
        Log.d("ReceivedShotRemoteDataSourceTest", "downloadUrl = ${downloadUrl["receivedShot-John"]}")
        assertTrue(downloadUrl["receivedShot-John"].toString().contains("receivedShot-John"))
    }

    @Test
    fun getReceivedShotsFromFirebase() = runTest {
        val receivedShotData = mutableMapOf<String, Uri>()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val tempFile = File.createTempFile("temp_video", ".mp4", context.cacheDir)
        val file = tempFile.toUri()

        receivedShotData["receivedShot-John"] = file
        receivedShotRemoteDataSource.writeReceivedShotToFirebase("John", receivedShotData, context)
        val receivedShots: MutableList<String> =
            receivedShotRemoteDataSource.getReceivedShotsFromFirebase().toMutableList()
        Log.d("ReceivedShotRepositoryTest", "receivedShots = $receivedShots")
        assertTrue(receivedShots[0].startsWith("John"))
    }

    @Test
    fun getIfSeenReceivedShotsFromFirebase() = runTest {
        val ifSeenReceivedShots = receivedShotRemoteDataSource.getIfSeenReceivedShotsFromFirebase()
        Log.d("ReceivedShotRemoteDataSourceTest", "ifSeenReceivedShots = $ifSeenReceivedShots")
        assertTrue(ifSeenReceivedShots.isNotEmpty())
    }

    @Test
    fun removeReceivedShotFromFirebase() = runTest {
        var receivedShotData = mutableMapOf<String, Uri>()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val tempFile = File.createTempFile("temp_video", ".mp4", context.cacheDir)
        val file = tempFile.toUri()

        receivedShotData["receivedShot-John"] = file
        receivedShotRemoteDataSource.writeReceivedShotToFirebase("John", receivedShotData, context)
        assertTrue(receivedShotRemoteDataSource.removeReceivedShotFromFirebase("John"))
    }

    @Test
    fun removeTheirReceivedShotFromFirebase() = runTest {
        var receivedShotData = mutableMapOf<String, Uri>()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val tempFile = File.createTempFile("temp_video", ".mp4", context.cacheDir)
        val file = tempFile.toUri()

        receivedShotData["receivedShot-John"] = file
        receivedShotRemoteDataSource.writeReceivedShotToFirebase("John", receivedShotData, context)
        assertTrue(receivedShotRemoteDataSource.removeTheirReceivedShotFromFirebase("John"))
    }
}