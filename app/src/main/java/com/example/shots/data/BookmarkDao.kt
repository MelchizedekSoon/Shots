package com.example.shots.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM Bookmark")
    fun getAll(): List<Bookmark>

    @Query("DELETE FROM Bookmark")
    fun nukeTable()

    @Query("SELECT * FROM bookmark WHERE bookmarkId IN (:bookmarkIds)")
    fun loadAllByIds(bookmarkIds: IntArray): List<Bookmark>

    @Query("SELECT * FROM bookmark WHERE bookmarkId IN (:bookmarkId)")
    fun findById(bookmarkId: String): Bookmark

    //This allows for you to add multiple bookmarks but manually one by one
    @Upsert
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(vararg bookmarks: Bookmark)

    //This allows you to add multiple bookmarks by way of list
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Upsert
    fun upsertAll(bookmarks: List<Bookmark>)

    @Delete
    fun delete(bookmark: Bookmark)

    @Update
    fun update(bookmark: Bookmark)

}