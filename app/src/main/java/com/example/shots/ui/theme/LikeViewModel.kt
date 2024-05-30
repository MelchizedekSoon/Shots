package com.example.shots.ui.theme

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shots.NetworkBoundResource
import com.example.shots.RoomModule
import com.example.shots.ViewModelModule
import com.example.shots.data.AppDatabase
import com.example.shots.data.FirebaseRepository
import com.example.shots.data.ReceivedLike
import com.example.shots.data.SentLike
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LikeViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val firebaseAuth: FirebaseAuth,
    private val appDatabase: AppDatabase
) : ViewModel() {

    private val usersViewModel =
        ViewModelModule.provideUsersViewModel(firebaseRepository, firebaseAuth, appDatabase)
    private val receivedLikeViewModel =
        ViewModelModule.provideReceivedLikeViewModel(firebaseRepository, firebaseAuth, appDatabase)
    private val sentLikeViewModel =
        ViewModelModule.provideSentLikeViewModel(firebaseRepository, firebaseAuth, appDatabase)
    private val userDao = RoomModule.provideUserDao(appDatabase)
    private val receivedLikeDao = RoomModule.provideReceiveLikeDao(appDatabase)
    private val sentLikeDao = RoomModule.provideSentLikeDao(appDatabase)

    fun handleLike(userId: String?, isLiked: Boolean, context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val yourUserId = firebaseAuth.currentUser?.displayName ?: ""
                val sentLikeData: MutableMap<String, Any> =
                    mutableMapOf()
                sentLikeData["sentLike-${userId}"] =
                    userId ?: ""
                val receivedLikeData: MutableMap<String, Any> =
                    mutableMapOf()
                receivedLikeData["receivedLike-${yourUserId}"] =
                    yourUserId

                if (isLiked) {

                    sentLikeViewModel.saveSentLikeToFirebase(
                        userId ?: "",
                        sentLikeData
                    )

                    receivedLikeViewModel.saveReceivedLikeToFirebase(
                        userId ?: "",
                        receivedLikeData
                    )

                    val receivingUser = usersViewModel.fetchUserFromRoom(userId ?: "")

                    val userData: MutableMap<String, Any> = mutableMapOf()
                    val mediaItems: MutableMap<String, Uri> = mutableMapOf()

                    receivingUser.newLikesCount = receivingUser.newLikesCount?.plus(1)

                    userData["newLikesCount"] = receivingUser.newLikesCount ?: 0

                    receivingUser.likesCount = receivingUser.likesCount?.plus(1)

                    userData["likesCount"] = receivingUser.likesCount ?: 0

                    usersViewModel.saveUserDataToFirebase(
                        userId ?: "",
                        userData,
                        mediaItems,
                        context
                    ) {
                        viewModelScope.launch {
                            withContext(Dispatchers.IO) {
                                usersViewModel.storeUserInRoom(receivingUser.id)
                                NetworkBoundResource().requestForLikeNotification(context, userId)
                            }
                        }
                    }


                } else {

                    sentLikeViewModel.removeSentLikeFromFirebase(
                        userId ?: ""
                    )

                    receivedLikeViewModel.removeReceivedLikeFromFirebase(
                        yourUserId
                    )

                    val receivingUser = usersViewModel.fetchUserFromRoom(userId ?: "")

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

                    usersViewModel.saveUserDataToFirebase(
                        userId ?: "",
                        userData,
                        mediaItems,
                        context
                    ) {
                        viewModelScope.launch {
                            usersViewModel.storeUserInRoom(receivingUser.id)
                        }
                    }

                }

                val retrievedSentLikes: List<String> =
                    sentLikeViewModel.getSentLikesFromFirebase(
                        firebaseAuth.currentUser?.displayName ?: ""
                    )
                Log.d(
                    ContentValues.TAG,
                    "The retrievedSentLikes - $retrievedSentLikes"
                )


                if (yourUserId.isNotBlank()) {
                    val sentLikeList =
                        sentLikeViewModel.getSentLikesFromFirebase(
                            yourUserId
                        )
                    var sentLike = SentLike(
                        yourUserId,
                        sentLikeList.toMutableList()
                    )
                    Log.d(
                        ContentValues.TAG,
                        "sentLike = ${sentLike.sentLikes}"
                    )
                    sentLike =
                        sentLike.copy(sentLikes = retrievedSentLikes.toMutableList())
                    try {
                        sentLikeDao.insert(sentLike)
                    } catch (npe: java.lang.NullPointerException) {
                        sentLikeDao.insert(sentLike)
                    }
                }



                if (!userId.isNullOrBlank()) {
                    val retrievedReceivedLikes: List<String> =
                        receivedLikeViewModel.getReceivedLikesFromFirebase(
                            firebaseAuth.currentUser?.displayName ?: ""
                                ?: ""
                        )
                    var receivedLike = ReceivedLike(
                        userId,
                        retrievedReceivedLikes.toMutableList()
                    )

                    Log.d(
                        ContentValues.TAG,
                        "The retrievedReceivedLikes - $retrievedReceivedLikes"
                    )
                    receivedLike =
                        receivedLike.copy(receivedLikes = retrievedReceivedLikes.toMutableList())
                    try {
                        receivedLikeDao.insert(receivedLike)
                    } catch (npe: java.lang.NullPointerException) {
                        receivedLikeDao.insert(receivedLike)
                    }
                }
            }
        }
    }

}