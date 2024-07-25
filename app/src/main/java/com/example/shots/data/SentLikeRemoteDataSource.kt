package com.example.shots.data

interface SentLikeRemoteDataSource {

        fun getYourUserId(): String

        suspend fun writeSentLikeToFirebase(
            userId: String,
            sentLikeData: MutableMap<String, Any>
        ): Boolean

        suspend fun getSentLikesFromFirebase(userId: String): List<String>

        suspend fun removeSentLikeFromFirebase(userId: String?): Boolean

}