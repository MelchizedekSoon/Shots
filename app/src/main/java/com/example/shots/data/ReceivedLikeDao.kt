package com.example.shots.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ReceivedLikeDao {
    @Query("SELECT * FROM ReceivedLike")
    fun getAll(): List<ReceivedLike>

    @Query("DELETE FROM ReceivedLike")
    fun nukeTable()

    @Query("SELECT * FROM receivedlike WHERE receivedLikeId IN (:receivedLikeIds)")
    fun loadAllByIds(receivedLikeIds: IntArray): List<ReceivedLike>

    @Query("SELECT * FROM receivedlike WHERE receivedLikeId IN (:receivedLikeIds)")
    fun findById(receivedLikeIds: String): ReceivedLike

    //This allows for you to add multiple bookmarks but manually one by one
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg receivedLike: ReceivedLike)

    //This allows you to add multiple bookmarks by way of list
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(receivedLikes: List<ReceivedLike>)

    @Delete
    fun delete(receivedLike: ReceivedLike)

    @Update
    fun update(receivedLike: ReceivedLike)
}