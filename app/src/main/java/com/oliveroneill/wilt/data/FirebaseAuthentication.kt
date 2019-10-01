package com.oliveroneill.wilt.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.oliveroneill.wilt.testing.OpenForTesting

/**
 * Authentication helper for logging in with Firebase. Used for signing up new users
 */
@OpenForTesting
class FirebaseAuthentication {
    private val auth = FirebaseAuth.getInstance()
    private val functions = FirebaseFunctions.getInstance("asia-northeast1")

    /**
     * Get the current logged in user, or null if there's no user logged in
     */
    val currentUser: String? by lazy {
        auth.currentUser?.uid
    }

    /**
     * Sign up this user with specified Spotify authorisation code. [callback] will be called with a custom
     * authentication token from the Firebase function
     */
    fun signUp(spotifyAuthCode: String, redirectUri: String, callback: (Result<String>) -> Unit) {
        functions
            .getHttpsCallable("signUp")
            .call(
                hashMapOf(
                    "spotifyAuthCode" to spotifyAuthCode,
                    "spotifyRedirectUri" to redirectUri
                )
            )
            .addOnFailureListener {
                callback(Result.failure(it))
            }
            .addOnSuccessListener {
                val data = it.data as Map<*, *>
                callback(Result.success(data["token"] as String))
            }
    }

    /**
     * Login this user with specified custom auth [token]. [callback] will be called with the username of the logged
     * in user
     */
    fun login(token: String, callback: (Result<String>) -> Unit) {
        auth.signInWithCustomToken(token)
            .addOnSuccessListener {
                callback(Result.success(it.user.uid))
            }.addOnFailureListener {
                callback(Result.failure(it))
            }
    }
}