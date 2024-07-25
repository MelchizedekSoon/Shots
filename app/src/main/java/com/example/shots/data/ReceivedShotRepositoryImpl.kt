package com.example.shots.data

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ReceivedShotRepositoryImpl @Inject
constructor(
    private val receivedShotLocalDataSource: ReceivedShotLocalDataSource,
    private val receivedShotRemoteDataSource: ReceivedShotRemoteDataSource
) : ReceivedShotRepository {

    override fun getYourUserId(): String {
        return receivedShotRemoteDataSource.getYourUserId()
    }

    override suspend fun saveReceivedShot(
        receivedShotId: String,
        receivedShotData: MutableMap<String, Uri>,
        context: Context
    ) {
        val success =
            receivedShotRemoteDataSource.writeReceivedShotToFirebase(
                receivedShotId,
                receivedShotData,
                context
            )
        if (success) {
            val receivedShotList =
                receivedShotRemoteDataSource.getReceivedShotsFromFirebase()
                    .toMutableList()
            try {
                var receivedShot = receivedShotLocalDataSource.findById(getYourUserId())
                receivedShot = receivedShot.copy(
                    receivedShotId = getYourUserId(),
                    receivedShots = receivedShotList
                )
                receivedShotLocalDataSource.upsert(receivedShot)
            } catch (npe: java.lang.NullPointerException) {
                val receivedShot =
                    ReceivedShot(getYourUserId(), receivedShotList.toMutableList())
                Log.d("ReceivedShotRepositoryImpl", "receivedShot was stored!")
                try {
                    receivedShotLocalDataSource.upsert(receivedShot)
                } catch (e: Exception) {
                    Log.d("ReceivedShotRepositoryImpl", "receivedShot failed to be stored!")
                }
            }
        }
    }

    override suspend fun storeReceivedShot(receivedShot: ReceivedShot) {
        receivedShotLocalDataSource.upsert(receivedShot)
    }

    override fun getReceivedShot(userId: String): ReceivedShot {
        return receivedShotLocalDataSource.findById(getYourUserId())
    }

    override suspend fun removeReceivedShot(userId: String): Boolean {
        val success = receivedShotRemoteDataSource.removeReceivedShotFromFirebase(userId)
        if (success) {
            val receivedShotList =
                receivedShotRemoteDataSource.getReceivedShotsFromFirebase()
                    .toMutableList()
            try {
                var receivedShot = receivedShotLocalDataSource.findById(getYourUserId())
                receivedShot = receivedShot.copy(
                    receivedShotId = getYourUserId(),
                    receivedShots = receivedShotList
                )
                receivedShotLocalDataSource.upsert(receivedShot)
                return true
            } catch (npe: java.lang.NullPointerException) {
                val receivedShot =
                    ReceivedShot(getYourUserId(), receivedShotList.toMutableList())
                Log.d("ReceivedShotRepositoryImpl", "receivedShot was stored!")
                try {
                    receivedShotLocalDataSource.upsert(receivedShot)
                    return true
                } catch (e: Exception) {
                    Log.d("ReceivedShotRepositoryImpl", "receivedShot failed to be stored!")
                    return false
                }
            }
        }
        return false
    }

    override suspend fun removeTheirReceivedShotFromFirebase(userId: String?): Boolean {
        val success = receivedShotRemoteDataSource.removeTheirReceivedShotFromFirebase(userId)
        if (success) {
            val receivedShotList =
                receivedShotRemoteDataSource.getReceivedShotsFromFirebase()
                    .toMutableList()
            try {
                var receivedShot = receivedShotLocalDataSource.findById(userId ?: "")
                receivedShot = receivedShot.copy(
                    receivedShotId = userId ?: "",
                    receivedShots = receivedShotList
                )
                receivedShotLocalDataSource.upsert(receivedShot)
                return true
            } catch (npe: java.lang.NullPointerException) {
                val receivedShot =
                    ReceivedShot(userId ?: "", receivedShotList.toMutableList())
                Log.d("ReceivedShotRepositoryImpl", "receivedShot was stored!")
                try {
                    receivedShotLocalDataSource.upsert(receivedShot)
                    return true
                } catch (e: Exception) {
                    Log.d("ReceivedShotRepositoryImpl", "receivedShot failed to be stored!")
                    return false
                }
            }
        }
        return false
    }

    override suspend fun getUpdatedReceivedShots(): Flow<List<String>> {
        val receivedShots = receivedShotLocalDataSource.findById(getYourUserId())
        return flow { emit(receivedShots.receivedShots) }
    }

    override suspend fun fetchUpdatedReceivedShots(): Flow<List<String>> {
        val receivedShots = receivedShotRemoteDataSource.getReceivedShotsFromFirebase()
        val receivedShot = ReceivedShot(getYourUserId(), receivedShots.toMutableList())
        Log.d("ReceivedShotRepositoryImpl", "receivedShot = $receivedShot")
        Log.d("ReceivedShotRepositoryImpl", "receivedShot = ${receivedShot.receivedShots}")
        receivedShotLocalDataSource.upsert(receivedShot)
        val storedReceivedShots = receivedShotLocalDataSource.findById(getYourUserId())
        return flow { emit(storedReceivedShots.receivedShots) }
    }

}