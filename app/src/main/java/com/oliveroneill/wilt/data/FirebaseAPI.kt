package com.oliveroneill.wilt.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.oliveroneill.wilt.data.dao.ArtistRank
import com.oliveroneill.wilt.testing.OpenForTesting
import com.oliveroneill.wilt.viewmodel.TopArtist
import com.oliveroneill.wilt.viewmodel.TopTrack
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * TimeRange for requests based on Spotify API. See time_range from here:
 * https://developer.spotify.com/documentation/web-api/reference/personalization/get-users-top-artists-and-tracks/
 */
sealed class TimeRange {
    object LongTerm: TimeRange()
    object MediumTerm: TimeRange()
    object ShortTerm: TimeRange()

    override fun toString(): String {
        return when (this) {
            is LongTerm -> "long_term"
            is MediumTerm -> "medium_term"
            is ShortTerm -> "short_term"
        }
    }
}

/**
 * API for making requests to Firebase.
 */
@OpenForTesting
class FirebaseAPI: ProfileRepository {
    private val functions = FirebaseFunctions.getInstance("asia-northeast1")
    private val auth = FirebaseAuth.getInstance()
    private val gson = Gson()
    /**
     * Token types for JSON conversion
     */
    private val artistType = object : TypeToken<FirebaseTopArtist>() {}.type
    private val trackType = object : TypeToken<FirebaseTopTrack>() {}.type
    private val ranksType = object : TypeToken<List<FirebaseArtistRank>>() {}.type

    /**
     * Get the current logged in user, or null if there's no user logged in
     */
    override val currentUser: String? by lazy {
        auth.currentUser?.uid
    }

    /**
     * The firebase response for the play history call
     */
    private data class FirebaseArtistRank(
        val week: String,
        val date: String,
        val top_artist: String,
        val count: Int,
        val imageUrl: String,
        val externalUrl: String,
        val spotifyUrl: String
    ) {
        /**
         * Convert this instance into the Room database entity
         */
        fun toArtistRank(): ArtistRank {
            return ArtistRank(week, LocalDate.parse(date), top_artist, count, imageUrl, externalUrl, spotifyUrl)
        }
    }

    /**
     * Get the top artists each month between [from] and [to] specified as unix timestamps in seconds.
     */
    fun topArtistsPerWeek(from: Int, to: Int, callback: (Result<List<ArtistRank>>) -> Unit) {
        functions
            .getHttpsCallable("getTopArtistPerWeek")
            .call(
                hashMapOf(
                    "start" to from,
                    "end" to to
                )
            )
            .addOnFailureListener {
                callback(Result.failure(it))
            }
            .addOnSuccessListener {
                // Convert list of map into ArtistRank class
                val jsonElement = gson.toJsonTree(it.data)
                val rankHistory = gson.fromJson<List<FirebaseArtistRank>>(jsonElement, ranksType)
                callback(Result.success(rankHistory.map { r -> r.toArtistRank() }))
            }
    }

    data class FirebaseDate(val value: String)

    /**
     * Internal format returned from firebase response
     */
    data class FirebaseTopArtist(val name: String,
                                 val count: Int,
                                 val lastPlay: FirebaseDate?,
                                 val imageUrl: String,
                                 val externalUrl: String,
                                 val spotifyUrl: String) {
        fun toTopArtist(): TopArtist {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            return TopArtist(
                name,
                count,
                if (lastPlay == null) null else LocalDateTime.parse(lastPlay.value, formatter),
                imageUrl,
                externalUrl,
                spotifyUrl
            )
        }
    }

    /**
     * Internal format returned from firebase response
     */
    data class FirebaseTopTrack(val name: String,
                                val totalPlayTimeMs: Long,
                                val lastPlay: FirebaseDate?,
                                val imageUrl: String,
                                val externalUrl: String,
                                val spotifyUrl: String) {
        fun toTopTrack(): TopTrack {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            return TopTrack(
                name,
                totalPlayTimeMs,
                if (lastPlay == null) null else LocalDateTime.parse(lastPlay.value, formatter),
                imageUrl,
                externalUrl,
                spotifyUrl
            )
        }
    }

    override fun topArtist(timeRange: TimeRange, index: Int, callback: (Result<TopArtist>) -> Unit) {
        functions
            .getHttpsCallable("topArtist")
            .call(
                hashMapOf(
                    "timeRange" to timeRange.toString(),
                    "index" to index
                )
            )
            .addOnFailureListener {
                callback(Result.failure(it))
            }
            .addOnSuccessListener {
                val jsonElement = gson.toJsonTree(it.data)
                val data = gson.fromJson<FirebaseTopArtist>(jsonElement, artistType)
                callback(Result.success(data.toTopArtist()))
            }
    }

    override fun topTrack(timeRange: TimeRange, index: Int, callback: (Result<TopTrack>) -> Unit) {
        functions
            .getHttpsCallable("topTrack")
            .call(
                hashMapOf(
                    "timeRange" to timeRange.toString(),
                    "index" to index
                )
            )
            .addOnFailureListener {
                callback(Result.failure(it))
            }
            .addOnSuccessListener {
                val jsonElement = gson.toJsonTree(it.data)
                val data = gson.fromJson<FirebaseTopTrack>(jsonElement, trackType)
                callback(Result.success(data.toTopTrack()))
            }
    }

    fun logout() {
        auth.signOut()
    }
}

