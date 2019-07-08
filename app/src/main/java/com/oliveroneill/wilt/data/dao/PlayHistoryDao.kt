package com.oliveroneill.wilt.data.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PlayHistoryDao {
    /**
     * Get the play history starting at [endDate] and going back in time
     */
    @Query("SELECT * FROM artistrank WHERE date < :endDate ORDER BY date DESC")
    fun loadPlayHistory(endDate: String): DataSource.Factory<Int, ArtistRank>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(items: List<ArtistRank>)

    @Query("DELETE FROM artistrank")
    fun deleteAll()
}
