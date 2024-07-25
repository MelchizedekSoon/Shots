package com.example.shots.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert

@Dao
interface UserDao {
    @Query("SELECT * FROM User")
    fun getAll(): List<User>

    @Query("DELETE FROM User")
    fun nukeTable()

    @Query("SELECT * FROM user WHERE id IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<User>

    @Query("SELECT * FROM user WHERE userName LIKE :userName LIMIT 1")
    fun findByName(userName: String): User

    @Query("SELECT * FROM user WHERE id IN (:userId)")
    fun findById(userId: String): User

    //This allows for you to add multiple users but manually one by one
    @Upsert
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(vararg users: User)

    //This allows you to add multiple users by way of list
    @Upsert
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(users: List<User>)

    @Update
    fun update(user: User)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateAll(users: List<User>)

    @Delete
    fun delete(user: User)
}