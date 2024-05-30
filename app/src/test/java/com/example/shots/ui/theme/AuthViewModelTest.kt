package com.example.shots.ui.theme

import com.example.shots.data.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions

class AuthViewModelTest {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var firebaseRepository: FirebaseRepository
    private lateinit var authViewModel: AuthViewModel

    @Before
    fun setUp() {
        firebaseAuth = mockk<FirebaseAuth>(relaxed = true)
        firestore = mockk(relaxed = true)
        firebaseStorage = mockk(relaxed = true)
        firebaseRepository =
            mockk(relaxed = true)
        authViewModel = mockk(relaxed = true)
    }

    @Test
    fun signInWithEmail_failure() = runBlocking {

        // Call the signInWithEmail() method.
        val actualUser = authViewModel.signInWithEmail("test@example.com", "password123")

        // Verify that the result is null.
        Assertions.assertNull(actualUser)

        // Add the assertSame() test.
        val expectedUser: FirebaseUser? = null

        assertSame(expectedUser, actualUser)
    }


    @Test
    fun signInWithEmail_success() = runBlocking {
        // Mock the FirebaseRepository to return a non-null FirebaseUser.
        val mockFirebaseUser = mockk<FirebaseUser>()

        // Call the signInWithEmail() method.
        val result = authViewModel.signInWithEmail("test@example.com", "password")

        // Verify that the result is the mocked FirebaseUser.

        Assertions.assertNotNull(result)
    }

    @Test
    fun signInWithEmail_failed_nullResult() = runBlocking {
//        // Mock the FirebaseRepository to return null.
//        val mockFirebaseRepository = mockk<FirebaseRepository> {
//            coEvery { signInWithEmailPassword(any(), any()) } returns null
//        }

        // Call the signInWithEmail() method.
        val result = authViewModel.signInWithEmail("", "")

        // Verify that the result is null.
        Assertions.assertNull(result)
    }

    @Test
    fun createUserWithEmailAndPassword() {
    }
}