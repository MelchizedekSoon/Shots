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

class IfSeenReceivedShotRepositoryTest {

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
    private lateinit var ifSeenReceivedShotLocalDataSource: IfSeenReceivedShotLocalDataSource
    private lateinit var ifSeenReceivedShotRemoteDataSource: IfSeenReceivedShotRemoteDataSource
    private lateinit var ifSeenReceivedShotRepository: IfSeenReceivedShotRepository

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

        ifSeenReceivedShotLocalDataSource =
            IfSeenReceivedShotLocalDataSource(firebaseAuth, ifSeenReceivedShotDao)

        ifSeenReceivedShotRemoteDataSource =
            FakeIfSeenReceivedShotRemoteDataSourceImpl(
                firebaseAuth,
                firebaseFirestore,
                firebaseStorage
            )

        ifSeenReceivedShotRepository = FakeIfSeenReceivedShotRepositoryImpl(ifSeenReceivedShotLocalDataSource, ifSeenReceivedShotRemoteDataSource)
    }

    @Test
    fun getYourUserId() {
        assertTrue(ifSeenReceivedShotRemoteDataSource.getYourUserId() == "John")
    }

    @Test
    fun saveIfSeenReceivedShot() = runTest {
        val ifSeenReceivedShotData = mutableMapOf<String, Boolean>()
        ifSeenReceivedShotData["ifSeenReceivedShot-John"] = false
        ifSeenReceivedShotRepository.saveIfSeenReceivedShot("John", ifSeenReceivedShotData)
        val ifSeenReceivedShot = ifSeenReceivedShotRepository.getIfSeenReceivedShot("John")
        assertTrue(ifSeenReceivedShot.ifSeenReceivedShots.size > 0)
    }

    @Test
    fun storeIfSeenReceivedShot() = runTest {
        val ifSeenReceivedShotData = mutableMapOf<String, Boolean>()
        ifSeenReceivedShotData["ifSeenReceivedShot-John"] = false
        val ifSeenReceivedShot = IfSeenReceivedShot("John", mutableListOf("John", "Ron", "Con"))
        ifSeenReceivedShotRepository.storeIfSeenReceivedShot(ifSeenReceivedShot)
        val returnedIfSeenReceivedShot = ifSeenReceivedShotRepository.getIfSeenReceivedShot("John")
        assertTrue(returnedIfSeenReceivedShot.ifSeenReceivedShots.size > 0)
    }

    @Test
    fun getIfSeenReceivedShot() = runTest {
        val ifSeenReceivedShotData = mutableMapOf<String, Boolean>()
        ifSeenReceivedShotData["ifSeenReceivedShot-John"] = false
        val ifSeenReceivedShot = IfSeenReceivedShot("John", mutableListOf("John", "Ron", "Con"))
        ifSeenReceivedShotRepository.storeIfSeenReceivedShot(ifSeenReceivedShot)
        assertTrue(ifSeenReceivedShotRepository.getIfSeenReceivedShot("John").ifSeenReceivedShots.size > 0)
    }

    @Test
    fun getUpdatedIfSeenReceivedShots() = runTest {
        val ifSeenReceivedShotData = mutableMapOf<String, Boolean>()
        ifSeenReceivedShotData["ifSeenReceivedShot-John"] = false
        ifSeenReceivedShotRepository.saveIfSeenReceivedShot("John", ifSeenReceivedShotData)
        var ifSeenReceivedShots = mutableListOf<String>()
        ifSeenReceivedShotRepository.getUpdatedIfSeenReceivedShots().collect {
            ifSeenReceivedShots = it.toMutableList()
        }
        assertTrue(ifSeenReceivedShots.size > 0)
    }

    @Test
    fun fetchUpdatedIfSeenReceivedShots() = runTest {
        val ifSeenReceivedShotData = mutableMapOf<String, Boolean>()
        ifSeenReceivedShotData["ifSeenReceivedShot-John"] = false
        ifSeenReceivedShotData["ifSeenReceivedShot-Ron"] = false
        ifSeenReceivedShotRepository.saveIfSeenReceivedShot("John", ifSeenReceivedShotData)
        var ifSeenReceivedShots = mutableListOf<String>()
        ifSeenReceivedShotRepository.getUpdatedIfSeenReceivedShots().collect {
            ifSeenReceivedShots = it.toMutableList()
        }
        assertTrue(ifSeenReceivedShots.size >= 2)
    }

    @Test
    fun removeIfSeenReceivedShot() = runTest {
        val ifSeenReceivedShotData = mutableMapOf<String, Boolean>()
        ifSeenReceivedShotData["ifSeenReceivedShot-John"] = false
        ifSeenReceivedShotData["ifSeenReceivedShot-Ron"] = false
        ifSeenReceivedShotRepository.saveIfSeenReceivedShot("John", ifSeenReceivedShotData)
        var ifSeenReceivedShots = mutableListOf<String>()
        ifSeenReceivedShotRepository.getUpdatedIfSeenReceivedShots().collect {
            ifSeenReceivedShots = it.toMutableList()
        }
        ifSeenReceivedShotRepository.removeIfSeenReceivedShot("John")
        var updatedIfSeenReceivedShots = mutableListOf<String>()
        ifSeenReceivedShotRepository.getUpdatedIfSeenReceivedShots().collect {
            updatedIfSeenReceivedShots = it.toMutableList()
        }
        assertTrue(ifSeenReceivedShots.size > updatedIfSeenReceivedShots.size)
    }
}