package com.example.shots.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters


@Entity(
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["userWhoBlockedYouId"],
        onDelete = ForeignKey.CASCADE
    )]
)

@TypeConverters(Converters::class)
data class UserWhoBlockedYou(
    @PrimaryKey val userWhoBlockedYouId: String,
    val usersWhoBlockedYou: MutableList<String>
) {
    constructor() : this("", mutableListOf())
}