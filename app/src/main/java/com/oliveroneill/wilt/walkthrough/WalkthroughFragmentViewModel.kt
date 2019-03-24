package com.oliveroneill.wilt.walkthrough

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.oliveroneill.wilt.R
import com.oliveroneill.wilt.SingleLiveEvent

/**
 * States that the walkthrough screen can be in
 */
sealed class WalkthroughFragmentState() {
    data class LoggingIn(val request: SpotifyAuthenticationRequest): WalkthroughFragmentState();
    data class LoggedIn(val code: String): WalkthroughFragmentState();
    data class LoginError(val error: String): WalkthroughFragmentState();
}

/**
 * ViewModel for walkthrough. This primarily handles login
 */
class WalkthroughFragmentViewModel(application: Application): AndroidViewModel(application) {
    private val REDIRECT_URI = "wilt://spotify-login"
    private val CLIENT_ID: String = application.getString(R.string.spotify_client_id)

    /**
     * Set this value to receive Spotify sign in events
     */
    val state = SingleLiveEvent<WalkthroughFragmentState>()

    /**
     * Start spotify sign up process
     */
    fun spotifySignup() {
        state.value = WalkthroughFragmentState.LoggingIn(
            SpotifyAuthenticationRequest(
                CLIENT_ID,
                REDIRECT_URI,
                arrayOf("user-read-email", "user-read-recently-played", "user-top-read")
            )
        )
    }

    /**
     * Respond to Spotify auth
     */
    fun onSpotifyLoginResponse(response: SpotifyAuthenticationResponse) {
        when (response) {
            is SpotifyAuthenticationResponse.Success -> {
                state.value = WalkthroughFragmentState.LoggedIn(response.code)
            }
            is SpotifyAuthenticationResponse.Failure -> {
                state.value = WalkthroughFragmentState.LoginError(response.error)
            }
        }
    }
}
