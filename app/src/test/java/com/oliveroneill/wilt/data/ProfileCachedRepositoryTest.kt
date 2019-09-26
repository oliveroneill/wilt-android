package com.oliveroneill.wilt.data

import com.nhaarman.mockitokotlin2.*
import com.oliveroneill.wilt.data.dao.TopArtistCacheElement
import com.oliveroneill.wilt.data.dao.TopArtistDao
import com.oliveroneill.wilt.data.dao.TopTrackCacheElement
import com.oliveroneill.wilt.data.dao.TopTrackDao
import com.oliveroneill.wilt.viewmodel.TopArtist
import com.oliveroneill.wilt.viewmodel.TopTrack
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.fail
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.LocalDateTime

class ProfileCachedRepositoryTest {
    private lateinit var networkAPI: ProfileRepository
    private lateinit var trackCache: TopTrackDao
    private lateinit var artistCache: TopArtistDao
    private lateinit var cache: ProfileCachedRepository

    @Before
    fun setup() {
        networkAPI = mock()
        trackCache = mock()
        artistCache = mock()
        cache = ProfileCachedRepository(
            networkAPI, artistCache, trackCache,
            ArtistRankBoundaryCallbackTest.CurrentThreadExecutor()
        )
    }

    @Test
    fun `should use cache when valid for artist`() {
        val timeRange = TimeRange.MediumTerm
        val index = 55
        val date = LocalDateTime.now()
        val expected = TopArtist(
            "(Sandy) Alex G",
            124,
            date,
            "http://notarealurl.com/album_img.png",
            "http://anotherrandomurl.net/link",
            "spotify://arandomurl.net/link"
        )
        val cachedElement = TopArtistCacheElement(
            index,
            timeRange.toString(),
            "(Sandy) Alex G",
            124,
            date,
            "http://notarealurl.com/album_img.png",
            "http://anotherrandomurl.net/link",
            "spotify://arandomurl.net/link",
            LocalDateTime.now()
        )
        whenever(artistCache.getCachedResult(eq(index), eq(timeRange.toString()))).thenReturn(
            listOf(cachedElement)
        )
        cache.topArtist(timeRange, index) {
            it.onSuccess { artist ->
                assertEquals(expected, artist)
            }.onFailure {
                fail()
            }
        }
    }

    @Test
    fun `should not use expired cache for artist`() {
        val timeRange = TimeRange.MediumTerm
        val index = 55
        val date = LocalDateTime.now()
        // This value is different to the cache value because we'll call the underlying API
        val expected = TopArtist(
            "Death Grips",
            112,
            date.minusWeeks(2),
            "http://notarealurl.com/album_img.png",
            "http://anotherrandomurl.net/link",
            "spotify://arandomurl.net/link"
        )
        whenever(artistCache.getCachedResult(eq(index), eq(timeRange.toString()))).thenReturn(
            listOf(
                TopArtistCacheElement(
                    index,
                    timeRange.toString(),
                    "(Sandy) Alex G",
                    124,
                    date,
                    "http://notarealurl.com/album_img.png",
                    "http://anotherrandomurl.net/link",
                    "spotify://arandomurl.net/link",
                    LocalDateTime.now().minusWeeks(1)
                )
            )
        )
        whenever(networkAPI.topArtist(eq(timeRange), eq(index), any())).then {
            (it.getArgument(2) as (Result<TopArtist>) -> Unit).invoke(Result.success(expected))
        }
        cache.topArtist(timeRange, index) {
            it.onSuccess { artist ->
                assertEquals(expected, artist)
            }.onFailure {
                fail()
            }
        }
    }

