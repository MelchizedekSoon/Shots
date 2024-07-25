package com.example.shots.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert

@Dao
interface UserWhoBlockedYouDao {
    @Query("SELECT * FROM UserWhoBlockedYou")
    fun getAll(): List<UserWhoBlockedYou>

    @Query("DELETE FROM UserWhoBlockedYou")
    fun nukeTable()

    @Query("SELECT * FROM userWhoBlockedYou WHERE userWhoBlockedYouId IN (:userWhoBlockedYouIds)")
    fun loadAllByIds(userWhoBlockedYouIds: IntArray): List<UserWhoBlockedYou>

    @Query("SELECT * FROM userWhoBlockedYou WHERE userWhoBlockedYouId IN (:userWhoBlockedYouId)")
    fun findById(userWhoBlockedYouId: String): UserWhoBlockedYou

    //This allows for you to add multiple blockedUsers but manually one by one
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Upsert
    fun upsert(vararg users: UserWhoBlockedYou)

    //This allows you to add multiple blockedUsers by way of list
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Upsert
    fun upsertAll(userWhoBlockedYou: List<UserWhoBlockedYou>)

    @Delete
    fun delete(userWhoBlockedYou: UserWhoBlockedYou)

    @Update
    fun update(userWhoBlockedYou: UserWhoBlockedYou)

}