package com.example.shots.data

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.shots.FirebaseModule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BlockedUserLocalDataSourceTest {

    private lateinit var context: Context
    private lateinit var roomDatabase: AppDatabase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var userDao: UserDao
    private lateinit var blockedUserDao: BlockedUserDao
    private lateinit var userRepository: UserRepository
    private lateinit var userLocalDataSource: UserLocalDataSource
    private lateinit var userRemoteDataSource: UserRemoteDataSource

    @Before
    fun setUp() = runTest {
        context = InstrumentationRegistry.getInstrumentation().context

        roomDatabase = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java // Use your actual database class
        ).allowMainThreadQueries().build()

        firebaseAuth = FirebaseModule.provideFirebaseAuth()
        firebaseAuth.useEmulator("10.0.2.2", 9099)

        firebaseFirestore = FirebaseModule.provideFirestore()
        try {
            firebaseFirestore.useEmulator("10.0.2.2", 8080)
        } catch (_: IllegalStateException) {
        }

        firebaseStorage = FirebaseModule.provideStorage()
        firebaseStorage.useEmulator("10.0.2.2", 9199)

        userDao = roomDatabase.userDao()

        blockedUserDao = roomDatabase.blockedUserDao()

        userLocalDataSource = FakeUserLocalDataSourceImpl(firebaseAuth, userDao)

        userRemoteDataSource =
            FakeUserRemoteDataSourceImpl(firebaseAuth, firebaseFirestore, firebaseStorage)

        userRepository = FakeUserRepositoryImpl(userLocalDataSource, userRemoteDataSource)

        val userData = mutableMapOf<String, Any>()
        val mediaItems = mutableMapOf<String, Uri>()

        userData["id"] = "John"
        userData["userName"] = "John"
        userData["displayName"] = "John"

        (userRepository as FakeUserRepositoryImpl).saveUserData(
            "John",
            userData,
            mediaItems,
            context
        )
    }

    @Test
    fun findById() {
        val blockedUser = BlockedUser("John", mutableListOf())
        blockedUserDao.upsert(blockedUser)
        val returnedBlockedUser = blockedUserDao.findById("John")
        assertTrue(blockedUser.blockedUserId == returnedBlockedUser.blockedUserId)
    }

    @Test
    fun upsert() {
        val blockedUser = BlockedUser("John", mutableListOf())
        blockedUserDao.upsert(blockedUser)
        val returnedBlockedUser = blockedUserDao.findById("John")
        assertTrue(blockedUser.blockedUserId == returnedBlockedUser.blockedUserId)
    }

    @Test
    fun update() {
        val blockedUser = BlockedUser("John", mutableListOf())
        blockedUserDao.update(blockedUser)
        val updatedBlockedUser = BlockedUser("John", mutableListOf("John"))
        blockedUserDao.upsert(updatedBlockedUser)
        val returnedBlockedUser = blockedUserDao.findById("John")
        assertTrue(returnedBlockedUser.blockedUsers.size > blockedUser.blockedUsers.size)
    }

    @Test
    fun delete() {
        val blockedUser = BlockedUser("John", mutableListOf())
        blockedUserDao.upsert(blockedUser)
        blockedUserDao.delete(blockedUser)
        /**Ignore this message saying that it's always false, it's not always false. They're wrong!**/
        assertTrue(blockedUserDao.findById("John") == null)
    }
}