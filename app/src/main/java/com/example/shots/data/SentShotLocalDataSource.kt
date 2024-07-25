package com.example.shots.data

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class SentShotLocalDataSource @Inject constructor(
    val firebaseAuth: FirebaseAuth,
    private val sentShotDao: SentShotDao) {

    fun findById(sentShotId: String): SentShot {
        return sentShotDao.findById(sentShotId)
    }

    //This allows for you to add multiple sentShots but manually one by one
    fun upsert(vararg sentShots: SentShot) {
        for (sentShot in sentShots)
            sentShotDao.upsert(sentShot)
    }

    fun update(sentShot: SentShot) {
        sentShotDao.update(sentShot)
    }

    fun delete(sentShot: SentShot) {
        sentShotDao.delete(sentShot)
    }

}