package com.example.shots.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface SentLikeRepository {

    fun getYourUserId(): String

    suspend fun saveAndStoreSentLike(
        sentLikeId: String,
        sentLikeData: MutableMap<String, Any>
    )

    suspend fun storeSentLike(sentLike: SentLike)

    fun getSentLike(userId: String): SentLike

    suspend fun removeSentLike(sentLikeId: String)

    suspend fun fetchUpdatedSentLikes(): Flow<List<String>>

}