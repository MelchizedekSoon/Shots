package com.example.shots.data

import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class BookmarkRepositoryImpl @Inject
constructor(
    private val bookmarkLocalDataSource: BookmarkLocalDataSource,
    private val bookmarkRemoteDataSource: BookmarkRemoteDataSource,
    private val dispatcher: CoroutineDispatcher
) : BookmarkRepository {

    override fun getYourUserId(): String {
        return bookmarkRemoteDataSource.getYourUserId()
    }

    override suspend fun saveAndStoreBookmark(
        bookmarkId: String,
        bookmarkData: MutableMap<String, Any>
    ) {
        val success =
            bookmarkRemoteDataSource.writeBookmarkToFirebase(
                bookmarkId,
                bookmarkData
            )
        if (success) {
            var bookmarkList = bookmarkRemoteDataSource.getBookmarksFromFirebase().toMutableList()
            try {
                var bookmark = bookmarkLocalDataSource.findById(getYourUserId())
                bookmark = bookmark.copy(
                    bookmarkId = getYourUserId(),
                    bookmarks = bookmarkList
                )
                bookmarkLocalDataSource.upsert(bookmark)
            } catch (npe: java.lang.NullPointerException) {
                val bookmark =
                    Bookmark(getYourUserId(), bookmarkList.toMutableList())
                Log.d("BookmarkRepositoryImpl", "bookmark was stored!")
                try {
                    bookmarkLocalDataSource.upsert(bookmark)
                } catch (e: Exception) {
                    Log.d("BookmarkRepositoryImpl", "bookmark failed to be stored!")
                }
            }
        }
    }

    override suspend fun removeBookmark(bookmarkId: String) {
        bookmarkRemoteDataSource.deleteBookmarkFromFirebase(bookmarkId)
        var bookmarks = bookmarkRemoteDataSource.getBookmarksFromFirebase().toMutableList()
        val bookmark = Bookmark(bookmarkId, bookmarks.toMutableList())
        bookmarkLocalDataSource.upsert(bookmark)
    }

    override suspend fun storeBookmarkObject(bookmark: Bookmark) {
        bookmarkLocalDataSource.upsert(bookmark)
    }

    override suspend fun storeBookmark(bookmarkId: String) {
        val bookmark = bookmarkLocalDataSource.findById(bookmarkId)
        bookmarkLocalDataSource.upsert(bookmark)
    }

    override fun getBookmark(userId: String): Bookmark {
        return bookmarkLocalDataSource.findById(getYourUserId())
    }

    override fun getBookmarks(): Flow<List<String>> {
        Log.d(
            "BookmarkRepositoryImpl",
            "yourUserId in bookmarkRepositoryImpl is ${getYourUserId()}"
        )
        val bookmarkList = bookmarkLocalDataSource.findById(getYourUserId()).bookmarks
        return flow { emit(bookmarkList) }
    }

    override fun fetchUpdatedBookmarks(): Flow<List<String>> {
        return flow { emit(bookmarkRemoteDataSource.getBookmarksFromFirebase()) }
    }

    override fun fetchBookmarks(): Flow<List<String>> {
        return flow { emit(bookmarkRemoteDataSource.getBookmarksFromFirebase()) }
    }

}