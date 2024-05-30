package com.example.shots.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.TypeConverter

@Entity(
//    foreignKeys = [ForeignKey(
//        entity = User::class,
//        parentColumns = ["id"],
//        childColumns = ["childId"],
//        onDelete = ForeignKey.CASCADE
//    )]
)

class Converters {
    @TypeConverter
    fun fromString(value: String?): MutableList<String>? {
        return value?.split(",")?.mapTo(mutableListOf()) { it.trim() }
    }

    @TypeConverter
    fun toString(value: MutableList<String>?): String? {
        return value?.joinToString(",")
    }
}