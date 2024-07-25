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

class SentLikeRepositoryTest {

    private lateinit var context: Context
    private lateinit var roomDatabase: RoomDatabase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var userRepository: UserRepository
    private lateinit var userLocalDataSource: UserLocalDataSource
    private lateinit var userRemoteDataSource: UserRemoteDataSource
    private lateinit var userDao: UserDao
    private lateinit var sentLikeDao: SentLikeDao
    private lateinit var sentLikeLocalDataSource: SentLikeLocalDataSource
    private lateinit var sentLikeRemoteDataSource: SentLikeRemoteDataSource
    private lateinit var sentLikeRepository: SentLikeRepository

    @Before
    fun setUp() = runTest {
        context = InstrumentationRegistry.getInstrumentation().context

        roomDatabase = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java // Use your actual database class
        ).allowMainThreadQueries().build()

        sentLikeDao = (roomDatabase as AppDatabase).sentLikeDao()
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

        sentLikeLocalDataSource = SentLikeLocalDataSource(firebaseAuth, sentLikeDao)

        sentLikeRemoteDataSource = FakeSentLikeRemoteDataSourceImpl(
            firebaseAuth, firebaseFirestore, sentLikeDao
        )

        sentLikeRepository = FakeSentLikeRepositoryImpl(
            sentLikeLocalDataSource, sentLikeRemoteDataSource
        )
    }

    @Test
    fun getYourUserId() {
        assertTrue(sentLikeRemoteDataSource.getYourUserId() == "John")
    }

    @Test
    fun saveAndStoreSentLike() = runTest {
        val sentLikeData = mutableMapOf<String, Any>()
        sentLikeData["sentLike-John"] = "John"
        sentLikeRepository.saveAndStoreSentLike("John", sentLikeData)
        val sentLike = sentLikeRepository.getSentLike("John")
        assertTrue(sentLike.sentLikes.size > 0)
    }

    @Test
    fun storeSentLike() = runTest {
        val sentLike = SentLike("John", mutableListOf("John"))
        sentLikeRepository.storeSentLike(sentLike)
        assertTrue(sentLikeRepository.getSentLike("John").sentLikes.contains("John"))
    }

    @Test
    fun getSentLike() = runTest {
        val sentLikeData = mutableMapOf<String, Any>()
        sentLikeData["sentLike-Ferris"] = "Ferris"
        sentLikeData["sentLike-Ron"] = "Ron"
        sentLikeData["sentLike-Cato"] = "Cato"
        val sentLike = SentLike("John", mutableListOf("Ferris", "Ron", "Cato"))
        sentLikeRepository.storeSentLike(sentLike)
        assertTrue(sentLikeRepository.getSentLike("John").sentLikes.contains("Cato"))
    }

    @Test
    fun removeSentLike() = runTest {
        sentLikeRepository.removeSentLike("John")
        var sentLikes = mutableListOf<String>()
        sentLikeRepository.fetchUpdatedSentLikes().collect {
            sentLikes = it.toMutableList()
        }
        assertTrue(!sentLikes.contains("John"))
    }

    @Test
    fun fetchUpdatedSentLikes() = runTest {
        val sentLikeData = mutableMapOf<String, Any>()
        sentLikeData["sentLike-Statham"] = "Statham"
        sentLikeData["sentLike-Leatherface"] = "Leatherface"
        sentLikeData["sentLike-Kane"] = "Kane"
        sentLikeRepository.saveAndStoreSentLike("John", sentLikeData)
        var sentLikes = mutableListOf<String>()
        sentLikeRepository.fetchUpdatedSentLikes().collect {
            sentLikes = it.toMutableList()
        }
        assertTrue(sentLikes.contains("Kane"))
    }
}