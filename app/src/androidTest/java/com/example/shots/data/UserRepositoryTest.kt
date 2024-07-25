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

class UserRepositoryTest {

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
        assertTrue(userRepository.getYourUserId() == "John")
    }

    @Test
    fun saveUserData() = runTest {
        val userData = mutableMapOf<String, Any>()
        val mediaItems = mutableMapOf<String, Uri>()
        userData["id"] = "John"
        userData["displayName"] = "Johno"
        userData["userName"] = "John"
        userRepository.saveUserData("John", userData, mediaItems, context)
        userRepository.fetchUpdatedCurrentUser().collect {
            assertTrue(it.displayName == "Johno")
        }
    }

    @Test
    fun upsertUser() = runTest {
        val userData = mutableMapOf<String, Any>()
        val mediaItems = mutableMapOf<String, Uri>()
        userData["id"] = "John"
        userData["displayName"] = "Johno"
        userData["userName"] = "John"
        var user = User()
        user = user.copy(id = "John", displayName = "Johno", userName = "John")
        userRepository.storeUser(user)
        userRepository.getCurrentUser().collect {
            assertTrue(it.displayName == "Johno")
        }
    }

    @Test
    fun upsertUsers() = runTest {
        val userData = mutableMapOf<String, Any>()
        val mediaItems = mutableMapOf<String, Uri>()
        userData["id"] = "John"
        userData["displayName"] = "Johno"
        userData["userName"] = "John"
        var user = User()
        user = user.copy(id = "John", displayName = "Johnto", userName = "John")
        userRepository.storeUser(user)
        userRepository.getCurrentUser().collect {
            assertTrue(it.displayName == "Johnto")
        }
    }

    @Test
    fun getAllUsers() = runTest {
        val userData = mutableMapOf<String, Any>()
        val mediaItems = mutableMapOf<String, Uri>()
        userData["id"] = "Von"
        userData["displayName"] = "Von"
        userData["userName"] = "Von"
        userRepository.saveUserData("Von", userData, mediaItems, context)
        userRepository.getAllUsers().collect {
            assertTrue(it.isNotEmpty())
        }
    }

    @Test
    fun fetchUpdatedUsers() = runTest {
        var users = listOf<User>()
        userRepository.fetchUpdatedUsers().collect {
            users = it
        }
        assertTrue(users.isNotEmpty())
    }

    @Test
    fun fetchUpdatedCurrentUser() = runTest {
        var user = User()
        userRepository.fetchUpdatedCurrentUser().collect {
            user = it
        }
        assertTrue(user.displayName == "John")
    }

    @Test
    fun getCurrentUser() = runTest {
        var user = User()
        user = user.copy(id = "John", displayName = "John", userName = "John")
        userLocalDataSource.upsert(user)
        userRepository.getCurrentUser().collect {
            assertTrue(it.displayName == "John")
        }
    }

    @Test
    fun getUser() = runTest {
        assertTrue(userRepository.getUser("John").displayName == "John")
    }

    @Test
    fun storeUser() = runTest {
        var user = User()
        user = user.copy(id = "John", displayName = "John", userName = "John")
        userRepository.storeUser(user)
        userRepository.getCurrentUser().collect {
            assertTrue(it.displayName == "John")
        }
    }

    @Test
    fun storeUsers() = runTest {
        var user = User()
        user = user.copy(id = "John", displayName = "John", userName = "John")
        userRepository.storeUser(user)
        userRepository.getCurrentUser().collect {
            assertTrue(it.displayName == "John")
        }
    }

    @Test
    fun updateUsers() = runTest {
        var user = User()
        user = user.copy(id = "John", displayName = "John", userName = "John")
        userRepository.storeUser(user)
        userRepository.updateUsers()
        userRepository.getAllUsers().collect {
            assertTrue(it.isNotEmpty())
        }
    }
}