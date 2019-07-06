package com.oliveroneill.wilt.data.dao

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.oliveroneill.wilt.testing.OpenForTesting

/**
 * The data to be displayed per row in the view.
 *
 * This is now doubling as the Room entity too. Not sure if I should separate the firebase response and the DB
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
    val date: String,
    /**
     * The most played artist of the [week]
     */
    val top_artist: String,
    /**
     * The number of plays within the given [week]
     */
    val count: Int
)
