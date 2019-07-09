package com.oliveroneill.wilt.data

import com.google.firebase.functions.FirebaseFunctions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.oliveroneill.wilt.data.dao.ArtistRank
import com.oliveroneill.wilt.testing.OpenForTesting
import java.time.LocalDate

/**
 * API for making requests to Firebase
 */
@OpenForTesting
class FirebaseAPI {
    private val functions = FirebaseFunctions.getInstance("asia-northeast1")

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
    fun topArtists(from: Int, to: Int, callback: (Result<List<ArtistRank>>) -> Unit) {
        functions
            .getHttpsCallable("getTopArtist")
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
}

