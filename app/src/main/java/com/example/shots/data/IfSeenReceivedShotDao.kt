package com.example.shots.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert

@Dao
interface IfSeenReceivedShotDao {
    @Query("SELECT * FROM IfSeenReceivedShot")
    fun getAll(): List<IfSeenReceivedShot>

    @Query("DELETE FROM IfSeenReceivedShot")
    fun nukeTable()

    @Query("SELECT * FROM iFSeenReceivedShot WHERE ifSeenReceivedShotId IN (:ifSeenReceivedShotIds)")
    fun loadAllByIds(ifSeenReceivedShotIds: IntArray): List<IfSeenReceivedShot>

    @Query("SELECT * FROM iFSeenReceivedShot WHERE ifSeenReceivedShotId IN (:ifSeenReceivedShotIds)")
    fun findById(ifSeenReceivedShotIds: String): IfSeenReceivedShot

    //This allows for you to add multiple bookmarks but manually one by one
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Upsert
    fun upsert(vararg iFSeenReceivedShot: IfSeenReceivedShot)

    //This allows you to add multiple bookmarks by way of list
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Upsert
    fun upsertAll(iFSeenReceivedShots: List<IfSeenReceivedShot>)

    @Delete
    fun delete(iFSeenReceivedShot: IfSeenReceivedShot)

    @Update
    fun update(iFSeenReceivedShot: IfSeenReceivedShot)
}