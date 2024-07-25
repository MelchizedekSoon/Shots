package com.example.shots.data

import com.google.firebase.auth.FirebaseAuth

class SentLikeLocalDataSource(val firebaseAuth: FirebaseAuth,
                              val sentLikeDao: SentLikeDao) {

    fun findById(sentLikeId: String): SentLike {
        return sentLikeDao.findById(sentLikeId)
    }

    //This allows for you to add multiple sentLikes but manually one by one
    fun upsert(vararg sentLikes: SentLike) {
        for (sentLike in sentLikes)
            sentLikeDao.upsert(sentLike)
    }

    fun update(sentLike: SentLike) {
        sentLikeDao.update(sentLike)
    }

    fun delete(sentLike: SentLike) {
        sentLikeDao.delete(sentLike)
    }

}