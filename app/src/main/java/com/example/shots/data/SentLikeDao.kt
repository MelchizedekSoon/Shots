package com.example.shots.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface SentLikeDao {
    @Query("SELECT * FROM SentLike")
    fun getAll(): List<SentLike>

    @Query("DELETE FROM SentLike")
    fun nukeTable()

    @Query("SELECT * FROM sentLike WHERE sentLikeId IN (:sentLikeIds)")
    fun loadAllByIds(sentLikeIds: IntArray): List<SentLike>

    @Query("SELECT * FROM sentLike WHERE sentLikeId IN (:sentLikeIds)")
    fun findById(sentLikeIds: String): SentLike

    //This allows for you to add multiple bookmarks but manually one by one
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg sentLikes: SentLike)

    //This allows you to add multiple bookmarks by way of list
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(sentLikes: List<SentLike>)

    @Delete
    fun delete(sentLike: SentLike)

    @Update
    fun update(sentLike: SentLike)
}