    @Test
    fun `should request new data when cache is empty`() {
        val timeRange = TimeRange.MediumTerm
        val index = 55
        val date = LocalDateTime.now()
        // This value is different to the cache value because we'll call the underlying API
        val expected = TopArtist(
            "Death Grips",
            112,
            date.minusWeeks(2),
            "http://notarealurl.com/album_img.png",
            "http://anotherrandomurl.net/link",
            "spotify://arandomurl.net/link"
        )
        whenever(artistCache.getCachedResult(eq(index), eq(timeRange.toString()))).thenReturn(listOf())
        whenever(networkAPI.topArtist(eq(timeRange), eq(index), any())).then {
            (it.getArgument(2) as (Result<TopArtist>) -> Unit).invoke(Result.success(expected))
        }
        cache.topArtist(timeRange, index) {
            it.onSuccess { artist ->
                assertEquals(expected, artist)
            }.onFailure {
                fail()
            }
        }
    }

    @Test
    fun `should insert when receiving new data for artist`() {
        val timeRange = TimeRange.MediumTerm
        val index = 55
        val date = LocalDateTime.now()
        whenever(artistCache.getCachedResult(eq(index), eq(timeRange.toString()))).thenReturn(listOf())
        whenever(networkAPI.topArtist(eq(timeRange), eq(index), any())).then {
            (it.getArgument(2) as (Result<TopArtist>) -> Unit).invoke(
                Result.success(
                    TopArtist(
                        "Death Grips",
                        112,
                        date.minusWeeks(2),
                        "http://notarealurl.com/album_img.png",
                        "http://anotherrandomurl.net/link",
                        "spotify://arandomurl.net/link"
                    )
                )
            )
        }
        cache.topArtist(timeRange, index) { }
        val expected = TopArtistCacheElement(
            index,
            timeRange.toString(),
            "Death Grips",
            112,
            date.minusWeeks(2),
            "http://notarealurl.com/album_img.png",
            "http://anotherrandomurl.net/link",
            "spotify://arandomurl.net/link",
            LocalDateTime.now()
        )
        verify(artistCache).insert(argThat {
            // Match everything except the new date stored
            artistIndex == expected.artistIndex &&
                    this.timeRange == expected.timeRange &&
                    name == expected.name &&
                    totalPlays == expected.totalPlays &&
                    lastPlayed == expected.lastPlayed
        })
    }

    @Test
    fun `should use cache when valid for track`() {
        val timeRange = TimeRange.MediumTerm
        val index = 55
        val date = LocalDateTime.now()
        val expected = TopTrack(
            "Hope by (Sandy) Alex G",
            10_000,
            date,
            "http://notarealurl.com/album_img.png",
            "http://anotherrandomurl.net/link",
            "spotify://arandomurl.net/link"
        )
        val cachedElement = TopTrackCacheElement(
            index,
            timeRange.toString(),
            "Hope by (Sandy) Alex G",
            10_000,
            date,
            "http://notarealurl.com/album_img.png",
            "http://anotherrandomurl.net/link",
            "spotify://arandomurl.net/link",
            LocalDateTime.now()
        )
        whenever(trackCache.getCachedResult(eq(index), eq(timeRange.toString()))).thenReturn(
            listOf(cachedElement)
        )
        cache.topTrack(timeRange, index) {
            it.onSuccess { track ->
                assertEquals(expected, track)
            }.onFailure {
                fail()
            }
        }
    }


    @Test
    fun `should not use expired cache for track`() {
        val timeRange = TimeRange.MediumTerm
        val index = 55
        val date = LocalDateTime.now()
        val expected = TopTrack(
            "Hacker by Death Grips",
            22_000,
            date.minusWeeks(2),
            "http://notarealurl.com/album_img.png",
            "http://anotherrandomurl.net/link",
            "spotify://arandomurl.net/link"
        )
        whenever(trackCache.getCachedResult(eq(index), eq(timeRange.toString()))).thenReturn(
            listOf(
                TopTrackCacheElement(
                    index,
                    timeRange.toString(),
                    "Hope by (Sandy) Alex G",
                    10_000,
                    date,
                    "http://notarealurl.com/album_img.png",
                    "http://anotherrandomurl.net/link",
                    "spotify://arandomurl.net/link",
                    LocalDateTime.now().minusWeeks(1)
                )
            )
        )
        whenever(networkAPI.topTrack(eq(timeRange), eq(index), any())).then {
            (it.getArgument(2) as (Result<TopTrack>) -> Unit).invoke(Result.success(expected))
        }
        cache.topTrack(timeRange, index) {
            it.onSuccess { track ->
                assertEquals(expected, track)
            }.onFailure {
                fail()
            }
        }
    }

