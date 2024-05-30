package com.example.shots.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

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
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg bookmarks: Bookmark)

    //This allows you to add multiple bookmarks by way of list
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(bookmarks: List<Bookmark>)

    @Delete
    fun delete(bookmark: Bookmark)

    @Update
    fun update(bookmark: Bookmark)

}