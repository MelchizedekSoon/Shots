package com.example.shots.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters


@Entity(
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["bookmarkId"],
        onDelete = ForeignKey.CASCADE
    )]
)

@TypeConverters(Converters::class)
data class Bookmark(
    @PrimaryKey val bookmarkId: String,
    val bookmarks: MutableList<String>
) {
    constructor() : this("", mutableListOf())
}