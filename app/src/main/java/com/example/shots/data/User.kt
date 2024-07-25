package com.example.shots.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Education {
    SOME_HIGH_SCHOOL,
    HIGH_SCHOOL,
    SOME_COLLEGE,
    UNDERGRAD_DEGREE,
    SOME_GRAD_SCHOOL,
    TECH_TRADE_SCHOOL,
    GRAD_DEGREE,
    UNKNOWN
}

enum class LookingFor {
    SHORT_TERM_BUT_OPEN_MINDED,
    LONG_TERM_BUT_OPEN_MINDED,
    SHORT_TERM,
    LONG_TERM,
    FRIENDS,
    UNSURE,
    UNKNOWN
}

enum class Gender {
    MAN,
    WOMAN,
    NON_BINARY,
    UNKNOWN
}

enum class ShowMe {
    MEN,
    WOMEN,
    ANYONE,
    UNKNOWN
}

enum class Distance(val value: Int) {
    TEN(10),
    TWENTY(20),
    THIRTY(30),
    FORTY(40),
    FIFTY(50),
    SIXTY(60),
    SEVENTY(70),
    EIGHTY(80),
    NINETY(90),
    ONE_HUNDRED(100),
    ANYWHERE(15000)
}

enum class SexualOrientation {
    STRAIGHT,
    GAY,
    LESBIAN,
    BISEXUAL,
    ASEXUAL,
    DEMISEXUAL,
    PANSEXUAL,
    QUEER,
    UNKNOWN
}

enum class Kids {
    ONE_DAY,
    DONT_WANT,
    HAVE_AND_WANT_MORE,
    HAVE_AND_DONT_WANT_MORE,
    UNSURE,
    UNKNOWN
}

enum class Religion {
    CHRISTIANITY,
    ISLAM,
    HINDUISM,
    BUDDHISM,
    SIKHISM,
    JUDAISM,
    BAHAI_FAITH,
    CONFUCIANISM,
    JAINISM,
    SHINTOISM,
    UNKNOWN
}

enum class Pets {
    DOG,
    CAT,
    FISH,
    HAMSTER_OR_GUINEA_PIG,
    BIRD,
    RABBIT,
    REPTILE,
    AMPHIBIAN,
    UNKNOWN
}

enum class Exercise {
    OFTEN,
    SOMETIMES,
    RARELY,
    NEVER,
    UNKNOWN
}

enum class Smoking {
    YES,
    ON_OCCASION,
    NEVER_SMOKE,
    UNKNOWN
}

enum class Drinking {
    YES,
    ON_OCCASION,
    NEVER_DRINK,
    UNKNOWN
}

enum class Marijuana {
    YES,
    ON_OCCASION,
    NEVER, UNKNOWN
}

enum class TypeOfMedia {
    IMAGE,
    VIDEO,
    UNKNOWN,
}

