package com.example.shots.data

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class UserWhoBlockedYouLocalDataSource @Inject constructor(
    val firebaseAuth: FirebaseAuth,
    val userWhoBlockedYouDao: UserWhoBlockedYouDao) {

    fun getAll(): List<UserWhoBlockedYou> {
        return userWhoBlockedYouDao.getAll()
    }

    fun loadAllByIds(userWhoBlockedYouIds: IntArray): List<UserWhoBlockedYou> {
        return userWhoBlockedYouDao.loadAllByIds(userWhoBlockedYouIds)
    }

    fun findById(userId: String): UserWhoBlockedYou {
        return userWhoBlockedYouDao.findById(userId)
    }

    //This allows for you to add multiple users but manually one by one
    fun upsert(vararg users: UserWhoBlockedYou) {
        for (user in users)
            userWhoBlockedYouDao.upsert(user)
    }

    //This allows you to add multiple users by way of list
    fun upsertAll(users: List<UserWhoBlockedYou>) {
        userWhoBlockedYouDao.upsertAll(users)
    }

    fun update(user: UserWhoBlockedYou) {
        userWhoBlockedYouDao.update(user)
    }

    fun delete(user: UserWhoBlockedYou) {
        userWhoBlockedYouDao.delete(user)
    }

}