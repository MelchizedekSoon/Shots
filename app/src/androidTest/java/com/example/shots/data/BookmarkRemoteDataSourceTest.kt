package com.example.shots.data

import com.example.shots.FirebaseModule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BookmarkRemoteDataSourceTest {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var bookmarkRemoteDataSource: BookmarkRemoteDataSource

    @Before
    fun setUp() {
        firebaseAuth = FirebaseModule.provideFirebaseAuth()
        firebaseAuth.useEmulator("10.0.2.2", 9099)

        firebaseFirestore = FirebaseModule.provideFirestore()
        try {
            firebaseFirestore.useEmulator("10.0.2.2", 8080)
        } catch (_: IllegalStateException) {
        }

        bookmarkRemoteDataSource = FakeBookmarkRemoteDataSourceImpl(firebaseAuth, firebaseFirestore)
    }

    @Test
    fun getYourUserId() {
        assertEquals("John", bookmarkRemoteDataSource.getYourUserId())
    }

    @Test
    fun writeBookmarkToFirebase() = runTest {
        val bookmarkData = mutableMapOf<String, Any>()
        bookmarkData["bookmark-John"] = "John"
        bookmarkRemoteDataSource.writeBookmarkToFirebase("John", bookmarkData)
    }

    @Test
    fun getBookmarksFromFirebase() = runTest {
        val bookmarkData = mutableMapOf<String, Any>()
        bookmarkData["bookmark-John"] = "John"
        bookmarkRemoteDataSource.writeBookmarkToFirebase("John", bookmarkData)
        var bookmarks = bookmarkRemoteDataSource.getBookmarksFromFirebase().toMutableList()
        Assert.assertTrue(bookmarks.isNotEmpty())
    }

    @Test
    fun deleteBookmarkFromFirebase() = runTest {
        val bookmarkData = mutableMapOf<String, Any>()
        bookmarkData["bookmark-John"] = "John"
        bookmarkRemoteDataSource.deleteBookmarkFromFirebase("John")
        var bookmarks = bookmarkRemoteDataSource.getBookmarksFromFirebase().toMutableList()
        assertTrue(!bookmarks.contains("John"))
    }
}