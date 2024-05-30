package com.example.shots.ui.theme

import com.example.shots.RoomModule
import com.example.shots.data.AppDatabase
import com.example.shots.data.Bookmark
import com.example.shots.data.BookmarkDao
import com.example.shots.data.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*

import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class BookmarkViewModelTest {
    private lateinit var firebaseRepository: FirebaseRepository
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var appDatabase: AppDatabase
    private lateinit var bookmarkDao: BookmarkDao
    private lateinit var bookmarkViewModel: BookmarkViewModel
    private lateinit var usersViewModel: UsersViewModel
    private lateinit var user: User

    @Before
    fun setUp() {
        appDatabase = mockk(relaxed = true)
        firebaseRepository = mockk(relaxed = true)
        firebaseAuth = mockk(relaxed = true)
        bookmarkViewModel = BookmarkViewModel(firebaseRepository, firebaseAuth, appDatabase)
        bookmarkDao = RoomModule.provideBookmarkDao(appDatabase)
    }

    /** I'm still learning tests but Gemini
     * told me that this test is basically useless
     * cause "the test is not very useful. This is because the
     * provideBookmarkDao() method of the RoomModule is simply a wrapper around the bookmarkDao
     * property of the appDatabase. Therefore, the test is essentially checking
     * that the bookmarkDao property of the BookmarkViewModel is equal to itself."
     *
     * She told me to add the null test under this one
     */

    @Test
    fun getBookmarkDao() {
        val expectedDao = RoomModule.provideBookmarkDao(appDatabase)
        val actualDao = bookmarkViewModel.bookmarkDao

        assertEquals(expectedDao, actualDao)
    }

    @Test
    fun getBookmarkDao_isNotNull() {
        assertNotNull(bookmarkViewModel.bookmarkDao)
    }

    @Test
    fun fetchBookmarkFromRoom_whenBookmarkDoesNotExist_returnsEmptyBookmark() {
        // Mock the AppDatabase object
        val mockAppDatabase = mockk<AppDatabase>()

        // Mock the BookmarkDao object
        val mockBookmarkDao = mockk<BookmarkDao>()

        // Set up the mock BookmarkDao object to return null when findById() is called
//        every { mockBookmarkDao.findById(any()) } returns null

        // Set up the mock AppDatabase object to return the mock BookmarkDao object when bookmarkDao() is called
        every { mockAppDatabase.bookmarkDao() } returns mockBookmarkDao

        // Create a BookmarkViewModel object using the mock AppDatabase object
        val bookmarkViewModel = BookmarkViewModel(firebaseRepository, firebaseAuth, mockAppDatabase)

        // Call the fetchBookmarkFromRoom() method
        val bookmark = bookmarkDao.findById(user.id)

        // Assert that the returned bookmark is an empty bookmark
        assertEquals(Bookmark("bookmarkId", mutableListOf()), bookmark)
    }

    @Test
    fun fetchBookmarkFromRoom() {
    }

    @Test
    fun storeBookmarkInRoom() {
    }

    @Test
    fun convertBookmarksToList() {
    }
}