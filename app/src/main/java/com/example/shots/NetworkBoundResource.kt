package com.example.shots

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.shots.data.User
import com.google.gson.Gson
import com.onesignal.OneSignal
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class NetworkBoundResource() {

    fun createUser(userId: String?) {

        OneSignal.login(userId ?: "")

        val okHttpClient = OkHttpClient()

        val mediaType = "application/json".toMediaTypeOrNull()

        val body =
            """"{"identity":{"external_id":"${userId ?: ""}"}{"subscriptions":[{"type":"AndroidPush","token":"${userId ?: ""}",
                |"device_model":"${Build.MODEL}", 
                |"sdk":"${Build.VERSION.SDK_INT}"}]}""".trimMargin().toRequestBody(
                mediaType
            )


        val request = Request.Builder()
            .url("https://api.onesignal.com/apps/772687fc-521e-4b2c-9ace-60ce2cb533b6/users")
            .post(body)
            .addHeader("accept", "application/json")
            .addHeader("content-type", "application/json")
            .build()


        val response = okHttpClient.newCall(request).execute()
    }

    fun requestForLikeNotification(context: Context, userId: String?) {

        val oneSignalId = OneSignal.User.onesignalId
//        val externalId = "123456789" // You will supply the external user id to the OneSignal SDK
//        OneSignal.login(externalId)
        val externalId = userId

        Log.d("NetworkBoundResource", "userId = $externalId")

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
            "contents": {"en": "Someone liked you!"}
        }""".toRequestBody(mediaType)

        val apiKey = "OTkwMDRiNjUtYTA5Ny00NzYyLWIwNGEtMWI3MDI2ODIwMTk4"

        val request = Request.Builder().url("https://api.onesignal.com/notifications").post(body)
            .addHeader("accept", "application/json").addHeader("Authorization", "Basic ${apiKey}")
            .addHeader("content-type", "application/json")
            .build()

        val call = okHttpclient.newCall(request)

        val response = call.execute()

        val responseBody = response.body?.string()

        Log.d("NetworkBoundResource", "externalId = $externalId")
        Log.d("NetworkBoundResource", "oneSignalId = $oneSignalId")
        Log.d("NetworkBoundResource", "Response body: $responseBody")

        val gson = Gson()

//        val notificationResponse = gson.fromJson(responseBody, NotificationResponse::class.java)

        val responseCode = response.code

//        val CHANNEL_ID = getString(
//            context.contentResolver,
//            R.string.default_notification_channel_id.toString()
//        )

        val CHANNEL_ID = "Channel ID"

//        val notification = NotificationCompat.Builder(context, CHANNEL_ID).setContentTitle("Shots")
//            .setContentText(responseBody).setSmallIcon(R.drawable.ic_stat_onesignal_default)
//            .setColor(0xFF6F00).build()

//        val notificationManager = NotificationManagerCompat.from(context)
//        NotificationCompat.Builder(context, CHANNEL_ID)
//            .setContentTitle("Shots")
//            .setContentText("Someone liked you!")
//            .setColor(Color.parseColor("#FF6F00"))
//            .setSmallIcon(R.drawable.ic_stat_onesignal_default)
//            .build()
//
//
//        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
//            .setContentTitle("Shots")
//            .setContentText("Someone liked you!")
//            .setColor(0xFF6F00)
//            .setSmallIcon(R.drawable.ic_stat_onesignal_default)
//            .build()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d("NetworkBoundResource", "The notification should show now - inside")
//            notificationManager.notify(View.generateViewId(), notification)
            return
        }
        Log.d("NetworkBoundResource", "The notification should show now - outside")
//        notificationManager.notify(View.generateViewId(), notification)
    }

    fun requestForShotNotification(context: Context, userId: String?) {

        val oneSignalId = OneSignal.User.onesignalId
        val externalId = userId ?: "" // You will supply the external user id to the OneSignal SDK
        OneSignal.login(externalId)

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
            "contents": {"en": "Someone sent you a shot!"}
        }""".toRequestBody(mediaType)

        val apiKey = "OTkwMDRiNjUtYTA5Ny00NzYyLWIwNGEtMWI3MDI2ODIwMTk4"

        val request = Request.Builder().url("https://api.onesignal.com/notifications").post(body)
            .addHeader("accept", "application/json").addHeader("Authorization", "Basic ${apiKey}")
            .addHeader("content-type", "application/json")
            .build()

        val call = okHttpclient.newCall(request)

        val response = call.execute()

        val responseBody = response.body?.string()

        Log.d("NetworkBoundResource", "externalId = $externalId")
        Log.d("NetworkBoundResource", "oneSignalId = $oneSignalId")
        Log.d("NetworkBoundResource", "Response body: $responseBody")

        val gson = Gson()

//        val notificationResponse = gson.fromJson(responseBody, NotificationResponse::class.java)

        val responseCode = response.code

//        val CHANNEL_ID = getString(
//            context.contentResolver,
//            R.string.default_notification_channel_id.toString()
//        )

        val CHANNEL_ID = "Channel ID"

//        val notification = NotificationCompat.Builder(context, CHANNEL_ID).setContentTitle("Shots")
//            .setContentText(responseBody).setSmallIcon(R.drawable.ic_stat_onesignal_default)
//            .setColor(0xFF6F00).build()

//        val notificationManager = NotificationManagerCompat.from(context)
//        NotificationCompat.Builder(context, CHANNEL_ID)
//            .setContentTitle("Shots")
//            .setContentText("Someone liked you!")
//            .setColor(Color.parseColor("#FF6F00"))
//            .setSmallIcon(R.drawable.ic_stat_onesignal_default)
//            .build()
//
//
//        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
//            .setContentTitle("Shots")
//            .setContentText("Someone liked you!")
//            .setColor(0xFF6F00)
//            .setSmallIcon(R.drawable.ic_stat_onesignal_default)
//            .build()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d("NetworkBoundResource", "The notification should show now - inside")
//            notificationManager.notify(View.generateViewId(), notification)
            return
        }
        Log.d("NetworkBoundResource", "The notification should show now - outside")
//        notificationManager.notify(View.generateViewId(), notification)
    }

}