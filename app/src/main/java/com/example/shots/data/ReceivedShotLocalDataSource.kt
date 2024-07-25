package com.example.shots.data

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class ReceivedShotLocalDataSource @Inject constructor(
    val firebaseAuth: FirebaseAuth,
    private val receivedShotDao: ReceivedShotDao
) {

    fun findById(receivedShotId: String): ReceivedShot {
        return receivedShotDao.findById(receivedShotId)
    }

    //This allows for you to add multiple receivedShots but manually one by one
    fun upsert(vararg receivedShots: ReceivedShot) {
        for (receivedShot in receivedShots)
            receivedShotDao.upsert(receivedShot)
    }

    fun update(receivedShot: ReceivedShot) {
        val verifiedReceivedShot = findById(receivedShot.receivedShotId)
        if (verifiedReceivedShot != null) {
            receivedShotDao.update(receivedShot)
        }
    }

    fun delete(receivedShotId: String) {
        val verifiedReceivedShot = findById(receivedShotId)
        if (verifiedReceivedShot != null) {
            receivedShotDao.delete(verifiedReceivedShot)
        }
    }

}