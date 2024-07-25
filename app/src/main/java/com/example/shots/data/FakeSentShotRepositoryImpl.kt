package com.example.shots.data

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FakeSentShotRepositoryImpl @Inject constructor(
    private val sentShotLocalDataSource: SentShotLocalDataSource,
    private val sentShotRemoteDataSource: SentShotRemoteDataSource
) : SentShotRepository {

    override fun getYourUserId(): String {
        return "John"
    }


    override suspend fun saveSentShot(
        sentShotId: String,
        sentShotData: MutableMap<String, Uri>,
        context: Context
    ) {
        val success =
            sentShotRemoteDataSource.writeSentShotToFirebase(sentShotId, sentShotData, context)
        if (success) {
            val sentShotList =
                sentShotRemoteDataSource.getSentShotsFromFirebase()
                    .toMutableList()
            try {
                var sentShot = sentShotLocalDataSource.findById(getYourUserId())
                sentShot = sentShot.copy(
                    sentShotId = getYourUserId(),
                    sentShots = sentShotList
                )
                Log.d("SentShotRepositoryImpl", "sentShot was stored! - $sentShot")
                sentShotLocalDataSource.upsert(sentShot)
            } catch (npe: java.lang.NullPointerException) {
                val sentShot =
                    SentShot(getYourUserId(), sentShotList.toMutableList())
                Log.d("SentShotRepositoryImpl", "sentShot was stored! - $sentShot")
                try {
                    sentShotLocalDataSource.upsert(sentShot)
                } catch (e: Exception) {
                    Log.d("SentShotRepositoryImpl", "sentShot failed to be stored!")
                }
            }
        }
    }

    override suspend fun storeSentShot(sentShot: SentShot) {
        sentShotLocalDataSource.upsert(sentShot)
    }

    override fun getSentShot(userId: String): SentShot {
        return sentShotLocalDataSource.findById(getYourUserId())
    }

    override suspend fun removeSentShot(userId: String) {
        val success = sentShotRemoteDataSource.removeSentShotFromFirebase(userId)
        if (success) {
            val sentShotList =
                sentShotRemoteDataSource.getSentShotsFromFirebase()
                    .toMutableList()
            try {
                var sentShot = sentShotLocalDataSource.findById(getYourUserId())
                sentShot = sentShot.copy(
                    sentShotId = getYourUserId(),
                    sentShots = sentShotList
                )
                sentShotLocalDataSource.upsert(sentShot)
            } catch (npe: java.lang.NullPointerException) {
                val sentShot =
                    SentShot(getYourUserId(), sentShotList.toMutableList())
                Log.d("SentShotRepositoryImpl", "sentShot was stored!")
                try {
                    sentShotLocalDataSource.upsert(sentShot)
                } catch (e: Exception) {
                    Log.d("SentShotRepositoryImpl", "sentShot failed to be stored!")
                }
            }
        }
    }


    override suspend fun getUpdatedSentShots(): Flow<List<String>> {
        val sentShots = sentShotLocalDataSource.findById(getYourUserId())
        return flow {emit(sentShots.sentShots)}
    }

    override suspend fun fetchUpdatedSentShots(): Flow<List<String>> {
        val sentShots = sentShotRemoteDataSource.getSentShotsFromFirebase()
        return flow {emit(sentShots)}
    }

}