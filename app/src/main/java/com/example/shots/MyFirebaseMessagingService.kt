package com.example.shots

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.shots.data.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.auth.User
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.onesignal.OneSignal
import io.getstream.android.push.firebase.FirebaseMessagingDelegate
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody


class MyFirebaseMessagingService : FirebaseMessagingService() {


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//         TODO(developer): Handle FCM messages here.
        val context: Context = applicationContext // Get the application context

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d("MyFirebaseMessagingService", "From: ${remoteMessage.from}")

        FirebaseMessagingDelegate.handleRemoteMessage(remoteMessage)

//        val CHANNEL_ID = FirebaseModule.provideFirebaseAuth().currentUser?.email ?: ""
//
//        var builder = NotificationCompat.Builder(this, CHANNEL_ID)
//            .setSmallIcon(R.drawable.shots_3_cropped)
//            .setContentTitle(remoteMessage.notification?.title)
//            .setContentText(remoteMessage.notification?.body)
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)


        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("MyFirebaseMessagingService", "Message data payload: ${remoteMessage.data}")

//            // Check if data needs to be processed by long running job
//            if (needsToBeScheduled()) {
//                // For long-running tasks (10 seconds or more) use WorkManager.
//                scheduleJob()
//            } else {
//                // Handle message within 10 seconds
//                handleNow()
//            }
        }

        val sender = remoteMessage.data["sender"]
        var userId = remoteMessage.data["receiver_id"]

        if (!sender.isNullOrEmpty()) {
            Log.d("FirebaseMessagingService", "Sender: $sender")
            if (sender == "stream.chat") {
                val oneSignalId = OneSignal.User.onesignalId
                val externalId = userId // You will supply the external user id to the OneSignal SDK
//        OneSignal.login(externalId)

                val firebaseStorage = FirebaseModule.provideStorage()
                val firebaseFirestore = FirebaseModule.provideFirestore()
                val firebaseAuth = FirebaseModule.provideFirebaseAuth()
                val firebaseRepository = FirebaseModule.provideFirebaseRepository(
                    firebaseAuth,
                    firebaseFirestore, firebaseStorage
                )
//                val usersViewModel = ViewModelModule.provideUserViewModel(firebaseRepository)

                val appDatabase = RoomModule.provideAppDatabase(context)
                val userDao = RoomModule.provideUserDao(appDatabase)


                val userData: MutableMap<String, Any> = mutableMapOf()
                val mediaItems: MutableMap<String, Uri> = mutableMapOf()


//                val user = getUser(userRepository, firebaseAuth)

//                userData["newMessagesCount"] = user.newMessagesCount?.plus(1) ?: 0

//                usersViewModel.saveUserDataToFirebase(userId ?: "", userData, mediaItems, context) {
//
//                }


                val okHttpclient = OkHttpClient()

                Log.d("NetworkBoundResource", "Inside requestForLikeNotification")

                //     "\"target_channel\":\"push\"," +

                val mediaType = "application/json".toMediaTypeOrNull()

                val body = """{
            "app_id": "772687fc-521e-4b2c-9ace-60ce2cb533b6",
            "name": "Shots",
            "include_aliases": { "external_id": ["$externalId"]},
            "target_channel": "push",
            "data": {"foo": "bar"},
            "headings": {"en": "Shots"},
            "contents": {"en": "Someone messaged you!"}
        }""".toRequestBody(mediaType)

                val apiKey = "OTkwMDRiNjUtYTA5Ny00NzYyLWIwNGEtMWI3MDI2ODIwMTk4"

                val request =
                    Request.Builder().url("https://api.onesignal.com/notifications").post(body)
                        .addHeader("accept", "application/json")
                        .addHeader("Authorization", "Basic ${apiKey}")
                        .addHeader("content-type", "application/json")
                        .build()

                val call = okHttpclient.newCall(request)

                val response = call.execute()


            }
        }

        Log.d("FirebaseMessagingService", "Message notification: ${remoteMessage.notification}")

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d("FirebaseMessagingService", "Message Notification Body: ${it.body}")
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.

        Log.d(
            "FirebaseMessagingService",
            "Message Notification Body: ${remoteMessage.notification?.body}"
        )

        val title = remoteMessage.notification?.title
        val text = remoteMessage.notification?.body

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

// Create the notification channel
        val CHANNEL_ID = "SHOTS_NOTIFICATION"
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Shots Notification",
            NotificationManager.IMPORTANCE_HIGH
        )

// Register the notification channel with the system
        notificationManager.createNotificationChannel(channel)

// Build and display the notification
//        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
//            .setSmallIcon(R.drawable.sym_def_app_icon)
//            .setContentTitle(title)
//            .setContentText(text)
//            .setAutoCancel(true)
//            .build()

// Use a unique notification ID to identify and update the notification later if needed
//        val notificationId = 1
//        notificationManager.notify(notificationId, notification)


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("MyFirebaseMessagingService", "Permission not granted")
//            NotificationManagerCompat.from(this).notify(1, notification).notify()

            super.onMessageReceived(remoteMessage)
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

//        NotificationManagerCompat.from(this).notify(1, notification).notify()

        super.onMessageReceived(remoteMessage)
    }

    override fun onDeletedMessages() {
        // Called when messages are deleted from the server
        FirebaseMessagingService().onDeletedMessages()
    }

    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Log.d("MyFirebaseMessagingService", "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
//        sendRegistrationToServer(token)
    }
}

fun getUser(userRepository: UserRepository, firebaseAuth: FirebaseAuth): com.example.shots.data.User {
    var userId = firebaseAuth.currentUser?.displayName
    return userRepository.getUser(userId ?: "")
}

