package com.example.shots.data

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class FakeUserLocalDataSourceImpl @Inject constructor(
    val firebaseAuth: FirebaseAuth,
    val userDao: UserDao
) : UserLocalDataSource {

    override fun getAll(): List<User> {
        return userDao.getAll()
    }

    override fun loadAllByIds(userIds: IntArray): List<User> {
        return userDao.loadAllByIds(userIds)
    }

    override fun findByName(userName: String): User {
        return userDao.findByName(userName)
    }

    override fun findByYourId(): User {
        return userDao.findById("John")
    }

    override fun findById(userId: String): User {
        return userDao.findById(userId)
    }

    //This allows for you to add multiple users but manually one by one
    override fun upsert(vararg users: User) {
        for (user in users)
            userDao.upsert(user)
    }

    //This allows you to add multiple users by way of list
    override fun upsertAll(users: List<User>) {
        userDao.upsertAll(users)
    }

    override fun update(user: User) {
        userDao.update(user)
    }

    override fun updateAll(users: List<User>) {
        userDao.updateAll(users)
    }

    override fun delete(user: User) {
        userDao.delete(user)
    }

}