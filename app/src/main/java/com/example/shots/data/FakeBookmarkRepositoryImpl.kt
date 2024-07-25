package com.example.shots.data

import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FakeBookmarkRepositoryImpl @Inject
constructor(
    private val bookmarkLocalDataSource: BookmarkLocalDataSource,
    private val bookmarkRemoteDataSource: BookmarkRemoteDataSource,
    private val dispatcher: CoroutineDispatcher
) : BookmarkRepository {

    override fun getYourUserId(): String {
        return "John"
    }

    override suspend fun saveAndStoreBookmark(
        bookmarkId: String,
        bookmarkData: MutableMap<String, Any>
    ) {
        Log.d("FakeBookmarkRepositoryImpl", "inside save and store where bookmarkId is $bookmarkId")
        Log.d(
            "FakeBookmarkRepositoryImpl",
            "inside save and store where bookmarkData is $bookmarkData"
        )
        val success =
            bookmarkRemoteDataSource.writeBookmarkToFirebase(
                bookmarkId,
                bookmarkData
            )
        Log.d("FakeBookmarkRepositoryImpl", "success = $success")
        if (success) {
            val bookmarkList =
                bookmarkRemoteDataSource.getBookmarksFromFirebase()
                    .toMutableList()
            try {
                val bookmark = Bookmark(getYourUserId(), bookmarkList)
                bookmarkLocalDataSource.bookmarkDao.upsert(bookmark)
                Log.d("FakeBookmarkRepositoryImpl", "bookmark was stored! = ${bookmark.bookmarks}")
            } catch (npe: java.lang.NullPointerException) {
                val bookmark =
                    Bookmark(getYourUserId(), bookmarkList.toMutableList())
                Log.d("FakeBookmarkRepositoryImpl", "bookmark was stored! = ${bookmark.bookmarks}")
                try {
                    bookmarkLocalDataSource.upsert(bookmark)
                } catch (e: Exception) {
                    Log.d("FakeBookmarkRepositoryImpl", "bookmark failed to be stored!")
                }
            }
        }
    }

    override suspend fun removeBookmark(bookmarkId: String) {
        bookmarkRemoteDataSource.deleteBookmarkFromFirebase(bookmarkId)
        val bookmarks = bookmarkRemoteDataSource.getBookmarksFromFirebase()
        val bookmark = Bookmark(getYourUserId(), bookmarks.toMutableList())
        Log.d("FakeBookmarkRepositoryImpl", "inside removeBookmark, bookmark = ${bookmark}")
        bookmarkLocalDataSource.upsert(bookmark)
    }

    override suspend fun storeBookmarkObject(bookmark: Bookmark) {
        bookmarkLocalDataSource.upsert(bookmark)
        Log.d(
            "FakeBookmarkRepositoryImpl",
            "all bookmarks = ${bookmarkLocalDataSource.bookmarkDao.getAll()}"
        )
    }

    override suspend fun storeBookmark(bookmarkId: String) {
        val bookmark = bookmarkLocalDataSource.findById(bookmarkId)
        bookmarkLocalDataSource.upsert(bookmark)
    }

    override fun getBookmark(userId: String): Bookmark {
        return bookmarkLocalDataSource.findById(getYourUserId())
    }

    override fun getBookmarks(): Flow<List<String>> {
//        Log.d(
//            "FakeBookmarkRepositoryImpl",
//            "yourUserId in bookmarkRepositoryImpl is ${getYourUserId()}"
//        )
//        var bookmark = bookmarkLocalDataSource.findById(getYourUserId())
//        Log.d(
//            "FakeBookmarkRepositoryImpl", "bookmark in bookmarkRepositoryImpl is " +
//                    "$bookmark"
//        )
//        if (bookmark == null) {
//            var bookmarks = bookmarkRemoteDataSource.getBookmarksFromFirebase().toMutableList()
//            bookmark = Bookmark(getYourUserId(), bookmarks)
//            bookmarkLocalDataSource.upsert(bookmark)
//        }
//        var bookmarkList = bookmark.bookmarks
//        Log.d("FakeBookmarkRepositoryImpl", "bookmarkList is $bookmarkList in getBookmarks")
        return flow { emit(bookmarkRemoteDataSource.getBookmarksFromFirebase().toMutableList()) }
    }

    override fun fetchUpdatedBookmarks(): Flow<List<String>> {
        return flow { emit(bookmarkRemoteDataSource.getBookmarksFromFirebase()) }
    }

    override fun fetchBookmarks(): Flow<List<String>> {
        return flow { emit(bookmarkRemoteDataSource.getBookmarksFromFirebase()) }
    }

}