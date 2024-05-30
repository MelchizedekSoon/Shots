package com.example.shots.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface SentShotDao {
    @Query("SELECT * FROM SentShot")
    fun getAll(): List<SentShot>

    @Query("DELETE FROM SentShot")
    fun nukeTable()

    @Query("SELECT * FROM sentShot WHERE sentShotId IN (:sentShotIds)")
    fun loadAllByIds(sentShotIds: IntArray): List<SentShot>

    @Query("SELECT * FROM sentShot WHERE sentShotId IN (:sentShotIds)")
    fun findById(sentShotIds: String): SentShot

    //This allows for you to add multiple bookmarks but manually one by one
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg sentShots: SentShot)

    //This allows you to add multiple bookmarks by way of list
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(sentShots: List<SentShot>)

    @Delete
    fun delete(sentShot: SentShot)

    @Update
    fun update(sentShot: SentShot)
}