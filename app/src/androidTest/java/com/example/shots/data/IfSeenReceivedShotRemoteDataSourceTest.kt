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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class IfSeenReceivedShotRemoteDataSourceTest {

    private lateinit var context: Context
    private lateinit var roomDatabase: RoomDatabase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var userRepository: UserRepository
    private lateinit var userLocalDataSource: UserLocalDataSource
    private lateinit var userRemoteDataSource: UserRemoteDataSource
    private lateinit var userDao: UserDao
    private lateinit var ifSeenReceivedShotDao: IfSeenReceivedShotDao
    private lateinit var ifSeenReceivedShotRemoteDataSource: IfSeenReceivedShotRemoteDataSource

    @Before
    fun setUp() = runTest {

        context = InstrumentationRegistry.getInstrumentation().context

        roomDatabase = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java // Use your actual database class
        ).allowMainThreadQueries().build()

        ifSeenReceivedShotDao = (roomDatabase as AppDatabase).ifSeenReceivedShotDao()
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

        ifSeenReceivedShotRemoteDataSource =
            FakeIfSeenReceivedShotRemoteDataSourceImpl(
                firebaseAuth,
                firebaseFirestore,
                firebaseStorage
            )
    }

    @Test
    fun getYourUserId() {
        assertTrue(ifSeenReceivedShotRemoteDataSource.getYourUserId() == "John")
    }

    @Test
    fun writeIfSeenReceivedShotToFirebase() = runTest {
        val ifSeenReceivedShotData = mutableMapOf<String, Boolean>()
        ifSeenReceivedShotData["ifSeenReceivedShot-John"] = false
        val wasDone = ifSeenReceivedShotRemoteDataSource.writeIfSeenReceivedShotToFirebase(
            "John",
            ifSeenReceivedShotData
        )
        assertTrue(wasDone)
    }

    @Test
    fun getIfSeenReceivedShotsFromFirebase() = runTest {
        val ifSeenReceivedShotData = mutableMapOf<String, Boolean>()
        ifSeenReceivedShotData["ifSeenReceivedShot-John"] = false
        ifSeenReceivedShotRemoteDataSource.writeIfSeenReceivedShotToFirebase(
            "John",
            ifSeenReceivedShotData
        )
        assertTrue(
            ifSeenReceivedShotRemoteDataSource.getIfSeenReceivedShotsFromFirebase().isNotEmpty()
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun removeIfSeenReceivedShotFromFirebase() = runTest {
        val ifSeenReceivedShotData = mutableMapOf<String, Boolean>()
        ifSeenReceivedShotData["ifSeenReceivedShot-John"] = false
        ifSeenReceivedShotRemoteDataSource.writeIfSeenReceivedShotToFirebase(
            "John",
            ifSeenReceivedShotData
        )
        advanceUntilIdle()
        ifSeenReceivedShotRemoteDataSource.removeIfSeenReceivedShotFromFirebase("John")
        assertTrue(
            !ifSeenReceivedShotRemoteDataSource.getIfSeenReceivedShotsFromFirebase().contains("John")
        )
    }
}