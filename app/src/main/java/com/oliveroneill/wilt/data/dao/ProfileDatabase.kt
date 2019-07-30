package com.oliveroneill.wilt.data.dao

import android.content.Context
import androidx.room.*
import com.oliveroneill.wilt.testing.OpenForTesting
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Database(entities = [TopArtistCacheElement::class], version = 1)
@TypeConverters(ProfileTypeConverters::class)
abstract class TopArtistDatabase: RoomDatabase() {
    abstract fun topArtistCache(): TopArtistDao

    companion object {
        @Volatile
        private var INSTANCE: TopArtistDatabase? = null

        fun getDatabase(context: Context): TopArtistDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) return tempInstance
            synchronized(this) {
                return Room.databaseBuilder(
                    context.applicationContext,
                    TopArtistDatabase::class.java,
                    "Top Artist Cache"
                ).build().also {
                    INSTANCE = it
                }
            }
        }
    }
}

@Database(entities = [TopTrackCacheElement::class], version = 1)
@TypeConverters(ProfileTypeConverters::class)
abstract class TopTrackDatabase: RoomDatabase() {
    abstract fun topTrackCache(): TopTrackDao

    companion object {
        @Volatile
        private var INSTANCE: TopTrackDatabase? = null

        fun getDatabase(context: Context): TopTrackDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) return tempInstance
            synchronized(this) {
                return Room.databaseBuilder(
                    context.applicationContext,
                    TopTrackDatabase::class.java,
                    "Top Track Cache"
                ).build().also {
                    INSTANCE = it
                }
            }
        }
    }
}


@OpenForTesting
@Entity(primaryKeys = ["artistIndex", "timeRange"])
data class TopArtistCacheElement(
    val artistIndex: Int,
    val timeRange: String,
    val name: String,
    val totalPlays: Int,
    val lastPlayed: LocalDateTime?,
    /**
     * This will be the date that this element was placed into the cache to determine the expiry
     */
    val dateStored: LocalDateTime
)

@OpenForTesting
@Entity(primaryKeys = ["trackIndex", "timeRange"])
data class TopTrackCacheElement(
    val trackIndex: Int,
    val timeRange: String,
    val name: String,
    val totalPlayDurationMs: Long,
    val lastPlayed: LocalDateTime?,
    /**
     * This will be the date that this element was placed into the cache to determine the expiry
     */
    val dateStored: LocalDateTime
)

object ProfileTypeConverters {
    private val formatter = DateTimeFormatter.ISO_DATE_TIME
    @TypeConverter
    @JvmStatic
    fun toDate(value: String?): LocalDateTime? {
        return value?.
            let {
                LocalDateTime.parse(value, formatter)
            }
    }

    @TypeConverter
    @JvmStatic
    fun fromDate(date: LocalDateTime?): String? {
        return date?.format(formatter)
    }
}
