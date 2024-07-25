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

class ReceivedLikeRepositoryTest {

    private lateinit var context: Context
    private lateinit var roomDatabase: RoomDatabase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var userRepository: UserRepository
    private lateinit var userLocalDataSource: UserLocalDataSource
    private lateinit var userRemoteDataSource: UserRemoteDataSource
    private lateinit var userDao: UserDao
    private lateinit var receivedLikeDao: ReceivedLikeDao
    private lateinit var receivedLikeLocalDataSource: ReceivedLikeLocalDataSource
    private lateinit var receivedLikeRemoteDataSource: ReceivedLikeRemoteDataSource
    private lateinit var receivedLikeRepository: ReceivedLikeRepository

    @Before
    fun setUp() = runTest {
        context = InstrumentationRegistry.getInstrumentation().context

        roomDatabase = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java // Use your actual database class
        ).allowMainThreadQueries().build()

        receivedLikeDao = (roomDatabase as AppDatabase).receivedLikeDao()
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

        receivedLikeLocalDataSource =
            ReceivedLikeLocalDataSource(firebaseAuth, receivedLikeDao)

        receivedLikeRemoteDataSource =
            FakeReceivedLikeRemoteDataSourceImpl(
                firebaseAuth,
                firebaseFirestore
            )

        receivedLikeRepository = FakeReceivedLikeRepositoryImpl(
            receivedLikeLocalDataSource,
            receivedLikeRemoteDataSource
        )
    }

    @Test
    fun getYourUserId() {
        assertTrue(receivedLikeRemoteDataSource.getYourUserId() == "John")
    }

    @Test
    fun saveAndStoreReceivedLike() = runTest {
        val receivedLikeData = mutableMapOf<String, Any>()
        receivedLikeData["receivedLike-John"] = "John"
        receivedLikeRepository.saveAndStoreReceivedLike("John", "Ferris", receivedLikeData)
        val receivedLike = receivedLikeRepository.getReceivedLike("John")
        assertTrue(receivedLike.receivedLikes.size > 0)
    }

    @Test
    fun storeReceivedLike() = runTest {
        val receivedLike = ReceivedLike("John", mutableListOf("John"))
        receivedLikeRepository.storeReceivedLike(receivedLike)
        assertTrue(receivedLikeRepository.getReceivedLike("John").receivedLikes.contains("John"))
    }

    @Test
    fun getReceivedLike() = runTest {
        val receivedLikeData = mutableMapOf<String, Any>()
        receivedLikeData["receivedLike-Ferris"] = "Ferris"
        receivedLikeData["receivedLike-Ron"] = "Ron"
        receivedLikeData["receivedLike-Cato"] = "Cato"
        val receivedLike = ReceivedLike("John", mutableListOf("Ferris", "Ron", "Cato"))
        receivedLikeRepository.storeReceivedLike(receivedLike)
        assertTrue(receivedLikeRepository.getReceivedLike("John").receivedLikes.contains("Cato"))
    }

    @Test
    fun getReceivedLikes() = runTest {
        val receivedLikeData = mutableMapOf<String, Any>()
        receivedLikeData["receivedLike-Statham"] = "Statham"
        receivedLikeData["receivedLike-Leatherface"] = "Leatherface"
        receivedLikeData["receivedLike-Kane"] = "Kane"
        val receivedLike = ReceivedLike("John", mutableListOf("Statham", "Leatherface", "Kane"))
        receivedLikeRepository.storeReceivedLike(receivedLike)
        assertTrue(receivedLikeRepository.getReceivedLike("John").receivedLikes.contains("Kane"))
    }

    @Test
    fun fetchUpdatedReceivedLikes() = runTest {
        val receivedLikeData = mutableMapOf<String, Any>()
        receivedLikeData["receivedLike-Statham"] = "Statham"
        receivedLikeData["receivedLike-Leatherface"] = "Leatherface"
        receivedLikeData["receivedLike-Kane"] = "Kane"
        receivedLikeRepository.saveAndStoreReceivedLike("John", "John", receivedLikeData)
        var receivedLikes = mutableListOf<String>()
        receivedLikeRepository.fetchUpdatedReceivedLikes().collect {
            receivedLikes = it.toMutableList()
        }
        assertTrue(receivedLikes.contains("Kane"))
    }

    @Test
    fun removeReceivedLike() = runTest {
        receivedLikeRepository.removeReceivedLike("John")
        var receivedLikes = mutableListOf<String>()
        receivedLikeRepository.fetchUpdatedReceivedLikes().collect {
            receivedLikes = it.toMutableList()
        }
        assertTrue(!receivedLikes.contains("John"))
    }
}