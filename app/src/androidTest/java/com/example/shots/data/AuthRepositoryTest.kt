package com.example.shots.data

import com.example.shots.FirebaseModule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*

import org.junit.Before
import org.junit.Test

class AuthRepositoryTest {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var authRemoteDataSource: AuthRemoteDataSource
    private lateinit var authRepository: AuthRepository

    @Before
    fun setUp() {
        firebaseAuth = FirebaseModule.provideFirebaseAuth()
        firebaseAuth.useEmulator("10.0.2.2", 9099)

        firebaseFirestore = FirebaseModule.provideFirestore()
        try {
            firebaseFirestore.useEmulator("10.0.2.2", 8080)
        } catch (_: IllegalStateException) {
        }

        authRemoteDataSource = AuthRemoteDataSource(firebaseAuth, firebaseFirestore)
        authRepository = AuthRepositoryImpl(authRemoteDataSource)
    }

    @Test
    fun createUserWithEmailAndPassword() = runTest {
        val user = authRepository.createUserWithEmailAndPassword("john@gmail.com", "johnjohn")
        assertTrue(user != null)
        user?.delete()?.await()
    }

    @Test
    fun signInWithEmailPassword() = runTest {
        authRepository.createUserWithEmailAndPassword("john@gmail.com", "johnjohn")
        val user = authRepository.signInWithEmailPassword("john@gmail.com", "johnjohn")
        assertTrue(user != null)
        user?.user?.delete()?.await()
    }

}