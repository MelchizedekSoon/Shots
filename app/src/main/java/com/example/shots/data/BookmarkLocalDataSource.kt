package com.example.shots.data

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BookmarkLocalDataSource @Inject constructor(
    val firebaseAuth: FirebaseAuth,
    val bookmarkDao: BookmarkDao
) {

    fun findById(bookmarkId: String): Bookmark {
        return bookmarkDao.findById(bookmarkId) // Assuming your DAO provides a Flow
    }

    //This allows for you to add multiple bookmarks but manually one by one
    fun upsert(vararg bookmarks: Bookmark) {
        for (bookmark in bookmarks)
            bookmarkDao.upsert(bookmark)
    }

    fun update(bookmark: Bookmark) {
        bookmarkDao.update(bookmark)
    }

    fun delete(bookmark: Bookmark) {
        bookmarkDao.delete(bookmark)
    }

}