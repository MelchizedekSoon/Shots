package com.example.shots.data

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class IfSeenReceivedShotLocalDataSource @Inject constructor(
    val firebaseAuth: FirebaseAuth,
    private val ifSeenReceivedShotDao: IfSeenReceivedShotDao
) {

    fun findById(ifSeenReceivedShotId: String): IfSeenReceivedShot {
        return ifSeenReceivedShotDao.findById(ifSeenReceivedShotId)
    }

    //This allows for you to add multiple receivedShots but manually one by one
    fun upsert(vararg ifSeenReceivedShots: IfSeenReceivedShot) {
        for (ifSeenReceivedShot in ifSeenReceivedShots)
            ifSeenReceivedShotDao.upsert(ifSeenReceivedShot)
    }

    fun update(ifSeenReceivedShot: IfSeenReceivedShot) {
        ifSeenReceivedShotDao.update(ifSeenReceivedShot)
    }

    fun delete(ifSeenReceivedShot: IfSeenReceivedShot) {
        ifSeenReceivedShotDao.delete(ifSeenReceivedShot)
    }

}