    @Test
    fun `should make request when cache is empty for track`() {
        val timeRange = TimeRange.MediumTerm
        val index = 55
        val date = LocalDateTime.now()
        val expected = TopTrack(
            "Hacker by Death Grips",
            22_000,
            date.minusWeeks(2),
            "http://notarealurl.com/album_img.png",
            "http://anotherrandomurl.net/link",
            "spotify://arandomurl.net/link"
        )
        whenever(trackCache.getCachedResult(eq(index), eq(timeRange.toString()))).thenReturn(listOf())
        whenever(networkAPI.topTrack(eq(timeRange), eq(index), any())).then {
            (it.getArgument(2) as (Result<TopTrack>) -> Unit).invoke(Result.success(expected))
        }
        cache.topTrack(timeRange, index) {
            it.onSuccess { track ->
                assertEquals(expected, track)
            }.onFailure {
                fail()
            }
        }
    }

    @Test
    fun `should insert when receiving new data for track`() {
        val timeRange = TimeRange.MediumTerm
        val index = 55
        val date = LocalDateTime.now()
        whenever(trackCache.getCachedResult(eq(index), eq(timeRange.toString()))).thenReturn(listOf())
        whenever(networkAPI.topTrack(eq(timeRange), eq(index), any())).then {
            (it.getArgument(2) as (Result<TopTrack>) -> Unit).invoke(
                Result.success(
                    TopTrack(
                        "Hacker by Death Grips",
                        22_000,
                        date.minusWeeks(2),
                        "http://notarealurl.com/album_img.png",
                        "http://anotherrandomurl.net/link",
                        "spotify://arandomurl.net/link"
                    )
                )
            )
        }
        cache.topTrack(timeRange, index) {}
        val expected = TopTrackCacheElement(
            index,
            timeRange.toString(),
            "Hacker by Death Grips",
            22_000,
            date.minusWeeks(2),
            "http://notarealurl.com/album_img.png",
            "http://anotherrandomurl.net/link",
            "spotify://arandomurl.net/link",
            LocalDateTime.now()
        )
        verify(trackCache).insert(argThat {
            // Match everything except the new date stored
            trackIndex == expected.trackIndex &&
                    this.timeRange == expected.timeRange &&
                    name == expected.name &&
                    totalPlayDurationMs == expected.totalPlayDurationMs &&
                    lastPlayed == expected.lastPlayed
        })
    }

    @Test
    fun `should send error through for track when expired`() {
        val timeRange = TimeRange.MediumTerm
        val index = 55
        val expected = IOException("This is a test message")
        whenever(trackCache.getCachedResult(eq(index), eq(timeRange.toString()))).thenReturn(listOf())
        whenever(networkAPI.topTrack(eq(timeRange), eq(index), any())).then {
            (it.getArgument(2) as (Result<TopTrack>) -> Unit).invoke(Result.failure(expected))
        }
        cache.topTrack(timeRange, index) {
            it.onSuccess {
                fail()
            }.onFailure {error ->
                assertEquals(expected, error)
            }
        }
    }

    @Test
    fun `should send error through for artist when expired`() {
        val timeRange = TimeRange.MediumTerm
        val index = 55
        val expected = IOException("This is a test message")
        whenever(artistCache.getCachedResult(eq(index), eq(timeRange.toString()))).thenReturn(listOf())
        whenever(networkAPI.topTrack(eq(timeRange), eq(index), any())).then {
            (it.getArgument(2) as (Result<TopTrack>) -> Unit).invoke(Result.failure(expected))
        }
        cache.topArtist(timeRange, index) {
            it.onSuccess {
                fail()
            }.onFailure {error ->
                assertEquals(expected, error)
            }
        }
    }
}
