package com.oliveroneill.wilt.data

import com.google.firebase.functions.FirebaseFunctions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.oliveroneill.wilt.testing.OpenForTesting
import com.oliveroneill.wilt.viewmodel.ArtistRank

/**
 * API for making requests to Firebase
 */
@OpenForTesting
class FirebaseAPI {
    private val functions = FirebaseFunctions.getInstance()

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
                val ranksType = object : TypeToken<List<ArtistRank>>() {}.type
                val rankHistory = gson.fromJson<List<ArtistRank>>(jsonElement, ranksType)
                callback(Result.success(rankHistory))
            }
    }
}
