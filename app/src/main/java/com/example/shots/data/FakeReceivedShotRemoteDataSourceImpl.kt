package com.example.shots.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class FakeReceivedShotRemoteDataSourceImpl @Inject
constructor(
    val firebaseAuth: FirebaseAuth,
    val firebaseFirestore: FirebaseFirestore,
    val firebaseStorage: FirebaseStorage
) : ReceivedShotRemoteDataSource {

    override fun getYourUserId(): String {
        return "John"
    }

    override suspend fun writeReceivedShotToFirebase(
        userId: String,
        receivedShotData: MutableMap<String, Uri>,
        context: Context
    ): Boolean {
        return try {
            val downloadUrl = uploadReceivedShotToStorage(userId, receivedShotData, context)
            val userRef = firebaseFirestore.collection("users").document(userId)
            val receivedShotRef = userRef.collection("receivedShots")
                .document(userId)
                .set(downloadUrl, SetOptions.merge()).await()
            Log.d("FakeReceivedShotRemoteDataSourceImpl", "downloadUrl = $downloadUrl")
            Log.d("FakeReceivedShotRemoteDataSourceImpl", "receivedShot added successfully")
            true
        } catch (e: Exception) {
            Log.d("FakeReceivedShotRemoteDataSourceImpl", "Error adding receivedShot: ${e.message}")
            // Handle any potential errors here
            false
        }
    }

    override suspend fun uploadReceivedShotToStorage(
        userId: String,
        receivedShotItems: MutableMap<String, Uri>,
        context: Context
    ): Map<String, Uri?> {
        val downloadUrl = mutableMapOf<String, Uri?>()
        val storageRef =
            firebaseStorage.reference.child("users/${userId}/gallery/receivedShots")
        receivedShotItems.forEach { (fieldName, uri) ->
            uri.let { validUri ->
                val fileName =
                    "${fieldName}_${System.currentTimeMillis()}"

                removeReceivedShotFromFirebase(userId)

                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(validUri)
                val tempFile = File.createTempFile("temp", null, context.cacheDir)
                tempFile.deleteOnExit()
                val outputStream = FileOutputStream(tempFile)
                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                val localFileUri = Uri.fromFile(tempFile)

                val mediaRef = storageRef.child(fileName)
                try {
                    val addedFile = mediaRef.putFile(localFileUri).await()
                    Log.d("FakeReceivedShotRemoteDataSource", "File successfully added to storage")
                    val finishedDownloadUrl = mediaRef.downloadUrl.await()
                    downloadUrl[fieldName] = finishedDownloadUrl
                } catch (e: Exception) {
                    Log.d(
                        "FakeReceivedShotRemoteDataSource",
                        "There was an issue with uploading the file to Firebase. HTTP result and code: ${e.message}"
                    )
                }
            }
        }

        return downloadUrl
    }

    override suspend fun getReceivedShotsFromFirebase(): List<String> {
        val receivedShotsList = mutableListOf<String>()

        try {
            val receivedShotRef = firebaseFirestore
                .collection("users")
                .document(getYourUserId())
                .collection("receivedShots")

            val collection = receivedShotRef.get().await()

            for (document in collection.documents) {
                for ((key, value) in document.data.orEmpty()) {
                    if (key.startsWith("receivedShot-")) {
                        val fieldUserId = key.removePrefix("receivedShot-")
//                        val receivedShotValue = value
                        Log.d(
                            "FakeReceivedShotRemoteDataSource",
                            "Field key: $key, Field value: $value"
                        )
                        receivedShotsList.add("$fieldUserId-$value")
                    }
                }
            }
            return receivedShotsList
        } catch (e: Exception) {
            Log.d(
                "FakeReceivedShotRemoteDataSource",
                "Something went wrong fetching receivedShots: ${e.message}"
            )
        }

        return emptyList()
    }

    override suspend fun getIfSeenReceivedShotsFromFirebase(): List<String> {
        val ifSeenReceivedShotsList = mutableListOf<String>()

        try {
            val receivedShotRef = firebaseFirestore
                .collection("users")
                .document(getYourUserId())
                .collection("receivedShots")
                .document(getYourUserId())
                .collection("ifSeenReceivedShots")

            val collection = receivedShotRef.get().await()

            for (document in collection.documents) {
                for ((key, value) in document.data.orEmpty()) {
                    if (key.startsWith("ifSeenReceivedShot-")) {
                        val fieldUserId = key.removePrefix("ifSeenReceivedShot-")
//                        val receivedShotValue = value
                        Log.d(
                            "FakeReceivedShotRemoteDataSource",
                            "Field key: $key, Field value: $value"
                        )
                        ifSeenReceivedShotsList.add("$fieldUserId-$value")
                    }
                }
            }
            return ifSeenReceivedShotsList
        } catch (e: Exception) {
            Log.d(
                "FakeReceivedShotRemoteDataSource",
                "Something went wrong fetching ifSeenReceivedShots: ${e.message}"
            )
        }

        return emptyList()
    }

//    suspend fun removeReceivedShotFromFirebase(userId: String): Boolean {
//        val storageRef = firebaseStorage.reference
//        val userRefForStorage = storageRef.child("users/${getYourUserId()}")
//        val mediaRef = userRefForStorage.child("gallery/receivedShots")
//        val userRefForFirestore = firebaseFirestore.collection("users").document(getYourUserId())
//
//        try {
//            // Delete the downloaded url from Firestore
//            userRefForFirestore
//                .collection("receivedShots").document(getYourUserId())
//                .delete().await()
//            val mediaList = mediaRef.listAll().await()
//            Log.d(
//                "FakeReceivedShotRemoteDataSource",
//                "Inside storage for receivedShots, looking for" +
//                        " receivedShot-$userId"
//            )
//            for (item in mediaList.items) {
//                Log.d("FakeReceivedShotRemoteDataSource", "item.name = ${item.name}")
//                when {
//                    item.name.startsWith("receivedShot-$userId") -> {
//                        // Delete the existing media with the specified identifier from Storage
//                        item.delete().await()
//                        Log.d(
//                            "FakeReceivedShotRemoteDataSource",
//                            "Deleted previous shot that was in the ${"receivedShot-$userId"} spot from" +
//                                    "firestore and from storage"
//                        )
//                        // Once deleted, break out of the loop since we've removed the existing media
//                        break
//                    }
//                }
//            }
//        } catch (e: Exception) {
//            Log.e(
//                "FakeReceivedShotRemoteDataSource",
//                "Error deleting ${"receivedShot-$userId"}: ${e.message}"
//            )
//            return false
//        }
//        return true
//    }

    override suspend fun removeReceivedShotFromFirebase(userId: String?): Boolean {
        val storageRef = firebaseStorage.reference
        val userRefForStorage = storageRef.child("users/${userId}")
        val mediaRef = userRefForStorage.child("gallery/receivedShots")

        if (userId == null) {
            Log.d("FakeReceivedShotRemoteDataSource", "Invalid userId: null")
            return false // Error
        }

        return try {
            val receivedShotRef = firebaseFirestore
                .collection("users")
                .document(getYourUserId())
                .collection("receivedShots")
                .document(getYourUserId())

            val updates = hashMapOf<String, Any>(
                "receivedShot-${userId}" to FieldValue.delete()
            )

            receivedShotRef.update(updates).addOnSuccessListener {
                Log.d(
                    "FakeReceivedShotRemoteDataSource",
                    "Field 'receivedShot-$userId' deleted successfully"
                )
            }.addOnFailureListener {
                Log.d(
                    "FakeReceivedShotRemoteDataSource",
                    "Error deleting field 'receivedShot-$userId': ${it.message}"
                )
            }.await() // Await the completion of the update operation

            val mediaList = mediaRef.listAll().await()
            Log.d("FakeReceivedShotRemoteDataSource", "Inside " +
                    "removeReceivedShotFromFirebase = ${mediaList.items}")
            Log.d(
                "FakeReceivedShotRemoteDataSource",
                "Inside storage for receivedShots, looking for" +
                        " receivedShot-${getYourUserId()}"
            )
            for (item in mediaList.items) {
                Log.d("FakeReceivedShotRemoteDataSource", "item.name = ${item.name}")
                when {
                    item.name.contains("receivedShot-${getYourUserId()}") -> {
                        // Delete the existing media with the specified identifier from Storage
                        item.delete().await()
                        Log.d(
                            "FakeReceivedShotRemoteDataSource",
                            "Deleted previous shot that was in the ${"receivedShot-${getYourUserId()}"} spot from" +
                                    "firestore and from storage"
                        )
                        // Once deleted, break out of the loop since we've removed the existing media
                        break
                    }
                }
            }
            true
        } catch (e: Exception) {
            Log.d(
                "FakeReceivedShotRemoteDataSource",
                "Error deleting field 'receivedShot-$userId': ${e.message}"
            )
            false
        }
    }

    override suspend fun removeTheirReceivedShotFromFirebase(userId: String?): Boolean {
        val storageRef = firebaseStorage.reference
        val userRefForStorage = storageRef.child("users/${userId}")
        val mediaRef = userRefForStorage.child("gallery/receivedShots")

        if (userId == null) {
            Log.d("FakeReceivedShotRemoteDataSource", "Invalid userId: null")
            return false // Error
        }

        return try {
            val receivedShotRef = firebaseFirestore
                .collection("users")
                .document(userId)
                .collection("receivedShots")
                .document(userId)

            val updates = hashMapOf<String, Any>(
                "receivedShot-${getYourUserId()}" to FieldValue.delete()
            )

            receivedShotRef.update(updates).addOnSuccessListener {
                Log.d(
                    "ReceivedLikeRemoteDataSource",
                    "Field 'receivedShot-${getYourUserId()}' deleted successfully"
                )
            }.addOnFailureListener {
                Log.d(
                    "FakeReceivedShotRemoteDataSource",
                    "Error deleting field 'receivedShot-${getYourUserId()}': ${it.message}"
                )
            }.await() // Await the completion of the update operation

            val mediaList = mediaRef.listAll().await()
            Log.d(
                "FakeReceivedShotRemoteDataSource",
                "Inside storage for receivedShots, looking for" +
                        " receivedShot-${getYourUserId()}"
            )
            for (item in mediaList.items) {
                Log.d("FakeReceivedShotRemoteDataSource", "item.name = ${item.name}")
                when {
                    item.name.startsWith("receivedShot-${getYourUserId()}") -> {
                        // Delete the existing media with the specified identifier from Storage
                        item.delete().await()
                        Log.d(
                            "FakeReceivedShotRemoteDataSource",
                            "Deleted previous shot that was in the ${"receivedShot-${getYourUserId()}"} spot from" +
                                    "firestore and from storage"
                        )
                        // Once deleted, break out of the loop since we've removed the existing media
                        break
                    }
                }
            }
            true
        } catch (e: Exception) {
            Log.d(
                "FakeReceivedShotRemoteDataSource",
                "Error deleting field 'receivedShot-$userId': ${e.message}"
            )
            false
        }
    }


//    suspend fun removeReceivedShotFromFirebase(userId: String?): Boolean {
//        if (userId == null) {
//            Log.d("FakeReceivedShotRemoteDataSource", "Invalid userId: null")
//            return false // Error
//        }
//
//        Log.d("FakeReceivedShotRemoteDataSource", "userId: $userId")
//
//        return try {
//            val receivedShotRef = firebaseFirestore
//                .collection("users")
//                .document(getYourUserId())
//                .collection("receivedShots")
//                .document(getYourUserId())
//
//            val updates = hashMapOf<String, Any>(
//                "receivedShot-${userId}" to FieldValue.delete()
//            )
//
//            receivedShotRef.update(updates).addOnSuccessListener {
//                Log.d(
//                    "FakeReceivedShotRemoteDataSource",
//                    "Field 'receivedShot-$userId' deleted successfully"
//                )
//            }.addOnFailureListener {
//                Log.d(
//                    "FakeReceivedShotRemoteDataSource",
//                    "Error deleting field 'receivedShot-$userId': ${it.message}"
//                )
//            }.await() // Await the completion of the update operation
//
//            true
//        } catch (e: Exception) {
//            Log.d(
//                "FakeReceivedShotRemoteDataSource",
//                "Error deleting field 'receivedShot-$userId': ${e.message}"
//            )
//            false
//        }
//    }


}