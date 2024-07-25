package com.example.shots.data

import kotlinx.coroutines.flow.Flow

interface ReceivedLikeRepository {

    fun getYourUserId(): String

    suspend fun saveAndStoreReceivedLike(
        yourUserId: String,
        receivedLikeId: String,
        receivedLikeData: MutableMap<String, Any>
    )

    suspend fun storeReceivedLike(receivedLike: ReceivedLike)

    fun getReceivedLike(userId: String): ReceivedLike

    suspend fun getReceivedLikes(): Flow<List<String>>

    suspend fun fetchUpdatedReceivedLikes(): Flow<List<String>>

    suspend fun removeReceivedLike(receivedLikeId: String)

}