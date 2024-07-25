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

class FakeSentShotRemoteDataSourceImpl @Inject constructor(
    val firebaseAuth: FirebaseAuth,
    val firebaseFirestore: FirebaseFirestore,
    val firebaseStorage: FirebaseStorage,
) : SentShotRemoteDataSource {

    override fun getYourUserId(): String {
        return "John"
    }

    override suspend fun writeSentShotToFirebase(
        userId: String,
        sentShotData: MutableMap<String, Uri>,
        context: Context
    ): Boolean {
        return try {
            val downloadUrl = uploadSentShotToStorage(userId, sentShotData, context)
            Log.d("SentShotRemoteDataSource", "downloadUrl = $downloadUrl")
            val userRef = firebaseFirestore.collection("users").document(getYourUserId())
            val sentShotRef = userRef.collection("sentShots")
                .document(getYourUserId())
                .set(downloadUrl, SetOptions.merge()).await()
            Log.d("SentShotRemoteDataSource", "sentShot added successfully")
            true
        } catch (e: Exception) {
            Log.d("SentShotRemoteDataSource", "Error adding sentShot: ${e.message}")
// Handle any potential errors here
            false
        }
    }

     override suspend fun removeSentShotFromFirebase(userId: String?): Boolean {
        val storageRef = firebaseStorage.reference
        val userRefForStorage = storageRef.child("users/${getYourUserId()}")
        val mediaRef = userRefForStorage.child("gallery/sentShots")

        if (userId == null) {
            Log.d("SentShotRemoteDataSource", "Invalid userId: null")
            return false // Error
        }

        return try {
            val sentShotRef = firebaseFirestore
                .collection("users")
                .document(getYourUserId())
                .collection("sentShots")
                .document(getYourUserId())

            val updates = hashMapOf<String, Any>(
                "sentShot-${userId}" to FieldValue.delete()
            )

            sentShotRef.update(updates).addOnSuccessListener {
                Log.d(
                    "SentShotRemoteDataSource",
                    "Field 'sentShot-$userId' deleted successfully"
                )
            }.addOnFailureListener {
                Log.d(
                    "SentShotRemoteDataSource",
                    "Error deleting field 'sentShot-$userId': ${it.message}"
                )
            }.await() // Await the completion of the update operation

            val mediaList = mediaRef.listAll().await()
            Log.d(
                "SentShotRemoteDataSource",
                "Inside storage for sentShots, looking for" +
                        " sentShot-$userId"
            )
            for (item in mediaList.items) {
                Log.d("SentShotRemoteDataSource", "item.name = ${item.name}")
                when {
                    item.name.contains("sentShot-$userId") -> {
                        // Delete the existing media with the specified identifier from Storage
                        item.delete().addOnSuccessListener {
                            Log.d(
                                "SentShotRemoteDataSource",
                                "Successfully deleted media = ${item.name}"
                            )
                        }.addOnFailureListener {
                            Log.d(
                                "SentShotRemoteDataSource",
                                "Error deleting media: ${it.message}"
                            )
                        }
                        Log.d(
                            "SentShotRemoteDataSource",
                            "Deleted previous shot that was in the ${"sentShot-$userId"} spot from" +
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
                "SentShotRemoteDataSource",
                "Error deleting field 'sentShot-$userId': ${e.message}"
            )
            false
        }
    }


    override suspend fun uploadSentShotToStorage(
        userId: String,
        sentShotItems: MutableMap<String, Uri>,
        context: Context
    ): Map<String, Uri?> {
        val downloadUrl = mutableMapOf<String, Uri?>()
        val storageRef =
            firebaseStorage.reference.child("users/${getYourUserId()}/gallery/sentShots")
        Log.d("SentShotRemoteDataSource", "sentShotItems = $sentShotItems")
        sentShotItems.forEach { (fieldName, uri) ->
            uri.let { validUri ->
                val fileName =
                    "${fieldName}_${System.currentTimeMillis()}"

                removeSentShotFromFirebase(userId)

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
                    Log.d("SentShotRemoteDataSource", "File being added to storage")
                    val addedFile = mediaRef.putFile(localFileUri)
                    Log.d("SentShotRemoteDataSource", "is file paused? ${addedFile.isPaused}")
                    Log.d("SentShotRemoteDataSource", "is file in progress? ${addedFile.isInProgress}")
                    Log.d("SentShotRemoteDataSource", "is file adding complete? ${addedFile.isComplete}")
                    addedFile.addOnSuccessListener {
                        Log.d("SentShotRemoteDataSource", "File successfully added to storage")
                    }
                    addedFile.addOnFailureListener {
                        Log.d("SentShotRemoteDataSource", "File failed to be added to storage")
                    }
                    addedFile.await()
                    val finishedDownloadUrl = mediaRef.downloadUrl.await()
                    downloadUrl[fieldName] = finishedDownloadUrl
                } catch (e: Exception) {
                    Log.d(
                        "SentShotRemoteDataSource",
                        "There was an issue with uploading the file to Firebase. HTTP result and code: ${e.message}"
                    )
                }
            }
        }
        Log.d(
            "SentShotRemoteDataSource",
            "list of sentShots = $downloadUrl"
        )
        return downloadUrl
    }


    override suspend fun getSentShotsFromFirebase(): List<String> {

        val sentShotsList = mutableListOf<String>()

        try {
            val sentShotsRef = firebaseFirestore
                .collection("users")
                .document(getYourUserId())
                .collection("sentShots")

            val collection = sentShotsRef.get().await()

            for (document in collection.documents) {
                for ((key, value) in document.data.orEmpty()) {
                    if (key.startsWith("sentShot-")) {
                        val fieldUserId = key.removePrefix("sentShot-")
//                        val sentShotValue = value
                        Log.d("SentShotRemoteDataSource", "Field key: $key, Field value: $value")
                        sentShotsList.add("$fieldUserId-$value")
                    }
                }
//                for (field in document.data?.keys.orEmpty()) {
//                    if (field.startsWith("sentShot-")) {
//                        val fieldUserId = field.removePrefix("sentShot-")
////                        val sentShotValue = document.getString(field)
//                        val sentShotValue = field.removePrefix("sentShot-")
//                        Log.d("SentShotRemoteDataSource", "sentShotValue = $sentShotValue")
//                        sentShotsList.add("$fieldUserId-$sentShotValue")
//                    }
//                }
            }
            Log.d(
                "SentShotRemoteDataSource",
                "Inside sentShotViewModel in Firebase - the sentShotsList is $sentShotsList"
            )
            return sentShotsList
        } catch (e: Exception) {
            Log.d(
                "SentShotRemoteDataSource",
                "Something went wrong fetching sentShots: ${e.message}"
            )
        }

        return mutableListOf()
    }
}