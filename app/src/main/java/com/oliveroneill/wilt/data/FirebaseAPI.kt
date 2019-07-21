package com.oliveroneill.wilt.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.oliveroneill.wilt.data.dao.ArtistRank
import com.oliveroneill.wilt.testing.OpenForTesting
import com.oliveroneill.wilt.viewmodel.TopArtist
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * API for making requests to Firebase.
 */
@OpenForTesting
class FirebaseAPI {
    private val functions = FirebaseFunctions.getInstance("asia-northeast1")
    private val auth = FirebaseAuth.getInstance()

    /**
     * Get the current logged in user, or null if there's no user logged in
     */
    val currentUser: String? by lazy {
        auth.currentUser?.uid
    }

    /**
     * The firebase response for the play history call
     */
    private data class FirebaseArtistRank(
        val week: String,
        val date: String,
        val top_artist: String,
        val count: Int
    ) {
        /**
         * Convert this instance into the Room database entity
         */
        fun toArtistRank(): ArtistRank {
            return ArtistRank(week, LocalDate.parse(date), top_artist, count)
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
                val gson = Gson()
                val jsonElement = gson.toJsonTree(it.data)
                val ranksType = object : TypeToken<List<FirebaseArtistRank>>() {}.type
                val rankHistory = gson.fromJson<List<FirebaseArtistRank>>(jsonElement, ranksType)
                callback(Result.success(rankHistory.map { it.toArtistRank() }))
            }
    }

    data class FirebaseDate(val value: String)

    /**
     * Internal format returned from firebase response
     */
    data class FirebaseTopArtist(val name: String, val count: Int, val lastPlay: FirebaseDate?) {
        fun toTopArtist(): TopArtist {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            return TopArtist(
                name,
                count,
                if (lastPlay == null) null else LocalDateTime.parse(lastPlay.value, formatter)
            )
        }
    }

    fun topArtist(callback: (Result<TopArtist>) -> Unit) {
        functions
            .getHttpsCallable("topArtist")
            .call()
            .addOnFailureListener {
                callback(Result.failure(it))
            }
            .addOnSuccessListener {
                val gson = Gson()
                val jsonElement = gson.toJsonTree(it.data)
                val dataType = object : TypeToken<FirebaseTopArtist>() {}.type
                val data = gson.fromJson<FirebaseTopArtist>(jsonElement, dataType)
                callback(Result.success(data.toTopArtist()))
            }
    }
}