@Entity()
//@TypeConverters(User.CalendarConverter::class)
data class User(
    @PrimaryKey var id: String,
    var displayName: String?,
    var userName: String?,
    var latitude: Double?,
    var longitude: Double?,
    var birthday: Long?,
    var mediaOne: String?, // Path or URL of the first media item
    var typeOfMediaOne: TypeOfMedia?,
    var mediaTwo: String?, // Path or URL of the second media item
    var typeOfMediaTwo: TypeOfMedia?,
    var mediaThree: String?, // Path or URL of the third media item
    var typeOfMediaThree: TypeOfMedia?,
    var mediaFour: String?, // Path or URL of the fourth media item
    var typeOfMediaFour: TypeOfMedia?,
    var mediaFive: String?, // Path or URL of the fifth media item
    var typeOfMediaFive: TypeOfMedia?,
    var mediaSix: String?, // Path or URL of the sixth media item
    var typeOfMediaSix: TypeOfMedia?,
    var mediaSeven: String?, // Path or URL of the seventh media item
    var typeOfMediaSeven: TypeOfMedia?,
    var mediaEight: String?, // Path or URL of the eighth media item
    var typeOfMediaEight: TypeOfMedia?,
    var mediaNine: String?, // Path or URL of the ninth media item
    var typeOfMediaNine: TypeOfMedia?,
    var mediaProfileVideo: String?,
    var typeOfMediaProfileVideo: TypeOfMedia?,
    var aboutMe: String?, // Additional field for about me text
    var link: String?,
    var height: String?,
    var lookingFor: LookingFor?,
    var gender: Gender?,
    var sexualOrientation: SexualOrientation?,
    var work: String?,
    var education: Education?,
    var kids: Kids?,
    var religion: Religion?,
    var pets: Pets?,
    var exercise: Exercise?,
    var smoking: Smoking?,
    var drinking: Drinking?,
    var marijuana: Marijuana?,
    var promptOneAnswer: String?,
    var promptTwoAnswer: String?,
    var promptThreeAnswer: String?,
    var promptOneQuestion: String?,
    var promptTwoQuestion: String?,
    var promptThreeQuestion: String?,
    var showMe: ShowMe?,
    var showUsers: Distance,
    var acceptShots: Distance,
    var ageMinToShow: Int?,
    var ageMaxToShow: Int?,
    var newLikesCount: Int?,
    var newShotsCount: Int?,
    var newMessagesCount: Int?,
    var timesBookmarkedCount: Int?,
    var likesCount: Int?,
    var shotsCount: Int?,
//    var interests: Interests
) {

    constructor() : this(
        "", "", "", 0.0, 0.0, 0, "", TypeOfMedia.UNKNOWN, "", TypeOfMedia.UNKNOWN, "", TypeOfMedia.UNKNOWN, "", TypeOfMedia.UNKNOWN, "", TypeOfMedia.UNKNOWN,
        "",
        TypeOfMedia.UNKNOWN, "", TypeOfMedia.UNKNOWN, "", TypeOfMedia.UNKNOWN, "", TypeOfMedia.UNKNOWN, "", TypeOfMedia.UNKNOWN, "", "",
        "", LookingFor.UNKNOWN, Gender.UNKNOWN, SexualOrientation.UNKNOWN, "", Education.UNKNOWN, Kids.UNKNOWN, Religion.UNKNOWN, Pets.UNKNOWN,
        Exercise.UNKNOWN, Smoking.UNKNOWN, Drinking.UNKNOWN, Marijuana.UNKNOWN, "", "", "", "", "", "",
        ShowMe.UNKNOWN, Distance.SIXTY, Distance.SIXTY, 18, 35, 0, 0, 0, 0, 0, 0
    )


    val age: Int
        get() {
//            val today = Calendar.getInstance() // Get current date
//            Log.d(TAG, "Current day is $today")
//            val dob = birthday?.let { Calendar.getInstance().apply { timeInMillis = it } }
//                ?: Calendar.getInstance() // Get the user's birthday
//            Log.d(TAG, "DOB is $birthday")
//            var age = today.get(Calendar.YEAR) - (dob.get(Calendar.YEAR))
//            Log.d(TAG, "Age is $age")
//            if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
//                age-- // If the current date is before the user's birthday, subtract 1 from age
//            }
            return birthday?.div(31_556_952_000L)?.toInt() ?: 0
        }

//    object CalendarConverter {
//        @TypeConverter
//        fun fromCalendar(calendar: Calendar?): Long? {
//            return calendar?.timeInMillis
//        }
//
//        @TypeConverter
//        fun toCalendar(millis: Long?): Calendar? {
//            val calendar = Calendar.getInstance()
//            millis?.let { calendar.timeInMillis = it }
//            return calendar
//        }
//    }

//    class UserListConverter {
//        private val gson = Gson()
//
//        @TypeConverter
//        fun fromUserList(userList: List<User>): String {
//            return gson.toJson(userList)
//        }
//
//        @TypeConverter
//        fun toUserList(userListString: String): List<User> {
//            val type = object : TypeToken<List<User>>() {}.type
//            return gson.fromJson(userListString, type)
//        }
//    }


}