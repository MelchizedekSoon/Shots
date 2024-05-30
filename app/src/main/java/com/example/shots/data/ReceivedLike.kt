package com.example.shots.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["receivedLikeId"],
        onDelete = ForeignKey.CASCADE
    )]
)
@TypeConverters(Converters::class)
data class ReceivedLike(
    @PrimaryKey val receivedLikeId: String,
    val receivedLikes: MutableList<String>
) {
    constructor() : this("", mutableListOf())
}
