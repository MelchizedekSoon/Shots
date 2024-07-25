package com.example.shots.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FakeSentLikeRepositoryImpl @Inject constructor(
    private val sentLikeLocalDataSource: SentLikeLocalDataSource,
    private val sentLikeRemoteDataSource: SentLikeRemoteDataSource
) : SentLikeRepository {

    override fun getYourUserId(): String {
        return "John"
    }

    override suspend fun saveAndStoreSentLike(
        sentLikeId: String,
        sentLikeData: MutableMap<String, Any>
    ) {
        val success =
            sentLikeRemoteDataSource.writeSentLikeToFirebase(sentLikeId, sentLikeData)
        if (success) {
            val sentLikeList =
                sentLikeRemoteDataSource.getSentLikesFromFirebase(sentLikeId)
                    .toMutableList()
            try {
                var sentLike = sentLikeLocalDataSource.findById(getYourUserId())
                sentLike = sentLike.copy(
                    sentLikeId = getYourUserId(),
                    sentLikes = sentLikeList
                )
                sentLikeLocalDataSource.upsert(sentLike)
            } catch (npe: java.lang.NullPointerException) {
                val sentLike =
                    SentLike(getYourUserId(), sentLikeList.toMutableList())
                Log.d("SentLikeRepositoryImpl", "sentLike was stored!")
                try {
                    sentLikeLocalDataSource.upsert(sentLike)
                } catch (e: Exception) {
                    Log.d("SentLikeRepositoryImpl", "sentLike failed to be stored!")
                }
            }
        }
    }


    override suspend fun storeSentLike(sentLike: SentLike) {
        sentLikeLocalDataSource.upsert(sentLike)
    }

    override fun getSentLike(userId: String): SentLike {
        return sentLikeLocalDataSource.findById(getYourUserId())
    }

    override suspend fun removeSentLike(sentLikeId: String) {
        val success =
            sentLikeRemoteDataSource.removeSentLikeFromFirebase(sentLikeId)
        if (success) {
            val sentLikeList =
                sentLikeRemoteDataSource.getSentLikesFromFirebase(sentLikeId)
                    .toMutableList()
            try {
                var sentLike = sentLikeLocalDataSource.findById(getYourUserId())
                sentLike = sentLike.copy(
                    sentLikeId = getYourUserId(),
                    sentLikes = sentLikeList
                )
                sentLikeLocalDataSource.upsert(sentLike)
            } catch (npe: java.lang.NullPointerException) {
                val sentLike =
                    SentLike(getYourUserId(), sentLikeList.toMutableList())
                Log.d("SentLikeRepositoryImpl", "sentLike was stored!")
                try {
                    sentLikeLocalDataSource.upsert(sentLike)
                } catch (e: Exception) {
                    Log.d("SentLikeRepositoryImpl", "sentLike failed to be stored!")
                }
            }
        }
    }

    override suspend fun fetchUpdatedSentLikes(): Flow<List<String>> {
        val sentLikes = sentLikeRemoteDataSource.getSentLikesFromFirebase(getYourUserId())
        return flow { emit(sentLikes) }
    }

}