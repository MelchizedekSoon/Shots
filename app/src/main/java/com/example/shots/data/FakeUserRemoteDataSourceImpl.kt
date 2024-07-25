package com.example.shots.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

class FakeUserRemoteDataSourceImpl @Inject constructor(
    val firebaseAuth: FirebaseAuth,
    val firebaseFirestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
) : UserRemoteDataSource {

    override fun getYourUserId(): String {
        return "John"
    }

    override suspend fun getUsers(): List<User> {
        return try {
            val docRef = firebaseFirestore.collection("users").get().await()
            val users = mutableListOf<User>()
            for (doc in docRef) {
                val user = doc.toObject(User::class.java)
                Log.d("FakeUserRemoteDataSourceImpl", "User retrieved - $user")
                users.add(user)
            }
            users
        } catch (e: Exception) {
            Log.d(
                "FakeUserRemoteDataSourceImpl",
                "The get all users retrieval is not working right now - ${e.message}"
            )
            // Handle any potential errors here
            emptyList() // Return an empty list or any appropriate default value
        }
    }

    override suspend fun getUserData(userId: String): User? {
        try {
            val docRef = firebaseFirestore.collection("users").document(userId)
            try {
                val document = docRef.get().await()
                Log.d("FakeUserRemoteDataSourceImpl", "Here is the value for document - $document")
                if (document.exists()) {
                    val userData = document.data
                    Log.d(
                        "FakeUserRemoteDataSourceImpl",
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
                                "FakeUserRemoteDataSourceImpl",
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
                            Log.e("FakeUserRemoteDataSourceImpl", "Invalid userName value: $userNameValue")
                        }

                        val latitudeValue = userData["latitude"] as? Number
                        var latitude = 0.0
                        try {
                            latitudeValue?.let {
                                latitude = it.toDouble()
                            }
                        } catch (e: NumberFormatException) {
                            Log.e("FakeUserRemoteDataSourceImpl", "Invalid latitude value: $latitudeValue")
                            // Handle the error appropriately
                        }

                        val longitudeValue = userData["longitude"] as? Number
                        var longitude = 0.0
                        try {
                            longitudeValue?.let {
                                longitude = it.toDouble()
                            }
                        } catch (e: NumberFormatException) {
                            Log.e("FakeUserRemoteDataSourceImpl", "Invalid longitude value: $longitudeValue")
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
                                "FakeUserRemoteDataSourceImpl", "Invalid birthday value: ${birthdayValue}" +
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
                                "FakeUserRemoteDataSourceImpl",
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
                                "FakeUserRemoteDataSourceImpl",
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
                                "FakeUserRemoteDataSourceImpl",
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
                                "FakeUserRemoteDataSourceImpl",
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
                                "FakeUserRemoteDataSourceImpl",
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
                                "FakeUserRemoteDataSourceImpl",
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
                                "FakeUserRemoteDataSourceImpl",
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
                                "FakeUserRemoteDataSourceImpl",
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
                                "FakeUserRemoteDataSourceImpl",
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
                                "FakeUserRemoteDataSourceImpl",
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
                            Log.e("FakeUserRemoteDataSourceImpl", "Invalid mediaOne value: ${mediaOneValue}")
                        }
                        Log.d("FakeUserRemoteDataSourceImpl", "mediaOne upon return - $mediaOne")
                        val mediaTwoValue = userData["mediaTwo"] as? String
                        var mediaTwo: String? = ""
                        try {
                            if (mediaTwoValue != null) {
                                mediaTwo = mediaTwoValue
                            }
                        } catch (e: Exception) {
                            Log.e("FakeUserRemoteDataSourceImpl", "Invalid mediaTwo value: ${mediaTwoValue}")
                        }

                        val mediaThreeValue = userData["mediaThree"] as? String
                        var mediaThree: String? = ""
                        try {
                            if (mediaThreeValue != null) {
                                mediaThree = mediaThreeValue
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "FakeUserRemoteDataSourceImpl",
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
                                "FakeUserRemoteDataSourceImpl",
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
                                "FakeUserRemoteDataSourceImpl",
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
                            Log.e("FakeUserRemoteDataSourceImpl", "Invalid mediaSix value: ${mediaSixValue}")
                        }

                        val mediaSevenValue = userData["mediaSeven"] as? String
                        var mediaSeven: String? = ""
                        try {
                            if (mediaSevenValue != null) {
                                mediaSeven = mediaSevenValue
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "FakeUserRemoteDataSourceImpl",
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
                                "FakeUserRemoteDataSourceImpl",
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
                                "FakeUserRemoteDataSourceImpl",
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
                                "FakeUserRemoteDataSourceImpl",
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
                            Log.e("FakeUserRemoteDataSourceImpl", "Invalid gender value: ${genderValue}")
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
                                "FakeUserRemoteDataSourceImpl",
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
                            Log.e("FakeUserRemoteDataSourceImpl", "Invalid aboutMe value: ${aboutMeValue}")
                        }

                        val linkValue = userData["link"] as? String
                        var link: String? = ""
                        try {
                            if (linkValue != null) {
                                link = linkValue
                            }
                        } catch (e: Exception) {
                            Log.e("FakeUserRemoteDataSourceImpl", "Invalid link value: ${linkValue}")
                        }


                        val heightValue = userData["height"] as? String
                        var height: String? = ""
                        try {
                            if (heightValue != null) {
                                height = heightValue
                            }
                        } catch (e: Exception) {
                            Log.e("FakeUserRemoteDataSourceImpl", "Invalid height value: ${heightValue}")
                        }


                        val workValue = userData["work"] as? String
                        var work: String? = ""
                        try {
                            if (workValue != null) {
                                work = workValue
                            }
                        } catch (e: Exception) {
                            Log.e("FakeUserRemoteDataSourceImpl", "Invalid work value: ${workValue}")
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
                                        "FakeUserRemoteDataSourceImpl",
                                        "Invalid education value: $educationValue"
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "FakeUserRemoteDataSourceImpl",
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
//                            e.message?.let { Log.e("FakeUserRemoteDataSourceImpl"", it) }
//                            Log.e("FakeUserRemoteDataSourceImpl"", "Invalid education value: $educationValue")
//                            null
//                        }
//                    } else {
//                        // Handle the case where lookingForValue is null
//                        // For example, you might want to set a default value or log a message
//                        Log.d("FakeUserRemoteDataSourceImpl"", "education value is null")
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
                            Log.e("FakeUserRemoteDataSourceImpl", "Invalid kids value: ${kidsValue}")
                        }


                        val religionValue = userData["religion"] as? String
                        var religion: Religion? = Religion.UNKNOWN
                        try {
                            if (religionValue != null) {
                                religion = when (religionValue) {
                                    "CHRISTIANITY" -> Religion.CHRISTIANITY
                                    "ISLAM" -> Religion.ISLAM
                                    "HINDUISM" -> Religion.HINDUISM
                                    "BUDDHISM" -> Religion.BUDDHISM
                                    "SIKHISM" -> Religion.SIKHISM
                                    "JUDAISM" -> Religion.JUDAISM
                                    "BAHAI_FAITH" -> Religion.BAHAI_FAITH
                                    "CONFUCIANISM" -> Religion.CONFUCIANISM
                                    "JAINISM" -> Religion.JAINISM
                                    "SHINTOISM" -> Religion.SHINTOISM
                                    else -> Religion.UNKNOWN
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("FakeUserRemoteDataSourceImpl", "Invalid religion value: ${religionValue}")
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
                            Log.e("FakeUserRemoteDataSourceImpl", "Invalid pets value: ${petsValue}")
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
                            Log.e("FakeUserRemoteDataSourceImpl", "Invalid exercise value: ${exerciseValue}")
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
                            Log.e("FakeUserRemoteDataSourceImpl", "Invalid smoking value: ${smokingValue}")
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
                            Log.e("FakeUserRemoteDataSourceImpl", "Invalid drinking value: ${drinkingValue}")
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
                                "FakeUserRemoteDataSourceImpl",
                                "Invalid marijuana value: ${marijuanaValue}"
                            )
                        }


                        val promptOneAnswerValue = userData["promptOneAnswer"] as? String
                        var promptOneAnswer = promptOneAnswerValue
                        if (promptOneAnswer == null) {
                            promptOneAnswer = ""
//                            Log.e(
//                                "FakeUserRemoteDataSourceImpl"",
//                                "Invalid promptOneAnswer value: ${promptOneAnswerValue}"
//                            )
                        }

                        val promptTwoAnswerValue = userData["promptTwoAnswer"] as? String
                        var promptTwoAnswer = promptTwoAnswerValue
                        if (promptTwoAnswer == null) {
                            promptTwoAnswer = ""
//                            Log.e(
//                                "FakeUserRemoteDataSourceImpl"",
//                                "Invalid promptTwoAnswer value: ${promptTwoAnswerValue}"
//                            )
                        }

                        val promptThreeAnswerValue = userData["promptThreeAnswer"] as? String
                        var promptThreeAnswer = promptThreeAnswerValue
                        if (promptThreeAnswer == null) {
                            promptThreeAnswer = ""
//                            Log.e(
//                                "FakeUserRemoteDataSourceImpl"",
//                                "Invalid promptThreeAnswer value: ${promptThreeAnswerValue}"
//                            )
                        }

                        val promptOneQuestionValue = userData["promptOneQuestion"] as? String
                        var promptOneQuestion = promptOneQuestionValue
                        if (promptOneQuestion == null) {
                            promptOneQuestion = ""
//                            Log.e(
//                                "FakeUserRemoteDataSourceImpl"",
//                                "Invalid promptOneQuestion value: ${promptOneQuestionValue}"
//                            )
                        }

                        val promptTwoQuestionValue = userData["promptTwoQuestion"] as? String
                        var promptTwoQuestion = promptTwoQuestionValue
                        if (promptTwoQuestion == null) {
                            promptTwoQuestion = ""
//                            Log.e(
//                                "FakeUserRemoteDataSourceImpl"",
//                                "Invalid promptTwoQuestion value: ${promptTwoQuestionValue}"
//                            )
                        }

                        val promptThreeQuestionValue = userData["promptThreeQuestion"] as? String
                        var promptThreeQuestion = promptThreeQuestionValue
                        if (promptThreeQuestion == null) {
                            promptThreeQuestion = ""
//                            Log.e(
//                                "FakeUserRemoteDataSourceImpl"",
//                                "Invalid promptThreeQuestion value: $promptThreeQuestionValue"
//                            )
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
                            Log.e("FakeUserRemoteDataSourceImpl", "Invalid showMe value: ${showMeValue}")
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
                            Log.e(
                                "FakeUserRemoteDataSourceImpl",
                                "Invalid showUsers value: ${showUsersValue}"
                            )
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
                            Log.e(
                                "FakeUserRemoteDataSourceImpl",
                                "Invalid acceptShots value: ${acceptShotsValue}"
                            )
                        }

                        val ageMinToShowValue = userData["ageMinToShow"] as? Number
                        var ageMinToShow = 18
                        try {
                            ageMinToShowValue?.let {
                                ageMinToShow = it.toInt()
                            }
                        } catch (e: NumberFormatException) {
                            Log.e(
                                "FakeUserRemoteDataSourceImpl",
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
                                "FakeUserRemoteDataSourceImpl",
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
                                "FakeUserRemoteDataSourceImpl",
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
//                            "FakeUserRemoteDataSourceImpl"",
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
                                "FakeUserRemoteDataSourceImpl",
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
                                "FakeUserRemoteDataSourceImpl",
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
                                "FakeUserRemoteDataSourceImpl",
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
                                "FakeUserRemoteDataSourceImpl",
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
                                "FakeUserRemoteDataSourceImpl",
                                "Invalid shotsCount value: $shotsCountValue"
                            )
                            // Handle the error appropriately
                        }

                        Log.d("FakeUserRemoteDataSourceImpl", "religion upon return - $religion")

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
                        Log.d("FakeUserRemoteDataSourceImpl", "Missing required fields in user data")
                    }
                } else {
                    Log.d("FakeUserRemoteDataSourceImpl", "User document does not exist")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("FakeUserRemoteDataSourceImpl", "Error fetching user data: $e - ${e.message}")
            }
        } catch (e: Exception) {
            Log.d("FakeUserRemoteDataSourceImpl", "Error fetching user data: ${e.message}")
        }
        return null
    }

    override suspend fun deleteMediaFromFirebase(userId: String, mediaIdentifier: String) {
        val storageRef = firebaseStorage.reference
        val userRef = storageRef.child("users/$userId")
        val mediaRef = userRef.child("gallery")
        try {
            Log.d("FakeUserRemoteDataSourceImpl", "userId = $userId")
            Log.d("FakeUserRemoteDataSourceImpl", "Deleting media with identifier $mediaIdentifier")
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
                            "FakeUserRemoteDataSourceImpl",
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
                "FakeUserRemoteDataSourceImpl",
                "Error deleting media with identifier $mediaIdentifier: ${e.message}"
            )
        }
    }


    private suspend fun uploadMediaItemsToStorage(
        userId: String,
        mediaItems: MutableMap<String, Uri> = mutableMapOf(),
        userData: MutableMap<String, Any> = mutableMapOf(),
        context: Context
    ): Map<String, Uri?> {
        Log.d("FakeUserRemoteDataSourceImpl", "userId inside of uploadMediaItemsToStorage = $userId")
        val mediaUrls = mutableMapOf<String, Uri?>()
        val storageRef = firebaseStorage.reference.child("users/${userId}/gallery")
        mediaItems.forEach { (mediaIdentifier, uri) ->
            uri.let { validUri ->
                if (validUri.toString() == "") {
                    mediaUrls[mediaIdentifier] = null
                    when (mediaIdentifier) {
                        "mediaOne" -> userData["typeOfMediaOne"] = TypeOfMedia.UNKNOWN
                        "mediaTwo" -> userData["typeOfMediaTwo"] = TypeOfMedia.UNKNOWN
                        "mediaThree" -> userData["typeOfMediaThree"] = TypeOfMedia.UNKNOWN
                        "mediaFour" -> userData["typeOfMediaFour"] = TypeOfMedia.UNKNOWN
                        "mediaFive" -> userData["typeOfMediaFive"] = TypeOfMedia.UNKNOWN
                        "mediaSix" -> userData["typeOfMediaSix"] = TypeOfMedia.UNKNOWN
                        "mediaSeven" -> userData["typeOfMediaSeven"] = TypeOfMedia.UNKNOWN
                        "mediaEight" -> userData["typeOfMediaEight"] = TypeOfMedia.UNKNOWN
                        "mediaNine" -> userData["typeOfMediaNine"] = TypeOfMedia.UNKNOWN
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
                        Log.d("FakeUserRemoteDataSourceImpl", "The file's added type is $type")
                        when (mediaIdentifier) {
                            "mediaOne" -> userData["typeOfMediaOne"] =
                                if (type?.startsWith("image") == true) {
                                    "IMAGE"
                                } else if (type?.startsWith("video") == true) {
                                    "VIDEO"
                                } else {
                                    "UNKNOWN"
                                }

                            "mediaTwo" -> userData["typeOfMediaTwo"] =
                                if (type?.startsWith("image") == true) {
                                    "IMAGE"
                                } else if (type?.startsWith("video") == true) {
                                    "VIDEO"
                                } else {
                                    "UNKNOWN"
                                }

                            "mediaThree" -> userData["typeOfMediaThree"] =
                                if (type?.startsWith("image") == true) {
                                    "IMAGE"
                                } else if (type?.startsWith("video") == true) {
                                    "VIDEO"
                                } else {
                                    "UNKNOWN"
                                }

                            "mediaFour" -> userData["typeOfMediaFour"] =
                                if (type?.startsWith("image") == true) {
                                    "IMAGE"
                                } else if (type?.startsWith("video") == true) {
                                    "VIDEO"
                                } else {
                                    "UNKNOWN"
                                }

                            "mediaFive" -> userData["typeOfMediaFive"] =
                                if (type?.startsWith("image") == true) {
                                    "IMAGE"
                                } else if (type?.startsWith("video") == true) {
                                    "VIDEO"
                                } else {
                                    "UNKNOWN"
                                }

                            "mediaSix" -> userData["typeOfMediaSix"] =
                                if (type?.startsWith("image") == true) {
                                    "IMAGE"
                                } else if (type?.startsWith("video") == true) {
                                    "VIDEO"
                                } else {
                                    "UNKNOWN"
                                }

                            "mediaSeven" -> userData["typeOfMediaSeven"] =
                                if (type?.startsWith("image") == true) {
                                    "IMAGE"
                                } else if (type?.startsWith("video") == true) {
                                    "VIDEO"
                                } else {
                                    "UNKNOWN"
                                }

                            "mediaEight" -> userData["typeOfMediaEight"] =
                                if (type?.startsWith("image") == true) {
                                    "IMAGE"
                                } else if (type?.startsWith("video") == true) {
                                    "VIDEO"
                                } else {
                                    "UNKNOWN"
                                }

                            "mediaNine" -> userData["typeOfMediaNine"] =
                                if (type?.startsWith("image") == true) {
                                    "IMAGE"
                                } else if (type?.startsWith("video") == true) {
                                    "VIDEO"
                                } else {
                                    "UNKNOWN"
                                }
                        }
                        Log.d("FakeUserRemoteDataSourceImpl", "File successfully added to storage")
                        val downloadUrl = mediaRef.downloadUrl.await()
                        mediaUrls[mediaIdentifier] = downloadUrl

                    } catch (e: Exception) {
                        Log.d(
                            "FakeUserRemoteDataSourceImpl",
                            "There was an issue with uploading the file to Firebase. HTTP result and code: ${e.message}"
                        )
                    }
                }
            }
        }

        return mediaUrls
    }


    override suspend fun writeUserDataToFirebase(
        userId: String, userData: MutableMap<String, Any>,
        mediaItems: MutableMap<String, Uri>,
        context: Context
    ): Boolean {
        return try {
            Log.d("FakeUserRemoteDataSourceImpl", "userId inside of writeUserDataToFirebase = $userId")
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
            Log.d("FakeUserRemoteDataSourceImpl", "userId past uploadMediaItemsToStorage, inside " +
                    "writeUserDataToFirebase = $userId and the values are = " +
                    "${userDataWithMediaUrls}")
            firebaseFirestore.collection("users").document(userId).
                set(userDataWithMediaUrls, SetOptions.merge()).await()
            Log.d("FakeUserRemoteDataSourceImpl", "User content successfully added")
            true // Success
        } catch (e: Exception) {
            Log.d("FakeUserRemoteDataSourceImpl", "User content was not added ${e.message}")
            false // Error
        }
    }


}