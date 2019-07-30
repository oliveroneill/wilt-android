package com.oliveroneill.wilt.data

import com.oliveroneill.wilt.data.ProfileCachedRepository.Companion.TIME_TO_LIVE_IN_DAYS
import com.oliveroneill.wilt.data.dao.TopArtistCacheElement
import com.oliveroneill.wilt.data.dao.TopArtistDao
import com.oliveroneill.wilt.data.dao.TopTrackCacheElement
import com.oliveroneill.wilt.data.dao.TopTrackDao
import com.oliveroneill.wilt.viewmodel.TopArtist
import com.oliveroneill.wilt.viewmodel.TopTrack
import java.time.LocalDateTime
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * An implementation of [ProfileRepository] that will call the [underlyingRepository] and cache the results
 * and the use those results until the cache expires. The cache will expire daily
 */
class ProfileCachedRepository(
    private val underlyingRepository: ProfileRepository,
    private val artistCache: TopArtistDao,
    private val trackCache: TopTrackDao,
    // Used to handle database operations in the background
    private val executor: Executor = Executors.newSingleThreadExecutor()
): ProfileRepository {
    companion object {
        const val TIME_TO_LIVE_IN_DAYS = 1L
    }
    override val currentUser: String?
        get() = underlyingRepository.currentUser

    override fun topArtist(timeRange: TimeRange, index: Int, callback: (Result<TopArtist>) -> Unit) {
        executor.execute {
            val cached = artistCache.getCachedResult(index, timeRange.toString())
            // Check cache
            if (cached.isEmpty() || cached[0].isExpired) {
                // Make request since cache is bad
                underlyingRepository.topArtist(timeRange, index) { result ->
                    result.onSuccess {
                        // We're required to re-enter the executor here since the underlying repository
                        // may not call the callback on the same thread, eg. FirebaseAPI will call
                        // success on the main thread
                        executor.execute {
                            // Cache result
                            artistCache.insert(it.toTopArtistCacheElement(index, timeRange))
                            callback(result)
                        }
                    }.onFailure {
                        callback(result)
                    }
                }
                return@execute
            }
            // Send back cached result
            callback(Result.success(cached[0].toTopArtist()))
        }
    }

    override fun topTrack(timeRange: TimeRange, index: Int, callback: (Result<TopTrack>) -> Unit) {
        executor.execute {
            val cached = trackCache.getCachedResult(index, timeRange.toString())
            // Check cache
            if (cached.isEmpty() || cached[0].isExpired) {
                // Make request since cache is bad
                underlyingRepository.topTrack(timeRange, index) { result ->
                    result.onSuccess {
                        // We're required to re-enter the executor here since the underlying repository
                        // may not call the callback on the same thread, eg. FirebaseAPI will call
                        // success on the main thread
                        executor.execute {
                            // Cache result
                            trackCache.insert(it.toTopTrackCacheElement(index, timeRange))
                            callback(result)
                        }
                    }.onFailure {
                        callback(result)
                    }
                }
                return@execute
            }
            // Send back cached result
            callback(Result.success(cached[0].toTopTrack()))
        }
    }
}

private fun TopTrackCacheElement.toTopTrack(): TopTrack {
    return TopTrack(name , totalPlayDurationMs, lastPlayed)
}

private fun TopTrack.toTopTrackCacheElement(index: Int, timeRange: TimeRange): TopTrackCacheElement {
    return TopTrackCacheElement(
        index,
        timeRange.toString(),
        name,
        totalPlayTimeMs,
        lastPlayed,
        LocalDateTime.now()
    )
}

private val TopTrackCacheElement.isExpired: Boolean
    get() {
        return dateStored.isBefore(LocalDateTime.now().minusDays(TIME_TO_LIVE_IN_DAYS))
    }

private val TopArtistCacheElement.isExpired: Boolean
    get() {
        return dateStored.isBefore(LocalDateTime.now().minusDays(TIME_TO_LIVE_IN_DAYS))
    }

private fun TopArtistCacheElement.toTopArtist(): TopArtist {
    return TopArtist(name, totalPlays, lastPlayed)
}

private fun TopArtist.toTopArtistCacheElement(index: Int, timeRange: TimeRange): TopArtistCacheElement {
    return TopArtistCacheElement(
        index,
        timeRange.toString(),
        name,
        totalPlays,
        lastPlayed,
        LocalDateTime.now()
    )
}
