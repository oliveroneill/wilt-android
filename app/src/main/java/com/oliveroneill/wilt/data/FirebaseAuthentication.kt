package com.oliveroneill.wilt.data

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.functions.FirebaseFunctions
import com.oliveroneill.wilt.testing.OpenForTesting

/**
 * Authentication helper for logging in with Firebase. Used for signing up new users
 */
@OpenForTesting
class FirebaseAuthentication(context: Context) {
    init {
        FirebaseApp.initializeApp(context)
    }

    /**
     * Sign in this user with specified Spotify authorisation code. [callback] will be called with the authorisation
     * token from the Firebase function
     */
    fun login(spotifyAuthCode: String, callback: (Result<String>) -> Unit) {
        FirebaseFunctions.getInstance()
            .getHttpsCallable("signUp")
            .call(hashMapOf("user_id" to spotifyAuthCode))
            .addOnFailureListener {
                callback(Result.failure(it))
            }
            .addOnSuccessListener {
                val data = it.data as Map<*, *>
                callback(Result.success(data.get("token") as String))
            }
    }
}