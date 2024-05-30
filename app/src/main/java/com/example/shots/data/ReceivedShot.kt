package com.example.shots.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.shots.data.Converters
import com.example.shots.data.User

@Entity(
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["receivedShotId"],
        onDelete = ForeignKey.CASCADE
    )]
)
@TypeConverters(Converters::class)
data class ReceivedShot(
    @PrimaryKey val receivedShotId: String,
    val receivedShots: MutableList<String>
) {
    constructor() : this("", mutableListOf())

    companion object {
        private val usedRandomNumbers = mutableSetOf<Int>()

        fun generateId(userId: String): String {
            val randomNumber = generateUniqueRandomNumber()
            return "shots-$userId-$randomNumber"
        }

        private fun generateUniqueRandomNumber(): Int {
            var randomNumber = (0..9999).random()
            while (usedRandomNumbers.contains(randomNumber)) {
                randomNumber = (0..9999).random()
            }
            usedRandomNumbers.add(randomNumber)
            return randomNumber
        }
    }
}
