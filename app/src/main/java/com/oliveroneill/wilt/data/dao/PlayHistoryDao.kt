package com.oliveroneill.wilt.data.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PlayHistoryDao {
    /**
     * Get the play history going back in time
     */
    @Query("SELECT * FROM artistrank ORDER BY date(date) DESC")
    fun loadPlayHistory(): DataSource.Factory<Int, ArtistRank>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(items: List<ArtistRank>)

    @Query("DELETE FROM artistrank")
    fun deleteAll()
}
