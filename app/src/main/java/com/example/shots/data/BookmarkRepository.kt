package com.example.shots.data

import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {

    fun getYourUserId(): String

    suspend fun saveAndStoreBookmark(
        bookmarkId: String,
        bookmarkData: MutableMap<String, Any>
    )

    suspend fun storeBookmarkObject(bookmark: Bookmark)

    suspend fun storeBookmark(bookmarkId: String)

    fun getBookmark(userId: String): Bookmark

    fun getBookmarks(): Flow<List<String>>

    fun fetchBookmarks(): Flow<List<String>>

    fun fetchUpdatedBookmarks(): Flow<List<String>>

    suspend fun removeBookmark(bookmarkId: String)

}