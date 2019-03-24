package com.oliveroneill.wilt.walkthrough

import com.spotify.sdk.android.authentication.AuthenticationRequest
import com.spotify.sdk.android.authentication.AuthenticationResponse

/**
 * Spotify auth related classes
 */

/**
 * Spotify authentication request. This is a wrapper around Spotify's API
 */
data class SpotifyAuthenticationRequest(private val clientID: String,
                                        private val redirectUri: String,
                                        private val scope: Array<String>) {
    /**
     * Create a Spotify instance from the wrapper
     */
    fun toAuthenticationRequest(): AuthenticationRequest{
        val builder = AuthenticationRequest.Builder(clientID, AuthenticationResponse.Type.TOKEN, redirectUri)
        builder.setScopes(scope)
        return builder.build()
    }
}

/**
 * A sealed class to signify either success or failure of the auth request.
 */
sealed class SpotifyAuthenticationResponse() {
    companion object {
        /**
         * Create a wrapper instance from the Spotify instance
         */
        fun fromAuthenticationResponse(response: AuthenticationResponse): SpotifyAuthenticationResponse {
            return when (response.type) {
                AuthenticationResponse.Type.TOKEN -> Success(response.code)
                AuthenticationResponse.Type.ERROR -> Failure(response.error)
                else -> Failure("Unexpected response type: " + response.type)
            }
        }
    }
    data class Success(val code: String): SpotifyAuthenticationResponse()
    data class Failure(val error: String): SpotifyAuthenticationResponse()
}
