package com.example.shots.ui.theme

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shots.data.FirebaseRepository
import com.example.shots.data.ReceivedLikeRepository
import com.example.shots.data.SentLikeRepository
import com.example.shots.data.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LikeViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val sentLikeRepository: SentLikeRepository,
    private val receivedLikeRepository: ReceivedLikeRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    fun handleLike(userId: String?, isLiked: Boolean, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {

            val sentLikeData: MutableMap<String, Any> =
                mutableMapOf()
            sentLikeData["sentLike-${userId}"] =
                userId ?: ""

            val receivedLikeData: MutableMap<String, Any> =
                mutableMapOf()
            receivedLikeData["receivedLike-${receivedLikeRepository.getYourUserId()}"] =
                receivedLikeRepository.getYourUserId()

            if (!isLiked) {

                sentLikeRepository.saveAndStoreSentLike(
                    userId ?: "", sentLikeData
                )

                receivedLikeRepository.saveAndStoreReceivedLike(
                    receivedLikeRepository.getYourUserId(), userId ?: "", receivedLikeData
                )

                val receivingUser = userRepository.getUser(userId ?: "")

                val userData: MutableMap<String, Any> = mutableMapOf()
                val mediaItems: MutableMap<String, Uri> = mutableMapOf()

                receivingUser.newLikesCount = receivingUser.newLikesCount?.plus(1)

                userData["newLikesCount"] = receivingUser.newLikesCount ?: 0

                receivingUser.likesCount = receivingUser.likesCount?.plus(1)

                userData["likesCount"] = receivingUser.likesCount ?: 0

                userRepository.saveUserData(
                    userId ?: "", userData,
                    mediaItems, context
                )

            } else {

                sentLikeRepository.removeSentLike(userId ?: "")

                receivedLikeRepository.removeReceivedLike(receivedLikeRepository.getYourUserId())

                val receivingUser = userRepository.getUser(receivedLikeRepository.getYourUserId())

                val userData: MutableMap<String, Any> = mutableMapOf()
                val mediaItems: MutableMap<String, Uri> = mutableMapOf()

                receivingUser.newLikesCount = receivingUser.newLikesCount?.minus(1)

                if (receivingUser.newLikesCount!! < 0) {
                    receivingUser.newLikesCount = 0
                }

                userData["newLikesCount"] = receivingUser.newLikesCount ?: 0

                receivingUser.likesCount = receivingUser.likesCount?.minus(1)

                if (receivingUser.likesCount!! < 0) {
                    receivingUser.likesCount = 0
                }

                userData["likesCount"] = receivingUser.likesCount ?: 0

                userRepository.saveUserData(
                    userId ?: "", userData,
                    mediaItems, context
                )

            }

        }
    }

}