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

class SentLikeLocalDataSourceTest {

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

        sentLikeLocalDataSource =
            SentLikeLocalDataSource(firebaseAuth, sentLikeDao)
    }

    @Test
    fun findById() {
        sentLikeLocalDataSource.upsert(SentLike("John", mutableListOf("John")))
        assertTrue(sentLikeLocalDataSource.findById("John") != null)
    }

    @Test
    fun upsert() {
        sentLikeLocalDataSource.upsert(SentLike("John", mutableListOf("John")))
        assertTrue(sentLikeLocalDataSource.findById("John").sentLikes[0] == "John")
    }

    @Test
    fun update() {
        sentLikeLocalDataSource.upsert(SentLike("John", mutableListOf("John", "Ron")))
        assertTrue(sentLikeLocalDataSource.findById("John").sentLikes.contains("Ron"))
    }

    @Test
    fun delete() {
        sentLikeLocalDataSource.upsert(SentLike("John", mutableListOf("John", "Ron")))
        sentLikeLocalDataSource.delete(sentLikeLocalDataSource.findById("John"))
        assertTrue(sentLikeLocalDataSource.findById("John") == null)
    }
}