package com.oliveroneill.wilt.data.dao

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.oliveroneill.wilt.testing.OpenForTesting
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * The data to be displayed per row in the view.
 */
@OpenForTesting
@Entity
data class ArtistRank(
    /**
     * Week will be a string of the format {week_index}-{year}. Since the data is split up into weeks, this works
     * well as the identifier
     */
    @PrimaryKey val week: String,
    /**
     * The play date in ISO format. This will just be a random day within the [week] we're looking at where this
     * artist is played. This isn't that useful but it's nice to have a date format that we can use, to at least
     * get the month and year out of
     */
    val date: LocalDate,
    /**
     * The most played artist of the [week]
     */
    val topArtist: String,
    /**
     * The number of plays within the given [week]
     */
    val count: Int
)

/**
 * Used for converting the Dates into Strings to work with Room
 */
object PlayHistoryTypeConverters {
    /**
     * The format will be YYYY-MM-DD
     */
    private val formatter = DateTimeFormatter.ISO_DATE
    @TypeConverter
    @JvmStatic
    fun toDate(value: String?): LocalDate? {
        return value?.
            let {
                LocalDate.parse(value, formatter)
            }
    }

    @TypeConverter
    @JvmStatic
    fun fromDate(date: LocalDate?): String? {
        return date?.format(formatter)
    }
}
