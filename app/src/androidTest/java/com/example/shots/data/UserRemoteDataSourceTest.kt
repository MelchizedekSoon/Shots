package com.example.shots.data

import android.content.Context
import android.net.Uri
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

class UserRemoteDataSourceTest {

    private lateinit var context: Context
    private lateinit var roomDatabase: RoomDatabase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var userRepository: UserRepository
    private lateinit var userLocalDataSource: UserLocalDataSource
    private lateinit var userRemoteDataSource: UserRemoteDataSource
    private lateinit var userDao: UserDao

    @Before
    fun setUp() = runTest {
        context = InstrumentationRegistry.getInstrumentation().context

        roomDatabase = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java // Use your actual database class
        ).allowMainThreadQueries().build()

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

    }

    @Test
    fun getYourUserId() {
        assertTrue(userRemoteDataSource.getYourUserId() == "John")
    }

    @Test
    fun getUsers() = runTest {
        assertTrue(userRemoteDataSource.getUsers().isNotEmpty())
    }

    @Test
    fun getUserData() = runTest {
        val user = userRemoteDataSource.getUserData("John")
        assertTrue(user?.id == "John")
    }

    @Test
    fun deleteMediaFromFirebase() = runTest {
        val userData = mutableMapOf<String, Any>()
        val mediaItems: MutableMap<String, Uri> = mutableMapOf()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val tempFile = File.createTempFile("temp_video", ".mp4", context.cacheDir)
        val file = tempFile.toUri()
        mediaItems["mediaOne"] = file
        userRemoteDataSource.writeUserDataToFirebase("John", userData, mediaItems, context)
        userRemoteDataSource.deleteMediaFromFirebase("John", "mediaOne")
        assertTrue(userRemoteDataSource.getUserData("John")?.mediaOne?.isEmpty() == true)
    }

    @Test
    fun writeUserDataToFirebase() = runTest {
        val userData = mutableMapOf<String, Any>()
        val mediaItems: MutableMap<String, Uri> = mutableMapOf()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val tempFile = File.createTempFile("temp_video", ".mp4", context.cacheDir)
        val file = tempFile.toUri()
        mediaItems["mediaOne"] = file
        userRemoteDataSource.writeUserDataToFirebase("John", userData, mediaItems, context)
        assertTrue(userRemoteDataSource.getUserData("John")?.mediaOne?.isEmpty() == false)
    }
}