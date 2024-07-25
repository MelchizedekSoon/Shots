package com.example.shots.data

import com.example.shots.FirebaseModule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BlockedUserRemoteDataSourceTest {

    private lateinit var blockedUserRemoteDataSource: BlockedUserRemoteDataSource
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    @Before
    fun setUp() {
        firebaseAuth = FirebaseModule.provideFirebaseAuth()
        firebaseAuth.useEmulator("10.0.2.2", 9099)

        firebaseFirestore = FirebaseModule.provideFirestore()
        try {
            firebaseFirestore.useEmulator("10.0.2.2", 8080)
        } catch (_: IllegalStateException) {
        }

        blockedUserRemoteDataSource =
            FakeBlockedUserRemoteDataSourceImpl(firebaseAuth, firebaseFirestore)
    }

    @Test
    fun getYourUserId() {
        assertEquals("John", blockedUserRemoteDataSource.getYourUserId())
    }

    @Test
    fun writeBlockedUserToFirebase() = runTest {
        val blockedUserData = mutableMapOf<String, Any>()
        blockedUserData["blockedUser-John"] = "John"
        blockedUserRemoteDataSource.writeBlockedUserToFirebase("John", blockedUserData)
        assertTrue(blockedUserRemoteDataSource.getBlockedUsersFromFirebase().contains("John"))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getBlockedUsersFromFirebase() = runTest {
        val blockedUserData = mutableMapOf<String, Any>()
        blockedUserData["blockedUser-John"] = "John"

        blockedUserRemoteDataSource.writeBlockedUserToFirebase("John", blockedUserData)

        advanceUntilIdle()

        val blockedUsers = blockedUserRemoteDataSource.getBlockedUsersFromFirebase()

        advanceUntilIdle()

        assertTrue(blockedUsers.contains("John"))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteBlockedUserFromFirebase() = runTest {
        val blockedUserData = mutableMapOf<String, Any>()
        blockedUserData["blockedUser-John"] = "John"
        blockedUserRemoteDataSource.writeBlockedUserToFirebase("John", blockedUserData)

        advanceUntilIdle()

        val blockedUsers = blockedUserRemoteDataSource.getBlockedUsersFromFirebase()

        advanceUntilIdle()

        blockedUserRemoteDataSource.deleteBlockedUserFromFirebase("John")

        advanceUntilIdle()

        val updatedBlockedUsers = blockedUserRemoteDataSource.getBlockedUsersFromFirebase()
        assertTrue(blockedUsers.size > updatedBlockedUsers.size)
    }
}