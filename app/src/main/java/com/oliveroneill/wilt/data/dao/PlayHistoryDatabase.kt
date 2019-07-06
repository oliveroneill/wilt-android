package com.oliveroneill.wilt.data.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ArtistRank::class], version = 1)
abstract class PlayHistoryDatabase : RoomDatabase() {
    abstract fun historyDao(): PlayHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: PlayHistoryDatabase? = null

        fun getDatabase(context: Context): PlayHistoryDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) return tempInstance
            synchronized(this) {
                return Room.databaseBuilder(
                    context.applicationContext,
                    PlayHistoryDatabase::class.java,
                    "Play History Database"
                ).build().also {
                    INSTANCE = it
                }
            }
        }
    }
}
