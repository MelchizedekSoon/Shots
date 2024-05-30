package com.example.shots.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters


@Entity(
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["blockedUserId"],
        onDelete = ForeignKey.CASCADE
    )]
)

//@TypeConverters(Converters::class)
data class BlockedUser(
    @PrimaryKey var blockedUserId: String,
    val blockedUsers: MutableList<String>
) {
    constructor() : this("", mutableListOf())
}