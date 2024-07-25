package com.example.shots.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FakeReceivedLikeRepositoryImpl @Inject
constructor(
    val receivedLikeLocalDataSource: ReceivedLikeLocalDataSource,
    val receivedLikeRemoteDataSource: ReceivedLikeRemoteDataSource
) : ReceivedLikeRepository {

    override fun getYourUserId(): String {
        return "John"
    }

    override suspend fun saveAndStoreReceivedLike(
        yourUserId: String,
        receivedLikeId: String,
        receivedLikeData: MutableMap<String, Any>
    ) {
        val success =
            receivedLikeRemoteDataSource.writeReceivedLikeToFirebase(
                receivedLikeId,
                receivedLikeData
            )
        if (success) {
            val receivedLikeList =
                receivedLikeRemoteDataSource.getReceivedLikesFromFirebase()
                    .toMutableList()
            try {
                var receivedLike = receivedLikeLocalDataSource.findById(getYourUserId())
                receivedLike = receivedLike.copy(
                    receivedLikeId = getYourUserId(),
                    receivedLikes = receivedLikeList
                )
                receivedLikeLocalDataSource.upsert(receivedLike)
            } catch (npe: java.lang.NullPointerException) {
                val receivedLike =
                    ReceivedLike(getYourUserId(), receivedLikeList.toMutableList())
                Log.d("ReceivedLikeRepositoryImpl", "receivedLike was stored!")
                try {
                    receivedLikeLocalDataSource.upsert(receivedLike)
                } catch (e: Exception) {
                    Log.d("ReceivedLikeRepositoryImpl", "receivedLike failed to be stored!")
                }
            }
        }
    }

    override suspend fun storeReceivedLike(receivedLike: ReceivedLike) {
        receivedLikeLocalDataSource.upsert(receivedLike)
    }

    override fun getReceivedLike(userId: String): ReceivedLike {
        return receivedLikeLocalDataSource.findById(userId)
    }

    override suspend fun getReceivedLikes(): Flow<List<String>> {
        var receivedLike =
            receivedLikeLocalDataSource.findById(receivedLikeRemoteDataSource.getYourUserId())
        if (receivedLike == null) {
            receivedLike = ReceivedLike(getYourUserId(), receivedLikeRemoteDataSource.getReceivedLikesFromFirebase().toMutableList())
            receivedLikeLocalDataSource.upsert(
                receivedLike
            )
        }

        return flow {
            emit(
                receivedLike.receivedLikes
            )
        }
    }

    override suspend fun fetchUpdatedReceivedLikes(): Flow<List<String>> {
        val receivedLikeList = receivedLikeRemoteDataSource.getReceivedLikesFromFirebase()
        return flow { emit(receivedLikeList) }
    }

    override suspend fun removeReceivedLike(receivedLikeId: String) {
        val success =
            receivedLikeRemoteDataSource.removeReceivedLikeFromFirebase(receivedLikeId)
        if (success) {
            val receivedLikeList =
                receivedLikeRemoteDataSource.getReceivedLikesFromFirebase()
                    .toMutableList()
            try {
                var receivedLike =
                    receivedLikeLocalDataSource.findById(receivedLikeRemoteDataSource.getYourUserId())
                receivedLike = receivedLike.copy(
                    receivedLikeId = receivedLikeRemoteDataSource.getYourUserId(),
                    receivedLikes = receivedLikeList
                )
                receivedLikeLocalDataSource.upsert(receivedLike)
            } catch (npe: java.lang.NullPointerException) {
                val receivedLike =
                    ReceivedLike(
                        receivedLikeRemoteDataSource.getYourUserId(),
                        receivedLikeList.toMutableList()
                    )
                Log.d("ReceivedLikeRepositoryImpl", "receivedLike was stored!")
                try {
                    receivedLikeLocalDataSource.upsert(receivedLike)
                } catch (e: Exception) {
                    Log.d("ReceivedLikeRepositoryImpl", "receivedLike failed to be stored!")
                }
            }
        }
    }


}