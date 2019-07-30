package com.oliveroneill.wilt.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TopArtistDao {
    @Query("SELECT * FROM topartistcacheelement WHERE artistIndex = :artistIndex AND timeRange = :timeRange LIMIT 1")
    fun getCachedResult(artistIndex: Int, timeRange: String): List<TopArtistCacheElement>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: TopArtistCacheElement)

    @Query("DELETE FROM topartistcacheelement")
    fun deleteAll()
}

@Dao
interface TopTrackDao {
    @Query("SELECT * FROM toptrackcacheelement WHERE trackIndex = :trackIndex AND timeRange = :timeRange LIMIT 1")
    fun getCachedResult(trackIndex: Int, timeRange: String): List<TopTrackCacheElement>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: TopTrackCacheElement)

    @Query("DELETE FROM toptrackcacheelement")
    fun deleteAll()
}
