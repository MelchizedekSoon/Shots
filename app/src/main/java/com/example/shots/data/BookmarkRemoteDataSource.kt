package com.example.shots.data


interface BookmarkRemoteDataSource {

    fun getYourUserId(): String

    suspend fun writeBookmarkToFirebase(
        userId: String,
        bookmarkData: MutableMap<String, Any>,
    ): Boolean

    suspend fun getBookmarksFromFirebase(): List<String>

    suspend fun deleteBookmarkFromFirebase(userId: String?): Boolean

}