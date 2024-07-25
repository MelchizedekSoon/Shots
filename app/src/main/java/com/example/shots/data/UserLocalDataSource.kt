package com.example.shots.data

interface UserLocalDataSource {

    fun getAll(): List<User>

    fun loadAllByIds(userIds: IntArray): List<User>

    fun findByName(userName: String): User

    fun findByYourId(): User

    fun findById(userId: String): User

    //This allows for you to add multiple users but manually one by one
    fun upsert(vararg users: User)

    //This allows you to add multiple users by way of list
    fun upsertAll(users: List<User>)

    fun update(user: User)

    fun updateAll(users: List<User>)

    fun delete(user: User)
}