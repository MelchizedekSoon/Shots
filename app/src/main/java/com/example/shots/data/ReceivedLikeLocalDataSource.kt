package com.example.shots.data

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class ReceivedLikeLocalDataSource @Inject constructor(
    val firebaseAuth: FirebaseAuth,
    val receivedLikeDao: ReceivedLikeDao
) {

    fun findById(receivedLikeId: String): ReceivedLike {
        return receivedLikeDao.findById(receivedLikeId)
    }

    //This allows for you to add multiple receivedLikes but manually one by one
    fun upsert(vararg receivedLikes: ReceivedLike) {
        for (receivedLike in receivedLikes)
            receivedLikeDao.upsert(receivedLike)
    }

    fun update(receivedLike: ReceivedLike) {
        receivedLikeDao.update(receivedLike)
    }

    fun delete(receivedLike: ReceivedLike) {
        receivedLikeDao.delete(receivedLike)
    }

}