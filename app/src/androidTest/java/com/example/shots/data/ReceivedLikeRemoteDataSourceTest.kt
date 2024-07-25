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

class ReceivedLikeRemoteDataSourceTest {

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
    private lateinit var receivedLikeRemoteDataSource: ReceivedLikeRemoteDataSource

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

        receivedLikeRemoteDataSource =
            FakeReceivedLikeRemoteDataSourceImpl(
                firebaseAuth,
                firebaseFirestore
            )
    }

    @Test
    fun getYourUserId() {
        assertTrue(receivedLikeRemoteDataSource.getYourUserId() == "John")
    }

    @Test
    fun writeReceivedLikeToFirebase() = runTest {
        val receivedLikeData = mutableMapOf<String, Any>()
        receivedLikeData["receivedLike-John"] = "John"
        receivedLikeData["receivedLike-Ron"] = "Ron"
        assertTrue(
            receivedLikeRemoteDataSource.writeReceivedLikeToFirebase(
                "Ron",
                receivedLikeData
            )
        )
    }

    @Test
    fun getReceivedLikesFromFirebase() = runTest {
        val receivedLikeData = mutableMapOf<String, Any>()
        receivedLikeData["receivedLike-Lon"] = "Lon"
        receivedLikeRemoteDataSource.writeReceivedLikeToFirebase("John", receivedLikeData)
        assertTrue(receivedLikeRemoteDataSource.getReceivedLikesFromFirebase().isNotEmpty())
    }

    @Test
    fun removeReceivedLikeFromFirebase() = runTest {
        val receivedLikeData = mutableMapOf<String, Any>()
        receivedLikeRemoteDataSource.writeReceivedLikeToFirebase("John", receivedLikeData)
        assertTrue(receivedLikeRemoteDataSource.removeReceivedLikeFromFirebase("John"))
    }

}