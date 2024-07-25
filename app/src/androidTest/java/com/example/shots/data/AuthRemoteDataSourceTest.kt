package com.example.shots.data

import com.example.shots.FirebaseModule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class AuthRemoteDataSourceTest {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    @Before
    fun setUp() = runTest {
        firebaseAuth = FirebaseModule.provideFirebaseAuth()
        firebaseAuth.useEmulator("10.0.2.2", 9099)

        firebaseFirestore = FirebaseModule.provideFirestore()
        try {
            firebaseFirestore.useEmulator("10.0.2.2", 8080)
        } catch (_: IllegalStateException) {
        }
        val settings = FirebaseFirestoreSettings.Builder()
            .setHost("10.0.2.2:8080") // Use 10.0.2.2 for Android emulators
            .setSslEnabled(false).build()
        firebaseFirestore.firestoreSettings = settings

        firebaseAuth.createUserWithEmailAndPassword("john@gmail.com", "johnjohn")
            .await()

    }

    @Test
    fun signInWithEmailAndPassword() = runTest {
        val user =
            firebaseAuth.signInWithEmailAndPassword("john@gmail.com", "johnjohn").await().user
        Assert.assertTrue(user != null)
        user?.delete()?.await()
    }

    @Test
    fun createUserWithEmailAndPassword() = runTest {
        val user =
            firebaseAuth.signInWithEmailAndPassword("john@gmail.com", "johnjohn").await().user
        Assert.assertTrue(user != null)
        user?.delete()?.await()
    }

}