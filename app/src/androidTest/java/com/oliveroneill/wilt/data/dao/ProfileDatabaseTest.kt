package com.oliveroneill.wilt.data.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.oliveroneill.wilt.data.TimeRange
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class ProfileDatabaseTest {
    private lateinit var trackDao: TopTrackDao
    private lateinit var artistDao: TopArtistDao
    private lateinit var trackDB: TopTrackDatabase
    private lateinit var artistDB: TopArtistDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        trackDB = Room.inMemoryDatabaseBuilder(
            context, TopTrackDatabase::class.java
        ).build()
        artistDB = Room.inMemoryDatabaseBuilder(
            context, TopArtistDatabase::class.java
        ).build()
        trackDao = trackDB.topTrackCache()
        artistDao = artistDB.topArtistCache()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        trackDao.deleteAll()
        artistDao.deleteAll()
        trackDB.close()
        artistDB.close()
    }

    @Test
    @Throws(Exception::class)
    fun getCachedElementForTrack() {
        val timeRange = TimeRange.MediumTerm
        val index = 55
        val element1 = TopTrackCacheElement(
            index,
            timeRange.toString(),
            "Hope by (Sandy) Alex G",
            10_000,
            LocalDateTime.now().plusDays(2),
            "http://notarealurl.com/album_img.png",
            LocalDateTime.now().minusWeeks(11)
        )
        val element2 = TopTrackCacheElement(
            index,
            timeRange.toString(),
            "Hacker by Death Grips",
            22_000,
            LocalDateTime.now().minusDays(10),
            "http://adifferenturl.com/album_img.png",
            LocalDateTime.now().minusHours(4)
        )
        trackDao.insert(element1)
        trackDao.insert(element2)
        assertEquals(listOf(element2), trackDao.getCachedResult(index, timeRange.toString()))
    }

    @Test
    @Throws(Exception::class)
    fun getCachedElementForTrackMultipleElements() {
        val timeRange = TimeRange.MediumTerm
        val index = 55
        val index2 = 21
        val element1 = TopTrackCacheElement(
            index,
            timeRange.toString(),
            "Hope by (Sandy) Alex G",
            10_000,
            LocalDateTime.now().plusDays(2),
            "http://notarealurl.com/album_img.png",
            LocalDateTime.now().minusWeeks(11)
        )
        val element2 = TopTrackCacheElement(
            index2,
            timeRange.toString(),
            "Hacker by Death Grips",
            22_000,
            LocalDateTime.now().minusDays(10),
            "http://anotherurl.com/album_img.png",
            LocalDateTime.now().minusHours(4)
        )
        trackDao.insert(element1)
        trackDao.insert(element2)
        assertEquals(listOf(element1), trackDao.getCachedResult(index, timeRange.toString()))
        assertEquals(listOf(element2), trackDao.getCachedResult(index2, timeRange.toString()))
    }

    @Test
    @Throws(Exception::class)
    fun getCachedElementForArtist() {
        val timeRange = TimeRange.MediumTerm
        val index = 55
        val element1 = TopArtistCacheElement(
            index,
            timeRange.toString(),
            "(Sandy) Alex G",
            124,
            LocalDateTime.now().plusDays(2),
            "http://notarealurl.com/album_img.png",
            LocalDateTime.now().minusWeeks(11)
        )
        val element2 = TopArtistCacheElement(
            index,
            timeRange.toString(),
            "Death Grips",
            666,
            LocalDateTime.now().minusDays(10),
            "http://someotherurl.com/album_img.png",
            LocalDateTime.now().minusHours(4)
        )
        artistDao.insert(element1)
        artistDao.insert(element2)
        assertEquals(listOf(element2), artistDao.getCachedResult(index, timeRange.toString()))
    }

    @Test
    @Throws(Exception::class)
    fun getCachedElementForArtistMultipleElements() {
        val timeRange = TimeRange.MediumTerm
        val timeRange2 = TimeRange.ShortTerm
        val index = 55
        val element1 = TopArtistCacheElement(
            index,
            timeRange.toString(),
            "(Sandy) Alex G",
            124,
            LocalDateTime.now().plusDays(2),
            "http://notarealurl.com/album_img.png",
            LocalDateTime.now().minusWeeks(11)
        )
        val element2 = TopArtistCacheElement(
            index,
            timeRange2.toString(),
            "Death Grips",
            666,
            LocalDateTime.now().minusDays(10),
            "http://urlfornumber2.com/album_img.png",
            LocalDateTime.now().minusHours(4)
        )
        artistDao.insert(element1)
        artistDao.insert(element2)
        assertEquals(listOf(element1), artistDao.getCachedResult(index, timeRange.toString()))
        assertEquals(listOf(element2), artistDao.getCachedResult(index, timeRange2.toString()))
    }

    @Test
    @Throws(Exception::class)
    fun deleteAll() {
        val timeRange = TimeRange.MediumTerm
        val index = 55
        val element = TopTrackCacheElement(
            55,
            TimeRange.MediumTerm.toString(),
            "Hope by (Sandy) Alex G",
            10_000,
            LocalDateTime.now().plusDays(2),
            "http://notarealurl.com/album_img.png",
            LocalDateTime.now().minusWeeks(11)
        )
        trackDao.insert(element)
        trackDao.deleteAll()
        assertEquals(listOf<TopTrackCacheElement>(), trackDao.getCachedResult(index, timeRange.toString()))
    }
}
