package com.oliveroneill.wilt.walkthrough

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.oliveroneill.wilt.Event
import com.oliveroneill.wilt.R
import com.oliveroneill.wilt.testing.OpenForTesting

/**
 * States that the walkthrough screen can be in
 */
sealed class WalkthroughFragmentState {
    data class LoggingIn(val request: SpotifyAuthenticationRequest): WalkthroughFragmentState()
    data class LoggedIn(val code: String): WalkthroughFragmentState()
    data class LoginError(val error: String): WalkthroughFragmentState()
}

/**
 * ViewModel for walkthrough. This primarily handles login
 */
@OpenForTesting
class WalkthroughFragmentViewModel(application: Application): AndroidViewModel(application) {
    companion object {
        private const val REDIRECT_URI = "wilt://spotify-login"
    }
    private val clientID: String = application.getString(R.string.spotify_client_id)

    /**
     * Set this value to receive Spotify sign in events
     */
    private val _state = MutableLiveData<Event<WalkthroughFragmentState>>()
    val state : LiveData<Event<WalkthroughFragmentState>>
        get() = _state

    /**
     * Start spotify sign up process
     */
    fun spotifySignup() {
        _state.value = Event(
            WalkthroughFragmentState.LoggingIn(
                SpotifyAuthenticationRequest(
                    clientID,
                    REDIRECT_URI,
                    arrayOf("user-read-email", "user-read-recently-played", "user-top-read")
                )
            )
        )
    }

    /**
     * Respond to Spotify auth
     */
    fun onSpotifyLoginResponse(response: SpotifyAuthenticationResponse) {
        when (response) {
            is SpotifyAuthenticationResponse.Success -> {
                _state.value = Event(WalkthroughFragmentState.LoggedIn(response.code))
            }
            is SpotifyAuthenticationResponse.Failure -> {
                _state.value = Event(WalkthroughFragmentState.LoginError(response.error))
            }
        }
    }
}
