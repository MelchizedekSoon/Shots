package com.example.shots.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


// "FirebaseRepository".kt
class FirebaseRepository(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
) {
    sealed class SignUpResult {
        data class Success(val authResult: AuthResult) : SignUpResult()
        data class Failure(val reason: String) : SignUpResult()
    }

    suspend fun signInWithEmailPassword(email: String, password: String): AuthResult? {
        // Firebase authentication f
        return firebaseAuth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun createUserWithEmailAndPassword(email: String, password: String): AuthResult? {
        userProfileChangeRequest { }
        return firebaseAuth.createUserWithEmailAndPassword(email, password).await()
    }


    suspend fun getMetadataFromStorage(mediaIdentifier: String): String {
        val userId = firebaseAuth.currentUser?.displayName ?: ""
        if (userId.isBlank()) {
            Log.e("FirebaseRepository", "User ID is blank")
            return "" // Or throw an exception if appropriate
        }

        val storageRef = firebaseStorage.reference
        val galleryRef = storageRef.child("users/$userId/gallery")
        val mediaList = galleryRef.listAll().await()
        val filteredItem = mediaList.items.find { item ->
            item.name.startsWith(mediaIdentifier)
        }

        if (filteredItem == null) {
            Log.e("FirebaseRepository", "No matching item found in the gallery")
            return "" // Or throw an exception if appropriate
        }

        return suspendCancellableCoroutine { continuation ->
            filteredItem.metadata.addOnSuccessListener { metadata ->
                val contentType = metadata.contentType ?: ""
                Log.d("FirebaseRepository", "The contentType was found within the repository")
                continuation.resume(contentType)
            }.addOnFailureListener { exception ->
                Log.e(
                    "FirebaseRepository",
                    "Error getting metadata for item: ${filteredItem.path}",
                    exception
                )
                continuation.resumeWithException(exception)
            }
        }
    }

    suspend fun getUsers(): List<User> {
        val yourUserId = firebaseAuth.currentUser?.displayName ?: ""
        return try {
            val docRef = firebaseFirestore.collection("users").get().await()
            val users = mutableListOf<User>()

            for (doc in docRef) {
                val user = doc.toObject(User::class.java)
                users.add(user)
            }
            users
        } catch (e: Exception) {
            Log.d(
                "FirebaseRepository",
                "The get all users retrieval is not working right now - ${e.message}"
            )
            // Handle any potential errors here
            emptyList() // Return an empty list or any appropriate default value
        }
    }


//    suspend fun fetchListOfBookmarks(userId: String): String? {
//        val docRef = firebaseFirestore.collection("users").document(userId)
//        try {
//            val document = docRef.get().await()
////            Log.d("FirebaseRepository", "Here is the value for document - $document")
//            if (document.exists()) {
//                val userData = document.data
//                if (userData != null) {
//                    val bookmarksValue = userData["bookmarks"] as? String
//                    val bookmarks = bookmarksValue
//                    if (bookmarksValue == null) {
//                        Log.e("FirebaseRepository", "Invalid bookmarks value: $bookmarksValue")
//                    }
//                    return bookmarks
//                }
//            }
//        } catch (e: Exception) {
//            Log.d("FirebaseRepository", "Bookmarks cannot be retrieved. ${e.message}")
//        }
//        return null
//    }


    suspend fun getUserData(userId: String): User? {
        try {
            val docRef = firebaseFirestore.collection("users").document(userId)
            try {
                val document = docRef.get().await()
                Log.d("FirebaseRepository", "Here is the value for document - $document")
                if (document.exists()) {
                    val userData = document.data
                    Log.d(
                        "FirebaseRepository",
                        "mediaOne upon return is ${userData?.get("mediaOne")}"
                    )
                    // Check if all required fields are present and of expected types
                    if (userData != null) {
                        val displayNameValue = userData["displayName"] as? String
                        var displayName = ""
                        try {
                            if (displayNameValue != null) {
                                displayName = displayNameValue
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "FirebaseRepository",
                                "Invalid displayName value: $displayNameValue"
                            )
                        }
                        val userNameValue = userData["userName"] as? String
                        var userName = ""
                        try {
                            if (userNameValue != null) {
                                userName = userNameValue
                            }
                        } catch (e: Exception) {
                            Log.e("FirebaseRepository", "Invalid userName value: $userNameValue")
                        }

                        val latitudeValue = userData["latitude"] as? Number
                        var latitude = 0.0
                        try {
                            latitudeValue?.let {
                                latitude = it.toDouble()
                            }
                        } catch (e: NumberFormatException) {
                            Log.e("FirebaseRepository", "Invalid latitude value: $latitudeValue")
                            // Handle the error appropriately
                        }

                        val longitudeValue = userData["longitude"] as? Number
                        var longitude = 0.0
                        try {
                            longitudeValue?.let {
                                longitude = it.toDouble()
                            }
                        } catch (e: NumberFormatException) {
                            Log.e("FirebaseRepository", "Invalid longitude value: $longitudeValue")
                            // Handle the error appropriately
                        }

                        val birthdayValue = userData["birthday"] as? Number
                        var birthday: Long? = null
                        try {
                            if (birthdayValue != null) {
                                birthday = birthdayValue.toLong()
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "FirebaseRepository", "Invalid birthday value: ${birthdayValue}" +
                                        " - ${e.message}"
                            )
                        }

                        val typeOfMediaOneValue = userData["typeOfMediaOne"] as? String
                        var typeOfMediaOne: TypeOfMedia? = TypeOfMedia.UNKNOWN
                        try {
                            if (typeOfMediaOneValue != null) {
                                typeOfMediaOne = when (typeOfMediaOneValue) {
                                    "IMAGE" -> TypeOfMedia.IMAGE
                                    "VIDEO" -> TypeOfMedia.VIDEO
                                    else -> TypeOfMedia.UNKNOWN
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "FirebaseRepository",
                                "Invalid typeOfMediaOne value: ${typeOfMediaOneValue}"
                            )
                        }

                        val typeOfMediaTwoValue = userData["typeOfMediaTwo"] as? String
                        var typeOfMediaTwo: TypeOfMedia? = TypeOfMedia.UNKNOWN
                        try {
                            if (typeOfMediaTwoValue != null) {
                                typeOfMediaTwo = when (typeOfMediaTwoValue) {
                                    "IMAGE" -> TypeOfMedia.IMAGE
                                    "VIDEO" -> TypeOfMedia.VIDEO
                                    else -> TypeOfMedia.UNKNOWN
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "FirebaseRepository",
                                "Invalid typeOfMediaTwo value: ${typeOfMediaTwoValue}"
                            )
                        }

                        val typeOfMediaThreeValue = userData["typeOfMediaThree"] as? String
                        var typeOfMediaThree: TypeOfMedia? = TypeOfMedia.UNKNOWN
                        try {
                            if (typeOfMediaThreeValue != null) {
                                typeOfMediaThree = when (typeOfMediaThreeValue) {
                                    "IMAGE" -> TypeOfMedia.IMAGE
                                    "VIDEO" -> TypeOfMedia.VIDEO
                                    else -> TypeOfMedia.UNKNOWN
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "FirebaseRepository",
                                "Invalid typeOfMediaThree value: ${typeOfMediaThreeValue}"
                            )
                        }

                        val typeOfMediaFourValue = userData["typeOfMediaFour"] as? String
                        var typeOfMediaFour: TypeOfMedia? = TypeOfMedia.UNKNOWN
                        try {
                            if (typeOfMediaFourValue != null) {
                                typeOfMediaFour = when (typeOfMediaFourValue) {
                                    "IMAGE" -> TypeOfMedia.IMAGE
                                    "VIDEO" -> TypeOfMedia.VIDEO
                                    else -> TypeOfMedia.UNKNOWN
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "FirebaseRepository",
                                "Invalid typeOfMediaFour value: ${typeOfMediaFourValue}"
                            )
                        }

                        val typeOfMediaFiveValue = userData["typeOfMediaFive"] as? String
                        var typeOfMediaFive: TypeOfMedia? = TypeOfMedia.UNKNOWN
                        try {
                            if (typeOfMediaFiveValue != null) {
                                typeOfMediaFive = when (typeOfMediaFiveValue) {
                                    "IMAGE" -> TypeOfMedia.IMAGE
                                    "VIDEO" -> TypeOfMedia.VIDEO
                                    else -> TypeOfMedia.UNKNOWN
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "FirebaseRepository",
                                "Invalid typeOfMediaFive value: ${typeOfMediaFiveValue}"
                            )
                        }

                        val typeOfMediaSixValue = userData["typeOfMediaSix"] as? String
                        var typeOfMediaSix: TypeOfMedia? = TypeOfMedia.UNKNOWN
                        try {
                            if (typeOfMediaSixValue != null) {
                                typeOfMediaSix = when (typeOfMediaSixValue) {
                                    "IMAGE" -> TypeOfMedia.IMAGE
                                    "VIDEO" -> TypeOfMedia.VIDEO
                                    else -> TypeOfMedia.UNKNOWN
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "FirebaseRepository",
                                "Invalid typeOfMediaSix value: ${typeOfMediaSixValue}"
                            )
                        }

                        val typeOfMediaSevenValue = userData["typeOfMediaSeven"] as? String
                        var typeOfMediaSeven: TypeOfMedia? = TypeOfMedia.UNKNOWN
                        try {
                            if (typeOfMediaSevenValue != null) {
                                typeOfMediaSeven = when (typeOfMediaSevenValue) {
                                    "IMAGE" -> TypeOfMedia.IMAGE
                                    "VIDEO" -> TypeOfMedia.VIDEO
                                    else -> TypeOfMedia.UNKNOWN
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "FirebaseRepository",
                                "Invalid typeOfMediaSeven value: ${typeOfMediaSevenValue}"
                            )
                        }

                        val typeOfMediaEightValue = userData["typeOfMediaEight"] as? String
                        var typeOfMediaEight: TypeOfMedia? = TypeOfMedia.UNKNOWN
                        try {
                            if (typeOfMediaEightValue != null) {
                                typeOfMediaEight = when (typeOfMediaEightValue) {
                                    "IMAGE" -> TypeOfMedia.IMAGE
                                    "VIDEO" -> TypeOfMedia.VIDEO
                                    else -> TypeOfMedia.UNKNOWN
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "FirebaseRepository",
                                "Invalid typeOfMediaEight value: ${typeOfMediaEightValue}"
                            )
                        }

                        val typeOfMediaNineValue = userData["typeOfMediaNine"] as? String
                        var typeOfMediaNine: TypeOfMedia? = TypeOfMedia.UNKNOWN
                        try {
                            if (typeOfMediaNineValue != null) {
                                typeOfMediaNine = when (typeOfMediaNineValue) {
                                    "IMAGE" -> TypeOfMedia.IMAGE
                                    "VIDEO" -> TypeOfMedia.VIDEO
                                    else -> TypeOfMedia.UNKNOWN
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "FirebaseRepository",
                                "Invalid typeOfMediaNine value: ${typeOfMediaNineValue}"
                            )
                        }

                        val typeOfMediaProfileVideoValue =
                            userData["typeOfMediaProfileVideo"] as? String
                        var typeOfMediaProfileVideo: TypeOfMedia? = TypeOfMedia.UNKNOWN
                        try {
                            if (typeOfMediaProfileVideoValue != null) {
                                typeOfMediaProfileVideo = when (typeOfMediaProfileVideoValue) {
                                    "IMAGE" -> TypeOfMedia.IMAGE
                                    "VIDEO" -> TypeOfMedia.VIDEO
                                    else -> TypeOfMedia.UNKNOWN
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "FirebaseRepository",
                                "Invalid typeOfMediaProfileVideo value: ${typeOfMediaProfileVideoValue}"
                            )
                        }

                        val mediaOneValue = userData["mediaOne"] as? String
                        var mediaOne: String? = ""
                        try {
                            if (mediaOneValue != null) {
                                mediaOne = mediaOneValue
                            }
                        } catch (e: Exception) {
                            Log.e("FirebaseRepository", "Invalid mediaOne value: ${mediaOneValue}")
                        }
                        Log.d("FirebaseRepository", "mediaOne upon return - $mediaOne")
                        val mediaTwoValue = userData["mediaTwo"] as? String
                        var mediaTwo: String? = ""
                        try {
                            if (mediaTwoValue != null) {
                                mediaTwo = mediaTwoValue
                            }
                        } catch (e: Exception) {
                            Log.e("FirebaseRepository", "Invalid mediaTwo value: ${mediaTwoValue}")
                        }

                        val mediaThreeValue = userData["mediaThree"] as? String
                        var mediaThree: String? = ""
                        try {
                            if (mediaThreeValue != null) {
                                mediaThree = mediaThreeValue
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "FirebaseRepository",
                                "Invalid mediaThree value: ${mediaThreeValue}"
                            )
                        }

                        val mediaFourValue = userData["mediaFour"] as? String
                        var mediaFour: String? = ""
                        try {
                            if (mediaFourValue != null) {
                                mediaFour = mediaFourValue
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "FirebaseRepository",
                                "Invalid mediaFour value: ${mediaFourValue}"
                            )
                        }

                        val mediaFiveValue = userData["mediaFive"] as? String
                        var mediaFive: String? = ""
                        try {
                            if (mediaFiveValue != null) {
                                mediaFive = mediaFiveValue
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "FirebaseRepository",
                                "Invalid mediaFive value: ${mediaFiveValue}"
                            )
                        }

                        val mediaSixValue = userData["mediaSix"] as? String
                        var mediaSix: String? = ""
                        try {
                            if (mediaSixValue != null) {
                                mediaSix = mediaSixValue
                            }
                        } catch (e: Exception) {
                            Log.e("FirebaseRepository", "Invalid mediaSix value: ${mediaSixValue}")
                        }

                        val mediaSevenValue = userData["mediaSeven"] as? String
                        var mediaSeven: String? = ""
                        try {
                            if (mediaSevenValue != null) {
                                mediaSeven = mediaSevenValue
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "FirebaseRepository",
                                "Invalid mediaSeven value: ${mediaSevenValue}"
                            )
                        }

                        val mediaEightValue = userData["mediaEight"] as? String
                        var mediaEight: String? = ""
                        try {
                            if (mediaEightValue != null) {
                                mediaEight = mediaEightValue
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "FirebaseRepository",
                                "Invalid mediaEight value: ${mediaEightValue}"
                            )
                        }

                        val mediaNineValue = userData["mediaNine"] as? String
                        var mediaNine: String? = ""
                        try {
                            if (mediaNineValue != null) {
                                mediaNine = mediaNineValue
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "FirebaseRepository",
                                "Invalid mediaNine value: ${mediaNineValue}"
                            )
                        }

                        val mediaProfileVideoValue = userData["mediaProfileVideo"] as? String
                        var mediaProfileVideo: String? = ""
                        try {
                            if (mediaProfileVideoValue != null) {
                                mediaProfileVideo = mediaProfileVideoValue
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "FirebaseRepository",
                                "Invalid mediaProfileVideo value: ${mediaProfileVideoValue}"
                            )
                        }

                        val genderValue = userData["gender"] as? String
                        var gender: Gender? = Gender.UNKNOWN
                        try {
                            if (genderValue != null) {
                                gender = when (genderValue) {
                                    "MAN" -> Gender.MAN
                                    "WOMAN" -> Gender.WOMAN
                                    "NON_BINARY" -> Gender.NON_BINARY
                                    else -> Gender.UNKNOWN
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("FirebaseRepository", "Invalid gender value: ${genderValue}")
                        }

                        val lookingForValue = userData["lookingFor"] as? String
                        var lookingFor: LookingFor? = LookingFor.UNKNOWN
                        try {
                            if (lookingForValue != null) {
                                lookingFor = when (lookingForValue) {
                                    "LONG_TERM" -> LookingFor.LONG_TERM
                                    "SHORT_TERM" -> LookingFor.SHORT_TERM
                                    "LONG_TERM_BUT_OPEN_MINDED" -> LookingFor.LONG_TERM_BUT_OPEN_MINDED
                                    "SHORT_TERM_BUT_OPEN_MINDED" -> LookingFor.SHORT_TERM_BUT_OPEN_MINDED
                                    "FRIENDS" -> LookingFor.FRIENDS
                                    "UNSURE" -> LookingFor.UNSURE
                                    else -> LookingFor.UNKNOWN
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "FirebaseRepository",
                                "Invalid lookingFor value: ${lookingForValue}"
                            )
                        }

                        val aboutMeValue = userData["aboutMe"] as? String
                        var aboutMe: String? = ""
                        try {
                            if (aboutMeValue != null) {
                                aboutMe = aboutMeValue
                            }
                        } catch (e: Exception) {
                            Log.e("FirebaseRepository", "Invalid aboutMe value: ${aboutMeValue}")
                        }

                        val linkValue = userData["link"] as? String
                        var link: String? = ""
                        try {
                            if (linkValue != null) {
                                link = linkValue
                            }
                        } catch (e: Exception) {
                            Log.e("FirebaseRepository", "Invalid link value: ${linkValue}")
                        }


                        val heightValue = userData["height"] as? String
                        var height: String? = ""
                        try {
                            if (heightValue != null) {
                                height = heightValue
                            }
                        } catch (e: Exception) {
                            Log.e("FirebaseRepository", "Invalid height value: ${heightValue}")
                        }


                        val workValue = userData["work"] as? String
                        var work: String? = ""
                        try {
                            if (workValue != null) {
                                work = workValue
                            }
                        } catch (e: Exception) {
                            Log.e("FirebaseRepository", "Invalid work value: ${workValue}")
                        }


                        val educationValue = userData["education"] as? String
                        var education: Education? = Education.UNKNOWN
                        try {
                            if (!educationValue.isNullOrEmpty()) {
                                education = when (educationValue.trim()) {
                                    "SOME_HIGH_SCHOOL" -> Education.SOME_HIGH_SCHOOL
                                    "HIGH_SCHOOL" -> Education.HIGH_SCHOOL
                                    "SOME_COLLEGE" -> Education.SOME_COLLEGE
                                    "UNDERGRAD_DEGREE" -> Education.UNDERGRAD_DEGREE
                                    "SOME_GRAD_SCHOOL" -> Education.SOME_GRAD_SCHOOL
                                    "GRAD_DEGREE" -> Education.GRAD_DEGREE
                                    "TECH_TRADE_SCHOOL" -> Education.TECH_TRADE_SCHOOL
                                    else -> Education.UNKNOWN
                                }

                                if (education == null) {
                                    Log.e(
                                        "FirebaseRepository",
                                        "Invalid education value: $educationValue"
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "FirebaseRepository",
                                "Error parsing education value: $educationValue",
                                e
                            )
                        }


//                    var education = if (educationValue != null) {
//                        try {
//                            education = when(educationValue) {
//
//                            }
//                            Education.valueOf(educationValue)
//                        } catch (e: IllegalArgumentException) {
//                            // Handle invalid value appropriately
//                            e.message?.let { Log.e("FirebaseRepository", it) }
//                            Log.e("FirebaseRepository", "Invalid education value: $educationValue")
//                            null
//                        }
//                    } else {
//                        // Handle the case where lookingForValue is null
//                        // For example, you might want to set a default value or log a message
//                        Log.d("FirebaseRepository", "education value is null")
//                        null
//                    }

                        val kidsValue = userData["kids"] as? String
                        var kids: Kids? = Kids.UNKNOWN
                        try {
                            if (kidsValue != null) {
                                kids = when (kidsValue) {
                                    "ONE_DAY" -> Kids.ONE_DAY
                                    "DONT_WANT" -> Kids.DONT_WANT
                                    "HAVE_AND_WANT_MORE" -> Kids.HAVE_AND_WANT_MORE
                                    "HAVE_AND_DONT_WANT_MORE" -> Kids.HAVE_AND_DONT_WANT_MORE
                                    "UNSURE" -> Kids.UNSURE
                                    else -> Kids.UNKNOWN
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("FirebaseRepository", "Invalid kids value: ${kidsValue}")
                        }


                        val religionValue = userData["religion"] as? String
                        var religion: Religion? = Religion.UNKNOWN
                        try {
                            if (religionValue != null) {
                                religion = when (religionValue) {
                                    "CHRISTIANITY" -> Religion.CHRISTIANITY
                                    "ISLAM" -> Religion.ISLAM
                                    "HINDIUSM" -> Religion.HINDUISM
                                    "BUDDHISM" -> Religion.BUDDHISM
                                    "SIKHISM" -> Religion.SIKHISM
                                    "JUDAISM" -> Religion.JUDAISM
                                    "BAHA'I FAITH" -> Religion.BAHAI_FAITH
                                    "CONFUCIANISM" -> Religion.CONFUCIANISM
                                    "JAINISM" -> Religion.JAINISM
                                    "SHINTOISM" -> Religion.SHINTOISM
                                    else -> Religion.UNKNOWN
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("FirebaseRepository", "Invalid religion value: ${religionValue}")
                        }


                        val petsValue = userData["pets"] as? String
                        var pets: Pets? = Pets.UNKNOWN
                        try {
                            if (petsValue != null) {
                                pets = when (petsValue) {
                                    "DOG" -> Pets.DOG
                                    "CAT" -> Pets.CAT
                                    "FISH" -> Pets.FISH
                                    "HAMSTER_OR_GUINEA_PIG" -> Pets.HAMSTER_OR_GUINEA_PIG
                                    "BIRD" -> Pets.BIRD
                                    "RABBIT" -> Pets.RABBIT
                                    "REPTILE" -> Pets.REPTILE
                                    "AMPHIBIAN" -> Pets.AMPHIBIAN
                                    else -> Pets.UNKNOWN
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("FirebaseRepository", "Invalid pets value: ${petsValue}")
                        }

                        val exerciseValue = userData["exercise"] as? String
                        var exercise: Exercise? = Exercise.UNKNOWN
                        try {
                            if (exerciseValue != null) {
                                exercise = when (exerciseValue) {
                                    "OFTEN" -> Exercise.OFTEN
                                    "SOMETIMES" -> Exercise.SOMETIMES
                                    "RARELY" -> Exercise.RARELY
                                    "NEVER" -> Exercise.NEVER
                                    else -> Exercise.UNKNOWN
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("FirebaseRepository", "Invalid exercise value: ${exerciseValue}")
                        }


                        val smokingValue = userData["smoking"] as? String
                        var smoking: Smoking? = Smoking.UNKNOWN
                        try {
                            if (smokingValue != null) {
                                smoking = when (smokingValue) {
                                    "YES" -> Smoking.YES
                                    "ON_OCCASION" -> Smoking.ON_OCCASION
                                    "NEVER_SMOKE" -> Smoking.NEVER_SMOKE
                                    else -> Smoking.UNKNOWN
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("FirebaseRepository", "Invalid smoking value: ${smokingValue}")
                        }

                        val drinkingValue = userData["drinking"] as? String
                        var drinking: Drinking? = Drinking.UNKNOWN
                        try {
                            if (drinkingValue != null) {
                                drinking = when (drinkingValue) {
                                    "YES" -> Drinking.YES
                                    "ON_OCCASION" -> Drinking.ON_OCCASION
                                    "NEVER_DRINK" -> Drinking.NEVER_DRINK
                                    else -> Drinking.UNKNOWN
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("FirebaseRepository", "Invalid drinking value: ${drinkingValue}")
                        }

                        val marijuanaValue = userData["marijuana"] as? String
                        var marijuana: Marijuana? = Marijuana.UNKNOWN
                        try {
                            if (marijuanaValue != null) {
                                marijuana = when (marijuanaValue) {
                                    "YES" -> Marijuana.YES
                                    "ON_OCCASION" -> Marijuana.ON_OCCASION
                                    "NEVER" -> Marijuana.NEVER
                                    else -> Marijuana.UNKNOWN
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "FirebaseRepository",
                                "Invalid marijuana value: ${marijuanaValue}"
                            )
                        }


                        val promptOneAnswerValue = userData["promptOneAnswer"] as? String
                        var promptOneAnswer = promptOneAnswerValue
                        if (promptOneAnswer == null) {
                            promptOneAnswer = ""
                            Log.e(
                                "FirebaseRepository",
                                "Invalid promptOneAnswer value: ${promptOneAnswerValue}"
                            )
                        }

                        val promptTwoAnswerValue = userData["promptTwoAnswer"] as? String
                        var promptTwoAnswer = promptTwoAnswerValue
                        if (promptTwoAnswer == null) {
                            promptTwoAnswer = ""
                            Log.e(
                                "FirebaseRepository",
                                "Invalid promptTwoAnswer value: ${promptTwoAnswerValue}"
                            )
                        }

                        val promptThreeAnswerValue = userData["promptThreeAnswer"] as? String
                        var promptThreeAnswer = promptThreeAnswerValue
                        if (promptThreeAnswer == null) {
                            promptThreeAnswer = ""
                            Log.e(
                                "FirebaseRepository",
                                "Invalid promptThreeAnswer value: ${promptThreeAnswerValue}"
                            )
                        }

                        val promptOneQuestionValue = userData["promptOneQuestion"] as? String
                        var promptOneQuestion = promptOneQuestionValue
                        if (promptOneQuestion == null) {
                            promptOneQuestion = ""
                            Log.e(
                                "FirebaseRepository",
                                "Invalid promptOneQuestion value: ${promptOneQuestionValue}"
                            )
                        }

                        val promptTwoQuestionValue = userData["promptTwoQuestion"] as? String
                        var promptTwoQuestion = promptTwoQuestionValue
                        if (promptTwoQuestion == null) {
                            promptTwoQuestion = ""
                            Log.e(
                                "FirebaseRepository",
                                "Invalid promptTwoQuestion value: ${promptTwoQuestionValue}"
                            )
                        }

                        val promptThreeQuestionValue = userData["promptThreeQuestion"] as? String
                        var promptThreeQuestion = promptThreeQuestionValue
                        if (promptThreeQuestion == null) {
                            promptThreeQuestion = ""
                            Log.e(
                                "FirebaseRepository",
                                "Invalid promptThreeQuestion value: $promptThreeQuestionValue"
                            )
                        }

                        val showMeValue = userData["showMe"] as? String
                        var showMe: ShowMe? = ShowMe.UNKNOWN
                        try {
                            if (showMeValue != null) {
                                showMe = when (showMeValue) {
                                    "MEN" -> ShowMe.MEN
                                    "WOMEN" -> ShowMe.WOMEN
                                    "ANYONE" -> ShowMe.ANYONE
                                    else -> ShowMe.WOMEN
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("FirebaseRepository", "Invalid showMe value: ${showMeValue}")
                        }

                        val showUsersValue = userData["showUsers"] as? String
                        var showUsers: Distance = Distance.TEN
                        try {
                            if (showUsersValue != null) {
                                showUsers = when (showUsersValue) {
                                    "TEN" -> Distance.TEN
                                    "TWENTY" -> Distance.TWENTY
                                    "THIRTY" -> Distance.THIRTY
                                    "FORTY" -> Distance.FORTY
                                    "FIFTY" -> Distance.FIFTY
                                    "SIXTY" -> Distance.SIXTY
                                    "SEVENTY" -> Distance.SEVENTY
                                    "EIGHTY" -> Distance.EIGHTY
                                    "NINETY" -> Distance.NINETY
                                    "ONE_HUNDRED" -> Distance.ONE_HUNDRED
                                    "ANYWHERE" -> Distance.ANYWHERE
                                    else -> Distance.TEN
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("FirebaseRepository", "Invalid showUsers value: ${showUsersValue}")
                        }

                        val acceptShotsValue = userData["acceptShots"] as? String
                        var acceptShots: Distance = Distance.TEN
                        try {
                            if (acceptShotsValue != null) {
                                acceptShots = when (acceptShotsValue) {
                                    "TEN" -> Distance.TEN
                                    "TWENTY" -> Distance.TWENTY
                                    "THIRTY" -> Distance.THIRTY
                                    "FORTY" -> Distance.FORTY
                                    "FIFTY" -> Distance.FIFTY
                                    "SIXTY" -> Distance.SIXTY
                                    "SEVENTY" -> Distance.SEVENTY
                                    "EIGHTY" -> Distance.EIGHTY
                                    "NINETY" -> Distance.NINETY
                                    "ONE_HUNDRED" -> Distance.ONE_HUNDRED
                                    "ANYWHERE" -> Distance.ANYWHERE
                                    else -> Distance.TEN
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("FirebaseRepository", "Invalid acceptShots value: ${acceptShotsValue}")
                        }

                        val ageMinToShowValue = userData["ageMinToShow"] as? Number
                        var ageMinToShow = 18
                        try {
                            ageMinToShowValue?.let {
                                ageMinToShow = it.toInt()
                            }
                        } catch (e: NumberFormatException) {
                            Log.e(
                                "FirebaseRepository",
                                "Invalid ageMinToShow value: $ageMinToShowValue"
                            )
                            // Handle the error appropriately
                        }

                        val ageMaxToShowValue = userData["ageMaxToShow"] as? Number
                        var ageMaxToShow = 35
                        try {
                            ageMaxToShowValue?.let {
                                ageMaxToShow = it.toInt()
                            }
                        } catch (e: NumberFormatException) {
                            Log.e(
                                "FirebaseRepository",
                                "Invalid ageMaxToShow value: $ageMaxToShowValue"
                            )
                            // Handle the error appropriately
                        }

                        val newLikesCountValue = userData["newLikesCount"] as? Number
                        var newLikesCount = 0
                        try {
                            newLikesCountValue?.let {
                                newLikesCount = it.toInt()
                            }
                        } catch (e: NumberFormatException) {
                            Log.e(
                                "FirebaseRepository",
                                "Invalid newLikesCount value: $newLikesCountValue"
                            )
                            // Handle the error appropriately
                        }

//                    val newBookmarksCountValue = userData["newBookmarksCount"] as? Number
//                    var newBookmarksCount = 0
//                    try {
//                        newBookmarksCountValue?.let {
//                            newBookmarksCount = it.toInt()
//                        }
//                    } catch (e: NumberFormatException) {
//                        Log.e(
//                            "FirebaseRepository",
//                            "Invalid newBookmarksCount value: $newBookmarksCountValue"
//                        )
//                        // Handle the error appropriately
//                    }

                        val newShotsCountValue = userData["newShotsCount"] as? Number
                        var newShotsCount = 0
                        try {
                            newShotsCountValue?.let {
                                newShotsCount = it.toInt()
                            }
                        } catch (e: NumberFormatException) {
                            Log.e(
                                "FirebaseRepository",
                                "Invalid newShotsCount value: $newShotsCountValue"
                            )
                            // Handle the error appropriately
                        }

                        val newMessagesCountValue = userData["newMessagesCount"] as? Number
                        var newMessagesCount = 0
                        try {
                            newMessagesCountValue?.let {
                                newMessagesCount = it.toInt()
                            }
                        } catch (e: NumberFormatException) {
                            Log.e(
                                "FirebaseRepository",
                                "Invalid newMessagesCount value: $newMessagesCountValue"
                            )
                            // Handle the error appropriately
                        }

                        val timesBookmarkedCountValue = userData["timesBookmarkedCount"] as? Number
                        var timesBookmarkedCount = 0
                        try {
                            timesBookmarkedCountValue?.let {
                                timesBookmarkedCount = it.toInt()
                            }
                        } catch (e: NumberFormatException) {
                            Log.e(
                                "FirebaseRepository",
                                "Invalid timesBookmarkedCount value: $timesBookmarkedCountValue"
                            )
                            // Handle the error appropriately
                        }

                        val likesCountValue = userData["likesCount"] as? Number
                        var likesCount = 0
                        try {
                            likesCountValue?.let {
                                likesCount = it.toInt()
                            }
                        } catch (e: NumberFormatException) {
                            Log.e(
                                "FirebaseRepository",
                                "Invalid likesCount value: $likesCountValue"
                            )
                            // Handle the error appropriately
                        }

                        val shotsCountValue = userData["shotsCount"] as? Number
                        var shotsCount = 0
                        try {
                            shotsCountValue?.let {
                                shotsCount = it.toInt()
                            }
                        } catch (e: NumberFormatException) {
                            Log.e(
                                "FirebaseRepository",
                                "Invalid shotsCount value: $shotsCountValue"
                            )
                            // Handle the error appropriately
                        }

                        return User(
                            userId,
                            displayName,
                            userName,
                            latitude,
                            longitude,
                            birthday,
                            mediaOne,
                            typeOfMediaOne,
                            mediaTwo,
                            typeOfMediaTwo,
                            mediaThree,
                            typeOfMediaThree,
                            mediaFour,
                            typeOfMediaFour,
                            mediaFive,
                            typeOfMediaFive,
                            mediaSix,
                            typeOfMediaSix,
                            mediaSeven,
                            typeOfMediaSeven,
                            mediaEight,
                            typeOfMediaEight,
                            mediaNine,
                            typeOfMediaNine,
                            mediaProfileVideo,
                            typeOfMediaProfileVideo,
                            aboutMe,
                            link,
                            height,
                            lookingFor,
                            gender,
                            SexualOrientation.UNKNOWN,
                            work,
                            education,
                            kids,
                            religion,
                            pets,
                            exercise,
                            smoking,
                            drinking,
                            marijuana,
                            promptOneAnswer,
                            promptTwoAnswer,
                            promptThreeAnswer,
                            promptOneQuestion,
                            promptTwoQuestion,
                            promptThreeQuestion,
                            showMe,
                            showUsers,
                            acceptShots,
                            ageMinToShow,
                            ageMaxToShow,
                            newLikesCount,
                            newShotsCount,
                            newMessagesCount,
                            timesBookmarkedCount,
                            likesCount,
                            shotsCount
                        )

                    } else {
                        Log.d("FirebaseRepository", "Missing required fields in user data")
                    }
                } else {
                    Log.d("FirebaseRepository", "User document does not exist")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("FirebaseRepository", "Error fetching user data: $e - ${e.message}")
            }
        } catch (e: Exception) {
            Log.d("FirebaseRepository", "Error fetching user data: ${e.message}")
        }
        return null
    }

    suspend fun fetchMediaUrls(userId: String): MutableMap<String, String> {
        val storage = Firebase.storage
        val userRef = storage.reference.child("users/$userId/gallery")
        val mediaMap = mutableMapOf<String, String>()
        try {
            val items = userRef.listAll().await()
            for (item in items.items) {
                when {
                    item.name.startsWith("mediaOne") -> {
                        mediaMap["mediaOne"] = item.name
                    }

                    item.name.startsWith("mediaTwo") -> {
                        mediaMap["mediaTwo"] = item.name
                    }

                    item.name.startsWith("mediaThree") -> {
                        mediaMap["mediaThree"] = item.name
                    }

                    item.name.startsWith("mediaFour") -> {
                        mediaMap["mediaFour"] = item.name
                    }

                    item.name.startsWith("mediaFive") -> {
                        mediaMap["mediaFive"] = item.name
                    }

                    item.name.startsWith("mediaSix") -> {
                        mediaMap["mediaSix"] = item.name
                    }

                    item.name.startsWith("mediaSeven") -> {
                        mediaMap["mediaSeven"] = item.name
                    }

                    item.name.startsWith("mediaEight") -> {
                        mediaMap["mediaEight"] = item.name
                    }

                    item.name.startsWith("mediaNine") -> {
                        mediaMap["mediaNine"] = item.name
                    }
                }
            }
            return mediaMap
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("FirebaseRepository", "Error fetching URLs: $e")
        }
        return mediaMap
    }

//    suspend fun writeBookmarksToFirebase(
//        bookmarkId: String,
//        userData: MutableMap<String, Any>
//    ): Boolean {
//        try {
//            firebaseFirestore.collection("users").document(bookmarkId)
//                .set(userData, SetOptions.merge()).await()
//        } catch (e: Exception) {
//            Log.d("FirebaseRepository", "writing bookmarks to Firebase failed! - ${e.message}")
//            return false
//        }
//        return true
//    }

    //the methods for bookmark below

    suspend fun writeBookmarksToFirebase(
        userId: String,
        bookmarkData: MutableMap<String, Any>
    ): Boolean {
        return try {
            val yourUserId = firebaseAuth.currentUser?.displayName ?: ""
            val userRef = firebaseFirestore.collection("users").document(yourUserId)
            val bookmarkRef = userRef.collection("bookmarks").document(yourUserId)
                .set(bookmarkData, SetOptions.merge()).await()
            Log.d("FirebaseRepository", "Bookmarks added successfully")
            true
        } catch (e: Exception) {
            Log.d("FirebaseRepository", "Error adding bookmarks: ${e.message}")
            // Handle any potential errors here
            false
        }
    }


    suspend fun getBookmarksFromFirebase(userId: String): List<String> {
        if (userId.isBlank()) {
            return emptyList()
        }

        val bookmarkList = mutableListOf<String>()

        try {
            val bookmarkRef = firebaseFirestore
                .collection("users")
                .document(userId)
                .collection("bookmarks")

            val collection = bookmarkRef.get().await()

            for (document in collection.documents) {
                for (field in document.data?.keys.orEmpty()) {
                    Log.d("FirebaseRepository", "field = $field")
                    if (field.startsWith("bookmark-")) {
//                        val bookmarkValue = document.getString(field)
                        val bookmarkValue = field.removePrefix("bookmark-")
                        Log.d("FirebaseRepository", "bookmarkValue = $bookmarkValue")
                        bookmarkValue.let {
                            bookmarkList.add(it)
                        }
                    }
                }
            }
            Log.d("FirebaseRepository", "Finished bookmarkList - $bookmarkList")
            return bookmarkList
        } catch (e: Exception) {
            Log.d("FirebaseRepository", "Something went wrong fetching bookmarks: ${e.message}")
        }

        return emptyList()
    }


    suspend fun deleteBookmarkFromFirebase(userId: String?): Boolean {
        if (userId == null) {
            Log.d("FirebaseRepository", "Invalid userId: null")
            return false // Error
        }

        return try {
            val yourUserId = firebaseAuth.currentUser?.displayName ?: ""
            val bookmarkDocRef = firebaseFirestore
                .collection("users")
                .document(yourUserId)
                .collection("bookmarks")
                .document(yourUserId)

            val field = "bookmark-${userId}"

            val updateData = hashMapOf<String, Any>(
                field to FieldValue.delete()
            )

            bookmarkDocRef.update(updateData)
                .addOnSuccessListener {
                    Log.d("FirebaseRepository", "Field updated successfully")
                }
                .addOnFailureListener { exception ->
                    Log.d("FirebaseRepository", "Error updating field: ${exception.message}")
                }
//            bookmarkDocRef.get().addOnSuccessListener { querySnapshot ->
//                for (document in querySnapshot.documents) {
//                    // Access the document data here
//                    val bookmarkData = document.data
//                    Log.d("FirebaseRepository", "bookmarkData: ${document.data}")
//                    // Process the bookmark data as needed
//                    // ...
//                }
//            }.addOnFailureListener { exception ->
//                // Handle any errors that occur during the query
//                Log.d("FirebaseRepository", "Error getting bookmarks: ${exception.message}")
//            }

//            val bookmarkBefore = bookmarkDocRef.get().await()
//            val bookmarkDataBefore = bookmarkBefore.getString("bookmark-$userId")

            Log.d("FirebaseRepository", "The userId right before attempting the deletion - $userId")

            val updates = hashMapOf<String, Any>(
                "bookmark-$userId" to FieldValue.delete()
            )

//            bookmarkDocRef.update(updates).await()

//            val bookmarkAfter = bookmarkDocRef.get().await()
//            val bookmarkDataAfter = bookmarkAfter.getString("bookmark-$userId")

            Log.d("FirebaseRepository", "The userId right after attempting the deletion - $userId")

//            Log.d("FirebaseRepository", "Bookmark data before deletion: $bookmarkDataBefore")
//            Log.d("FirebaseRepository", "Bookmark data after deletion: $bookmarkDataAfter")

            true
        } catch (e: Exception) {
            Log.d("FirebaseRepository", "Error deleting bookmark: ${e.message}")
            false
        }
    }


    //the methods for sentLike below

    suspend fun writeSentLikeToFirebase(
        userId: String,
        sentLikeData: MutableMap<String, Any>
    ): Boolean {
        return try {
            val yourUserId = firebaseAuth.currentUser?.displayName ?: ""
            val userRef = firebaseFirestore.collection("users").document(yourUserId)
            val sentLikeRef = userRef.collection("sentLikes").document(yourUserId)
                .set(sentLikeData, SetOptions.merge()).await()
            Log.d("FirebaseRepository", "sentLikes added successfully")
            true
        } catch (e: Exception) {
            Log.d("FirebaseRepository", "Error adding sentLikes: ${e.message}")
            // Handle any potential errors here
            false
        }
    }


    suspend fun getSentLikesFromFirebase(userId: String): List<String> {
        if (userId.isBlank()) {
            return emptyList()
        }
        val yourUserId = firebaseAuth.currentUser?.displayName ?: ""
        val sentLikeList = mutableListOf<String>()

        try {
            val sentLikeRef = firebaseFirestore
                .collection("users")
                .document(yourUserId)
                .collection("sentLikes")

            val collection = sentLikeRef.get().await()

            for (document in collection.documents) {
                for (field in document.data?.keys.orEmpty()) {
                    Log.d("FirebaseRepository", "field = $field")
                    if (field.startsWith("sentLike-")) {
//                        val sentLikeValue = document.getString(field)?.removePrefix("sentLike-")
                        val sentLikeValue = field.removePrefix("sentLike-")
                        Log.d("FirebaseRepository", "sentLikeValue = $sentLikeValue")
                        sentLikeValue.let {
                            sentLikeList.add(it)
                        }
                    }
                }
            }
            Log.d("FirebaseRepository", "Finished sentLikeList - $sentLikeList")
            return sentLikeList
        } catch (e: Exception) {
            Log.d("FirebaseRepository", "Something went wrong fetching sentLikes: ${e.message}")
        }

        return emptyList()
    }


    suspend fun deleteSentLikeFromFirebase(userId: String?): Boolean {
        if (userId == null) {
            Log.d("FirebaseRepository", "Invalid userId: null")
            return false // Error
        }

        return try {
            val yourUserId = firebaseAuth.currentUser?.displayName ?: ""
            val sentLikeRef = firebaseFirestore
                .collection("users")
                .document(yourUserId)
                .collection("sentLikes")
                .document(yourUserId)

            val updates = hashMapOf<String, Any>(
                "sentLike-$userId" to FieldValue.delete()
            )

            sentLikeRef.update(updates).addOnSuccessListener {
                Log.d("FirebaseRepository", "Field 'sentLike-$userId' deleted successfully")
            }.addOnFailureListener {
                Log.d(
                    "FirebaseRepository",
                    "Error deleting field 'sentLike-$userId': ${it.message}"
                )
            }.await() // Await the completion of the update operation

            true
        } catch (e: Exception) {
            Log.d("FirebaseRepository", "Error deleting field 'sentLike-$userId': ${e.message}")
            false
        }
    }


    //the methods for receivedLike below

    suspend fun writeReceivedLikeToFirebase(
        userId: String,
        receivedLikeData: MutableMap<String, Any>
    ): Boolean {
        return try {
            val userRef = firebaseFirestore.collection("users").document(userId)
            val receivedLikeRef = userRef.collection("receivedLikes")
                .document(firebaseAuth.currentUser?.displayName ?: "")
                .set(receivedLikeData, SetOptions.merge()).await()
            Log.d("FirebaseRepository", "receivedLikes added successfully")
            true
        } catch (e: Exception) {
            Log.d("FirebaseRepository", "Error adding receivedLikes: ${e.message}")
            // Handle any potential errors here
            false
        }
    }


    suspend fun getReceivedLikesFromFirebase(userId: String): List<String> {
        if (userId.isBlank()) {
            return emptyList()
        }

        val receivedLikeList = mutableListOf<String>()

        try {
            val receivedLikeRef = firebaseFirestore
                .collection("users")
                .document(userId)
                .collection("receivedLikes")

            val collection = receivedLikeRef.get().await()

            for (document in collection.documents) {
                for (field in document.data?.keys.orEmpty()) {
                    if (field.startsWith("receivedLike-")) {
//                        val receivedLikeValue = document.getString(field)
                        val receivedLikeValue = field.removePrefix("receivedLike-")
                        receivedLikeValue.let {
                            receivedLikeList.add(it)
                        }
                    }
                }
            }

            Log.d("FirebaseRepository", "receivedLikeList = $receivedLikeList")

            return receivedLikeList
        } catch (e: Exception) {
            Log.d("FirebaseRepository", "Something went wrong fetching receivedLikes: ${e.message}")
        }

        return emptyList()
    }


    suspend fun deleteReceivedLikeFromFirebase(userId: String?): Boolean {
        if (userId == null) {
            Log.d("FirebaseRepository", "Invalid userId: null")
            return false // Error
        }

        return try {
            val yourUserId = firebaseAuth.currentUser?.displayName ?: ""
            val receivedLikeDocRef = firebaseFirestore
                .collection("users")
                .document(yourUserId)
                .collection("receivedLikes")
                .document(yourUserId)

            val receivedLikeBefore = receivedLikeDocRef.get().await()
            val receivedLikeDataBefore = receivedLikeBefore.getString("receivedLike-$userId")

            Log.d("FirebaseRepository", "The userId right before attempting the deletion - $userId")

            val updates = hashMapOf<String, Any>(
                "receivedLike-$userId" to FieldValue.delete()
            )

            receivedLikeDocRef.update(updates).await()

            val receivedLikeAfter = receivedLikeDocRef.get().await()
            val receivedLikeDataAfter = receivedLikeAfter.getString("receivedLike-$userId")

            Log.d("FirebaseRepository", "The userId right after attempting the deletion - $userId")

            Log.d(
                "FirebaseRepository",
                "receivedLike data before deletion: $receivedLikeDataBefore"
            )
            Log.d("FirebaseRepository", "receivedLike data after deletion: $receivedLikeDataAfter")


            true
        } catch (e: Exception) {
            Log.d("FirebaseRepository", "Error deleting receivedLike: ${e.message}")
            false
        }
    }


    //the methods for sentShot below

    suspend fun writeSentShotToFirebase(
        userId: String,
        sentShotData: MutableMap<String, Uri>,
        context: Context
    ): Boolean {
        return try {
            val yourUserId = firebaseAuth.currentUser?.displayName ?: ""
            val downloadUrl = uploadSentShotToStorage(userId, sentShotData, context)
            Log.d("FirebaseRepository", "downloadUrl = $downloadUrl")
            val userRef = firebaseFirestore.collection("users").document(yourUserId)
            val sentShotRef = userRef.collection("sentShots")
                .document(userId)
                .set(downloadUrl, SetOptions.merge()).await()
            Log.d("FirebaseRepository", "sentShot added successfully")
            true
        } catch (e: Exception) {
            Log.d("FirebaseRepository", "Error adding sentShot: ${e.message}")
            // Handle any potential errors here
            false
        }
    }

    suspend fun deleteSentShotFromFirebase(userId: String): Boolean {
        val yourUserId = firebaseAuth.currentUser?.displayName ?: ""
        val storageRef = firebaseStorage.reference
        val userRefForStorage = storageRef.child("users/$yourUserId")
        val mediaRef = userRefForStorage.child("gallery/sentShots")
        val userRefForFirestore = firebaseFirestore.collection("users").document(yourUserId)

        try {
            // Delete the downloaded url from Firestore
            userRefForFirestore
                .collection("sentShots").document(userId)
                .delete().await()
            val mediaList = mediaRef.listAll().await()
            for (item in mediaList.items) {
                when {
                    item.name.startsWith("sentShot-$userId") -> {
                        // Delete the existing media with the specified identifier from Storage
                        item.delete().await()
                        Log.d(
                            "FirebaseRepository",
                            "Deleted previous shot that was in the ${"sentShot-$userId"} spot from" +
                                    "firestore and from storage"
                        )
                        // Once deleted, break out of the loop since we've removed the existing media
                        break
                    }
                }
            }
            deleteReceivedShotFromFirebase(userId)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error deleting ${"sentShot-$userId"}: ${e.message}")
            return false
        }
        return true
    }


    private suspend fun uploadSentShotToStorage(
        userId: String,
        sentShotItems: MutableMap<String, Uri> = mutableMapOf(),
        context: Context
    ): Map<String, Uri?> {
        val yourUserId = firebaseAuth.currentUser?.displayName ?: ""
        val downloadUrl = mutableMapOf<String, Uri?>()
        val storageRef = firebaseStorage.reference.child("users/$yourUserId/gallery/sentShots")
        sentShotItems.forEach { (fieldName, uri) ->
            uri.let { validUri ->
                val fileName =
                    "${fieldName}_${System.currentTimeMillis()}"

                deleteSentShotFromFirebase(userId)

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
                    Log.d("FirebaseRepository", "File successfully added to storage")
                    val finishedDownloadUrl = mediaRef.downloadUrl.await()
                    downloadUrl[fieldName] = finishedDownloadUrl
                } catch (e: Exception) {
                    Log.d(
                        "FirebaseRepository",
                        "There was an issue with uploading the file to Firebase. HTTP result and code: ${e.message}"
                    )
                }
            }
        }

        return downloadUrl
    }


    suspend fun getSentShotsFromFirebase(userId: String): List<String> {
        if (userId.isBlank()) {
            return emptyList()
        }
        Log.d("FirebaseRepository", "Inside get SentShotsFromFirebase in repo")
        val sentShotsList = mutableListOf<String>()

        try {
            val sentShotsRef = firebaseFirestore
                .collection("users")
                .document(userId)
                .collection("sentShots")

            val collection = sentShotsRef.get().await()

            for (document in collection.documents) {
                for ((key, value) in document.data.orEmpty()) {
                    if (key.startsWith("sentShot-")) {
                        val fieldUserId = key.removePrefix("sentShot-")
//                        val sentShotValue = value
                        Log.d("FirebaseRepository", "Field key: $key, Field value: $value")
                        sentShotsList.add("$fieldUserId-$value")
                    }
                }
//                for (field in document.data?.keys.orEmpty()) {
//                    if (field.startsWith("sentShot-")) {
//                        val fieldUserId = field.removePrefix("sentShot-")
////                        val sentShotValue = document.getString(field)
//                        val sentShotValue = field.removePrefix("sentShot-")
//                        Log.d("FirebaseRepository", "sentShotValue = $sentShotValue")
//                        sentShotsList.add("$fieldUserId-$sentShotValue")
//                    }
//                }
            }
            Log.d(
                "FirebaseRepository",
                "Inside sentShotViewModel in Firebase - the sentShotsList is $sentShotsList"
            )
            return sentShotsList
        } catch (e: Exception) {
            Log.d("FirebaseRepository", "Something went wrong fetching sentShots: ${e.message}")
        }

        return mutableListOf()
    }


    //the methods for receivedShot below

    suspend fun writeReceivedShotToFirebase(
        userId: String,
        receivedShotData: MutableMap<String, Uri>,
        context: Context
    ): Boolean {
        return try {
            val yourUserId = firebaseAuth.currentUser?.displayName ?: ""
            val downloadUrl = uploadReceivedShotToStorage(userId, receivedShotData, context)
            Log.d("FirebaseRepository", "downloadUrl = $downloadUrl")
            val userRef = firebaseFirestore.collection("users").document(userId)
            val receivedShotRef = userRef.collection("receivedShots")
                .document(yourUserId)
                .set(downloadUrl, SetOptions.merge()).await()
            Log.d("FirebaseRepository", "receivedShot added successfully")
            true
        } catch (e: Exception) {
            Log.d("FirebaseRepository", "Error adding receivedShot: ${e.message}")
            // Handle any potential errors here
            false
        }
    }

    private suspend fun uploadReceivedShotToStorage(
        userId: String,
        receivedShotItems: MutableMap<String, Uri> = mutableMapOf(),
        context: Context
    ): Map<String, Uri?> {
        val downloadUrl = mutableMapOf<String, Uri?>()
        val storageRef = firebaseStorage.reference.child("users/$userId/gallery/receivedShots")
        receivedShotItems.forEach { (fieldName, uri) ->
            uri.let { validUri ->
                val fileName =
                    "${fieldName}_${System.currentTimeMillis()}"

                deleteReceivedShotFromFirebase(userId)

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
                    Log.d("FirebaseRepository", "File successfully added to storage")
                    val finishedDownloadUrl = mediaRef.downloadUrl.await()
                    downloadUrl[fieldName] = finishedDownloadUrl
                } catch (e: Exception) {
                    Log.d(
                        "FirebaseRepository",
                        "There was an issue with uploading the file to Firebase. HTTP result and code: ${e.message}"
                    )
                }
            }
        }

        return downloadUrl
    }


    suspend fun getReceivedShotsFromFirebase(userId: String): List<String> {
        Log.d("FirebaseRepository", "Inside getReceivedShotsFromFirebase!")
        if (userId.isBlank()) {
            Log.d("FirebaseRepository", "userId was blank!")
            return emptyList()
        }

        val receivedShotsList = mutableListOf<String>()

        try {
            val receivedShotRef = firebaseFirestore
                .collection("users")
                .document(userId)
                .collection("receivedShots")

            val collection = receivedShotRef.get().await()

            for (document in collection.documents) {
                for ((key, value) in document.data.orEmpty()) {
                    if (key.startsWith("receivedShot-")) {
                        val fieldUserId = key.removePrefix("receivedShot-")
//                        val receivedShotValue = value
                        Log.d("FirebaseRepository", "Field key: $key, Field value: $value")
                        receivedShotsList.add("$fieldUserId-$value")
                    }
                }
            }

            return receivedShotsList
        } catch (e: Exception) {
            Log.d("FirebaseRepository", "Something went wrong fetching receivedShots: ${e.message}")
        }

        return emptyList()
    }


    suspend fun deleteReceivedShotFromFirebase(userId: String): Boolean {
        val yourUserId = firebaseAuth.currentUser?.displayName ?: ""
        val storageRef = firebaseStorage.reference
        val userRefForStorage = storageRef.child("users/$userId")
        val mediaRef = userRefForStorage.child("gallery/receivedShots")
        val userRefForFirestore = firebaseFirestore.collection("users").document(userId)

        try {
            // Delete the downloaded url from Firestore
            userRefForFirestore
                .collection("receivedShots").document(yourUserId)
                .delete().await()
            val mediaList = mediaRef.listAll().await()
            Log.d(
                "FirebaseRepository",
                "Inside storage for receivedShots, looking for" +
                        " receivedShot-$userId"
            )
            for (item in mediaList.items) {
                when {
                    item.name.startsWith("receivedShot-$userId") -> {
                        // Delete the existing media with the specified identifier from Storage
                        item.delete().await()
                        Log.d(
                            "FirebaseRepository",
                            "Deleted previous shot that was in the ${"receivedShot-$userId"} spot from" +
                                    "firestore and from storage"
                        )
                        // Once deleted, break out of the loop since we've removed the existing media
                        break
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(
                "FirebaseRepository",
                "Error deleting ${"receivedShot-$userId"}: ${e.message}"
            )
            return false
        }
        return true
    }

    // write blockedUser to Firebase

    suspend fun writeBlockedUserToFirebase(
        userId: String,
        blockedUserData: MutableMap<String, Any>
    ): Boolean {
        return try {
            val yourUserId = firebaseAuth.currentUser?.displayName ?: ""
            val userRef = firebaseFirestore.collection("users").document(yourUserId)
            val blockedUserRef = userRef.collection("blockedUsers").document(yourUserId)
                .set(blockedUserData, SetOptions.merge()).await()
            Log.d("FirebaseRepository", "blockedUsers added successfully")

            val userWhoBlockedYouData: MutableMap<String, Any> = mutableMapOf()
            val mediaItems: MutableMap<String, Uri> = mutableMapOf()

            userWhoBlockedYouData["userWhoBlockedYou-$yourUserId"] = yourUserId
            writeUserWhoBlockedYouToFirebase(userId, userWhoBlockedYouData)

            true
        } catch (e: Exception) {
            Log.d("FirebaseRepository", "Error adding blockedUsers: ${e.message}")
            // Handle any potential errors here

            false
        }
    }

    // write userWhoBlockedYou to Firebase

    suspend fun writeUserWhoBlockedYouToFirebase(
        userId: String,
        userWhoBlockedYouData: MutableMap<String, Any>
    ): Boolean {
        return try {
            val yourUserId = firebaseAuth.currentUser?.displayName ?: ""
            val userRef = firebaseFirestore.collection("users").document(userId)
            val usersWhoBlockedYouRef = userRef.collection("usersWhoBlockedYou").document(userId)
                .set(userWhoBlockedYouData, SetOptions.merge()).await()
            Log.d("FirebaseRepository", "blockedUsers added successfully")

            true
        } catch (e: Exception) {
            Log.d("FirebaseRepository", "Error adding blockedUsers: ${e.message}")
            // Handle any potential errors here
            false
        }
    }

    // get blockedUsers from Firestore

    suspend fun getBlockedUsersFromFirebase(userId: String): List<String> {
        if (userId.isBlank()) {
            return emptyList()
        }
        val yourUserId = firebaseAuth.currentUser?.displayName ?: ""
        val blockedUsersList = mutableListOf<String>()

        try {
            val blockedUserRef = firebaseFirestore
                .collection("users")
                .document(yourUserId)
                .collection("blockedUsers")

            val collection = blockedUserRef.get().await()

            for (document in collection.documents) {
                for (field in document.data?.keys.orEmpty()) {
                    Log.d("FirebaseRepository", "field = $field")
                    if (field.startsWith("blockedUser-")) {
//                        val blockedUserValue = document.getString(field)?.removePrefix("blockedUser-")
                        val blockedUserValue = field.removePrefix("blockedUser-")
                        Log.d("FirebaseRepository", "blockedUserValue = $blockedUserValue")
                        blockedUserValue.let {
                            blockedUsersList.add(it)
                        }
                    }
                }
            }
            Log.d("FirebaseRepository", "Finished blockedUsersList - $blockedUsersList")
            return blockedUsersList
        } catch (e: Exception) {
            Log.d(
                "FirebaseRepository",
                "Something went wrong fetching blockedUsersList: ${e.message}"
            )
        }

        return emptyList()
    }

    // get usersWhoBlockedYou from Firestore

    suspend fun getUsersWhoBlockedYouFromFirebase(userId: String): List<String> {
        if (userId.isBlank()) {
            return emptyList()
        }
        val yourUserId = firebaseAuth.currentUser?.displayName ?: ""
        val usersWhoBlockedYouList = mutableListOf<String>()

        try {
            val usersWhoBlockedYouRef = firebaseFirestore
                .collection("users")
                .document(yourUserId)
                .collection("usersWhoBlockedYou")

            val collection = usersWhoBlockedYouRef.get().await()

            for (document in collection.documents) {
                for (field in document.data?.keys.orEmpty()) {
                    Log.d("FirebaseRepository", "field = $field")
                    if (field.startsWith("userWhoBlockedYou-")) {
//                        val blockedUserValue = document.getString(field)?.removePrefix("blockedUser-")
                        val userWhoBlockedYouValue = field.removePrefix("userWhoBlockedYou-")
                        Log.d(
                            "FirebaseRepository",
                            "userWhoBlockedYouValue = $userWhoBlockedYouValue"
                        )
                        userWhoBlockedYouValue.let {
                            usersWhoBlockedYouList.add(it)
                        }
                    }
                }
            }
            Log.d("FirebaseRepository", "Finished usersWhoBlockedYouList - $usersWhoBlockedYouList")
            return usersWhoBlockedYouList
        } catch (e: Exception) {
            Log.d(
                "FirebaseRepository",
                "Something went wrong fetching usersWhoBlockedYouList: ${e.message}"
            )
        }

        return emptyList()
    }

    // delete userWhoBlockedYou from firebase

    private suspend fun deleteUserWhoBlockedYouFromFirebase(userId: String?): Boolean {
        val yourUserId = firebaseAuth.currentUser?.displayName ?: ""
        if (userId == null) {
            Log.d("FirebaseRepository", "Invalid userId: null")
            return false // Error
        }

        return try {
            val usersWhoBlockedYouRef = firebaseFirestore
                .collection("users")
                .document(yourUserId)
                .collection("usersWhoBlockedYou")
                .document(yourUserId)

            val updates = hashMapOf<String, Any>(
                "userWhoBlockedYou-$userId" to FieldValue.delete()
            )

            usersWhoBlockedYouRef.update(updates).addOnSuccessListener {
                Log.d(
                    "FirebaseRepository",
                    "Field userWhoBlockedYou-$userId deleted successfully"
                )
            }.addOnFailureListener {
                Log.d(
                    "FirebaseRepository",
                    "Error deleting field userWhoBlockedYou-$userId: ${it.message}"
                )
            }.await() // Await the completion of the update operation

            true
        } catch (e: Exception) {
            Log.d(
                "FirebaseRepository",
                "Error deleting field userWhoBlockedYou-$userId: ${e.message}"
            )
            false
        }
    }

    // delete blocked user from firebase

    suspend fun deleteBlockedUserFromFirebase(userId: String?): Boolean {
        if (userId == null) {
            Log.d("FirebaseRepository", "Invalid userId: null")
            return false // Error
        }

        return try {
            val yourUserId = firebaseAuth.currentUser?.displayName ?: ""
            val blockedUserRef = firebaseFirestore
                .collection("users")
                .document(yourUserId)
                .collection("blockedUsers")
                .document(yourUserId)

            val updates = hashMapOf<String, Any>(
                "blockedUser-$userId" to FieldValue.delete()
            )

            blockedUserRef.update(updates).addOnSuccessListener {
                Log.d("FirebaseRepository", "Field 'blockedUser-$userId' deleted successfully")
            }.addOnFailureListener {
                Log.d(
                    "FirebaseRepository",
                    "Error deleting field 'blockedUser-$userId': ${it.message}"
                )
            }.await() // Await the completion of the update operation

            deleteUserWhoBlockedYouFromFirebase(userId)

            true
        } catch (e: Exception) {
            Log.d("FirebaseRepository", "Error deleting field 'blockedUser-$userId': ${e.message}")
            false
        }
    }


    suspend fun writeUserDataToFirebase(
        userId: String, userData: MutableMap<String, Any>,
        mediaItems: MutableMap<String, Uri>,
        context: Context
    ): Boolean {
        return try {
            // Remove media-related data from the userData map
//            val userDataWithoutMedia = userData.filterKeys { !it.startsWith("media") }
            // Upload media items to Firebase Storage and store their download URLs
            val mediaUrls = uploadMediaItemsToStorage(userId, mediaItems, userData, context)
            // Add the download URLs of media items to the userData map
            val userDataWithMediaUrls = userData.toMutableMap()
            mediaUrls.forEach { (mediaIdentifier, url) ->
                var value = url.toString()
                if (value == "null") {
                    value = ""
                }
                userDataWithMediaUrls[mediaIdentifier] = value
            }
            Log.d("FirebaseRepository", "userId = $userId")
            firebaseFirestore.collection("users").document(userId)
                .set(userDataWithMediaUrls, SetOptions.merge()).await()
            Log.d("FirebaseRepository", "User content successfully added")
            true // Success
        } catch (e: Exception) {
            Log.d("FirebaseRepository", "User content was not added ${e.message}")
            false // Error
        }
    }

    fun getTypeFromUri(context: Context, uri: Uri): String? {
        val contentResolver = context.contentResolver
        return contentResolver.getType(uri)
    }

    suspend fun uploadMediaItemsToStorage(
        userId: String,
        mediaItems: MutableMap<String, Uri> = mutableMapOf(),
        userData: MutableMap<String, Any> = mutableMapOf(),
        context: Context
    ): Map<String, Uri?> {
        val mediaUrls = mutableMapOf<String, Uri?>()
        val storageRef = firebaseStorage.reference.child("users/$userId/gallery")
        mediaItems.forEach { (mediaIdentifier, uri) ->
            uri.let { validUri ->
                if (validUri.toString() == "") {
                    mediaUrls[mediaIdentifier] = null
                    when (mediaIdentifier) {
                        "mediaOne" -> userData["typeOfMediaOne"] = ""
                        "mediaTwo" -> userData["typeOfMediaTwo"] = ""
                        "mediaThree" -> userData["typeOfMediaThree"] = ""
                        "mediaFour" -> userData["typeOfMediaFour"] = ""
                        "mediaFive" -> userData["typeOfMediaFive"] = ""
                        "mediaSix" -> userData["typeOfMediaSix"] = ""
                        "mediaSeven" -> userData["typeOfMediaSeven"] = ""
                        "mediaEight" -> userData["typeOfMediaEight"] = ""
                        "mediaNine" -> userData["typeOfMediaNine"] = ""
                    }
                } else {
                    val fileName =
                        "$mediaIdentifier${UUID.randomUUID()}_${System.currentTimeMillis()}"

                    deleteMediaFromFirebase(userId, mediaIdentifier)

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
                        val type = addedFile.metadata?.contentType
                        Log.d("FirebaseRepository", "The file's added type is $type")
                        when (mediaIdentifier) {
                            "mediaOne" -> userData["typeOfMediaOne"] =
                                if (type?.startsWith("image") == true) {
                                    "IMAGE"
                                } else if (type?.startsWith("video") == true) {
                                    "VIDEO"
                                } else {
                                    ""
                                }

                            "mediaTwo" -> userData["typeOfMediaTwo"] =
                                if (type?.startsWith("image") == true) {
                                    "IMAGE"
                                } else if (type?.startsWith("video") == true) {
                                    "VIDEO"
                                } else {
                                    ""
                                }

                            "mediaThree" -> userData["typeOfMediaThree"] =
                                if (type?.startsWith("image") == true) {
                                    "IMAGE"
                                } else if (type?.startsWith("video") == true) {
                                    "VIDEO"
                                } else {
                                    ""
                                }

                            "mediaFour" -> userData["typeOfMediaFour"] =
                                if (type?.startsWith("image") == true) {
                                    "IMAGE"
                                } else if (type?.startsWith("video") == true) {
                                    "VIDEO"
                                } else {
                                    ""
                                }

                            "mediaFive" -> userData["typeOfMediaFive"] =
                                if (type?.startsWith("image") == true) {
                                    "IMAGE"
                                } else if (type?.startsWith("video") == true) {
                                    "VIDEO"
                                } else {
                                    ""
                                }

                            "mediaSix" -> userData["typeOfMediaSix"] =
                                if (type?.startsWith("image") == true) {
                                    "IMAGE"
                                } else if (type?.startsWith("video") == true) {
                                    "VIDEO"
                                } else {
                                    ""
                                }

                            "mediaSeven" -> userData["typeOfMediaSeven"] =
                                if (type?.startsWith("image") == true) {
                                    "IMAGE"
                                } else if (type?.startsWith("video") == true) {
                                    "VIDEO"
                                } else {
                                    ""
                                }

                            "mediaEight" -> userData["typeOfMediaEight"] =
                                if (type?.startsWith("image") == true) {
                                    "IMAGE"
                                } else if (type?.startsWith("video") == true) {
                                    "VIDEO"
                                } else {
                                    ""
                                }

                            "mediaNine" -> userData["typeOfMediaNine"] =
                                if (type?.startsWith("image") == true) {
                                    "IMAGE"
                                } else if (type?.startsWith("video") == true) {
                                    "VIDEO"
                                } else {
                                    ""
                                }
                        }
                        Log.d("FirebaseRepository", "File successfully added to storage")
                        val downloadUrl = mediaRef.downloadUrl.await()
                        mediaUrls[mediaIdentifier] = downloadUrl

                    } catch (e: Exception) {
                        Log.d(
                            "FirebaseRepository",
                            "There was an issue with uploading the file to Firebase. HTTP result and code: ${e.message}"
                        )
                    }
                }
            }
        }

        return mediaUrls
    }

    suspend fun uploadImageToStorage(imageUri: Uri, mediaIdentifier: String): Boolean {
        val userId = firebaseAuth.currentUser?.displayName
        return try {
            if (userId != null) {
                deleteMediaFromFirebase(userId, mediaIdentifier)
            }
            val storageRef = firebaseStorage.reference
            val userRef = storageRef.child("users/$userId")
            val imageRef = userRef.child("gallery")
            val imageName = "$mediaIdentifier${UUID.randomUUID()}.jpg"
            val imageFileRef = imageRef.child(imageName)
            val uploadTask = imageFileRef.putFile(imageUri)
            Log.d("FirebaseRepository", "Image successfully added to cloud storage")
            true // Success
        } catch (e: Exception) {
            Log.d("FirebaseRepository", "Image failed to be added to cloud storage")
            false // Error
        }
    }

    suspend fun deleteMediaFromFirebase(userId: String, mediaIdentifier: String) {
        val storageRef = firebaseStorage.reference
        val userRef = storageRef.child("users/$userId")
        val mediaRef = userRef.child("gallery")
        try {
            // Delete the downloaded url from Firestore
            firebaseFirestore.collection("users").document(userId)
                .update(
                    mapOf(
                        mediaIdentifier to ""
                    ),
                ).await()
            val mediaList = mediaRef.listAll().await()
            for (item in mediaList.items) {
                when {
                    item.name.startsWith(mediaIdentifier) -> {
                        // Delete the existing media with the specified identifier from Storage
                        item.delete().await()
                        Log.d(
                            "FirebaseRepository",
                            "Deleted previous image that was in the $mediaIdentifier spot from" +
                                    "firestore and from storage"
                        )
                        // Once deleted, break out of the loop since we've removed the existing media
                        break
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(
                "FirebaseRepository",
                "Error deleting media with identifier $mediaIdentifier: ${e.message}"
            )
        }
    }

    fun getImageFromStorage(imageUri: Uri, mediaIdentifier: String): Boolean {
        val userId = firebaseAuth.currentUser?.displayName ?: ""
        return try {
            val storageRef = firebaseStorage.reference
            val userRef = storageRef.child("users/$userId")
            val imageRef = userRef.child("gallery")
            val imageName = "$mediaIdentifier${UUID.randomUUID()}.jpg"
            val imageFileRef = imageRef.child(imageName)
            val uploadTask = imageFileRef.getFile(imageUri)
            Log.d("FirebaseRepository", "Image successfully added to cloud storage")
            true // Success
        } catch (e: Exception) {
            Log.d("FirebaseRepository", "Image failed to be added to cloud storage")
            false // Error
        }
    }

    fun uploadVideoToStorage(videoUri: Uri, mediaIdentifier: String): Boolean {
        val userId = firebaseAuth.currentUser?.displayName
        return try {
            val storageRef = firebaseStorage.reference
            val userRef = storageRef.child("users/$userId")
            val videoRef = userRef.child("gallery")
            val videoName =
                "$mediaIdentifier${UUID.randomUUID()}.mp4" // Use UUID to generate a unique filename
            val videoFileRef = videoRef.child(videoName)
            Log.d("FirebaseRepository", "The path is - $videoFileRef.path")
            val uploadTask = videoFileRef.putFile(videoUri)
            Log.d("FirebaseRepository", "Image successfully added to cloud storage")
            true // Success
        } catch (e: Exception) {
            Log.d("FirebaseRepository", "Image failed to be added to cloud storage")
            false // Error
        }
    }

    fun uploadProfileVideoToStorage(videoUri: Uri): Boolean {
        val userId = firebaseAuth.currentUser?.displayName
        return try {
            val storageRef = firebaseStorage.reference
            val userRef = storageRef.child("users/$userId")
            val profileVideoRef = userRef.child("profile")
            val profileVideoName =
                "video_${UUID.randomUUID()}.mp4" // Use UUID to generate a unique filename
            val profileVideoFileRef = profileVideoRef.child(profileVideoName)
            Log.d("FirebaseRepository", "The path is - $profileVideoFileRef.path")
            val uploadTask = profileVideoFileRef.putFile(videoUri)
            Log.d("FirebaseRepository", "Image successfully added to cloud storage")
            true // Success
        } catch (e: Exception) {
            Log.d("FirebaseRepository", "Image failed to be added to cloud storage")
            false // Error
        }
    }

// Other Firebase-related methods...

}

