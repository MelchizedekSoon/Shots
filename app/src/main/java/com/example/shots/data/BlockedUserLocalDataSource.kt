package com.example.shots.data

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class BlockedUserLocalDataSource @Inject constructor(
    val firebaseAuth: FirebaseAuth,
    val blockedUserDao: BlockedUserDao
) {

    fun findById(blockedUserId: String): BlockedUser {
        return blockedUserDao.findById(blockedUserId)
    }

    //This allows for you to add multiple blockedUsers but manually one by one
    fun upsert(vararg blockedUsers: BlockedUser) {
        for (blockedUser in blockedUsers)
            blockedUserDao.upsert(blockedUser)
    }

    fun update(blockedUser: BlockedUser) {
        blockedUserDao.update(blockedUser)
    }

    fun delete(blockedUser: BlockedUser) {
        blockedUserDao.delete(blockedUser)
    }

}