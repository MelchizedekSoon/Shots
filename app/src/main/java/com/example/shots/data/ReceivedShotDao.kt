package com.example.shots.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ReceivedShotDao {
    @Query("SELECT * FROM ReceivedShot")
    fun getAll(): List<ReceivedShot>

    @Query("DELETE FROM ReceivedShot")
    fun nukeTable()

    @Query("SELECT * FROM receivedShot WHERE receivedShotId IN (:receivedShotIds)")
    fun loadAllByIds(receivedShotIds: IntArray): List<ReceivedShot>

    @Query("SELECT * FROM receivedShot WHERE receivedShotId IN (:receivedShotIds)")
    fun findById(receivedShotIds: String): ReceivedShot

    //This allows for you to add multiple bookmarks but manually one by one
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg receivedShot: ReceivedShot)

    //This allows you to add multiple bookmarks by way of list
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(receivedShots: List<ReceivedShot>)

    @Delete
    fun delete(receivedShot: ReceivedShot)

    @Update
    fun update(receivedShot: ReceivedShot)
}