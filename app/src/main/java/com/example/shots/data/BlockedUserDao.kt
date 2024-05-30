package com.example.shots.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface BlockedUserDao {
    @Query("SELECT * FROM BlockedUser")
    fun getAll(): List<BlockedUser>

    @Query("DELETE FROM BlockedUser")
    fun nukeTable()

    @Query("SELECT * FROM blockedUser WHERE blockedUserId IN (:blockedUserIds)")
    fun loadAllByIds(blockedUserIds: IntArray): List<BlockedUser>

    @Query("SELECT * FROM blockedUser WHERE blockedUserId IN (:blockedUserId)")
    fun findById(blockedUserId: String): BlockedUser

    //This allows for you to add multiple blockedUsers but manually one by one
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg blockedUser: BlockedUser)

    //This allows you to add multiple blockedUsers by way of list
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(blockedUsers: List<BlockedUser>)

    @Delete
    fun delete(blockedUser: BlockedUser)

    @Update
    fun update(blockedUser: BlockedUser)

}