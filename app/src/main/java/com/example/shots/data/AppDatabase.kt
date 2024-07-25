package com.example.shots.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [User::class, Bookmark::class, SentLike::class, ReceivedLike::class,
               SentShot::class, ReceivedShot::class, BlockedUser::class, UserWhoBlockedYou::class,
               IfSeenReceivedShot::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun sentLikeDao(): SentLikeDao
    abstract fun receivedLikeDao(): ReceivedLikeDao
    abstract fun sentShotDao(): SentShotDao
    abstract fun receivedShotDao(): ReceivedShotDao
    abstract fun blockedUserDao(): BlockedUserDao
    abstract fun userWhoBlockedYouDao(): UserWhoBlockedYouDao
    abstract fun ifSeenReceivedShotDao(): IfSeenReceivedShotDao
}
