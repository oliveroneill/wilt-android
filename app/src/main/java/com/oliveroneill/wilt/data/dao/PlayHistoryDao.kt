package com.oliveroneill.wilt.data.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PlayHistoryDao {
    // TODO: handle different users in case someone logs in and out
    @Query("SELECT * FROM artistrank WHERE date < :start ORDER BY date DESC")
    fun loadPlayHistory(start: String): DataSource.Factory<Int, ArtistRank>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(items: List<ArtistRank>)
}
