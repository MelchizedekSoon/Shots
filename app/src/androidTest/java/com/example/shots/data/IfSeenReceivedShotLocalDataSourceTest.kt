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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class IfSeenReceivedShotLocalDataSourceTest {

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
    }

    @Test
    fun findById() {
        ifSeenReceivedShotLocalDataSource.upsert(IfSeenReceivedShot("John", mutableListOf("John", "Ron", "Von")))
        assertEquals("John", ifSeenReceivedShotLocalDataSource.findById("John").ifSeenReceivedShotId)
    }

    @Test
    fun upsert() {
        val ifSeenReceivedShotData = mutableMapOf<String, Boolean>()
        ifSeenReceivedShotData["ifSeenReceivedShot-John"] = false
        ifSeenReceivedShotData["ifSeenReceivedShot-Ron"] = false
        ifSeenReceivedShotData["ifSeenReceivedShot-Von"] = false
        val ifSeenReceivedShot = IfSeenReceivedShot("John", mutableListOf("John", "Von", "Con"))
        ifSeenReceivedShotLocalDataSource.upsert(ifSeenReceivedShot)
        assertTrue(ifSeenReceivedShotLocalDataSource.findById("John").ifSeenReceivedShots.size == 3)
    }

    @Test
    fun update() {
        val ifSeenReceivedShotData = mutableMapOf<String, Boolean>()
        ifSeenReceivedShotData["ifSeenReceivedShot-John"] = false
        ifSeenReceivedShotData["ifSeenReceivedShot-Ron"] = false
        ifSeenReceivedShotData["ifSeenReceivedShot-Von"] = false
        var ifSeenReceivedShots = mutableListOf<String>()
        ifSeenReceivedShots.add("John")
        ifSeenReceivedShots.add("Von")
        ifSeenReceivedShots.add("Con")
        var ifSeenReceivedShot = IfSeenReceivedShot("John", ifSeenReceivedShots)
        ifSeenReceivedShotLocalDataSource.upsert(ifSeenReceivedShot)
        ifSeenReceivedShotData["ifSeenReceivedShot-Mon"] = false
        ifSeenReceivedShots.add("Mon")
        ifSeenReceivedShotLocalDataSource.update(IfSeenReceivedShot("John", ifSeenReceivedShots))
        assertTrue(ifSeenReceivedShotLocalDataSource.findById("John").ifSeenReceivedShots.size == 4)
    }

    @Test
    fun delete() {
        val ifSeenReceivedShotData = mutableMapOf<String, Boolean>()
        ifSeenReceivedShotData["ifSeenReceivedShot-John"] = false
        ifSeenReceivedShotData["ifSeenReceivedShot-Ron"] = false
        ifSeenReceivedShotData["ifSeenReceivedShot-Von"] = false
        var ifSeenReceivedShots = mutableListOf<String>()
        ifSeenReceivedShots.add("John")
        ifSeenReceivedShots.add("Von")
        ifSeenReceivedShots.add("Con")
        var ifSeenReceivedShot = IfSeenReceivedShot("John", ifSeenReceivedShots)
        ifSeenReceivedShotLocalDataSource.delete(ifSeenReceivedShot)
        assertTrue(ifSeenReceivedShotLocalDataSource.findById("John") == null)
    }
}