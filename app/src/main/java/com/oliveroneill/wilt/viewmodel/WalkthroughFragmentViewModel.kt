package com.oliveroneill.wilt.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.oliveroneill.wilt.Event
import com.oliveroneill.wilt.R
import com.oliveroneill.wilt.data.FirebaseAuthentication
import com.oliveroneill.wilt.data.SpotifyAuthenticationRequest
import com.oliveroneill.wilt.data.SpotifyAuthenticationResponse
import com.oliveroneill.wilt.testing.OpenForTesting

/**
 * States that the walkthrough screen can be in
 */
sealed class WalkthroughFragmentState {
    // Initial state for displaying the walkthrough
    object Walkthrough : WalkthroughFragmentState()
    data class LoggingIn(val request: SpotifyAuthenticationRequest): WalkthroughFragmentState()
    data class LoggedIn(val code: String): WalkthroughFragmentState()
    data class LoginError(val error: String): WalkthroughFragmentState()
}

/**
 * ViewModel for walkthrough. This primarily handles login
 */
@OpenForTesting
class WalkthroughFragmentViewModel @JvmOverloads constructor(application: Application,
                                                             private val firebase: FirebaseAuthentication = FirebaseAuthentication(application)
): AndroidViewModel(application) {
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

    init {
        // Set initial state
        _state.value = Event(WalkthroughFragmentState.Walkthrough)
    }

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
                wiltLogin(response.code)
            }
            is SpotifyAuthenticationResponse.Failure -> {
                _state.value = Event(WalkthroughFragmentState.LoginError(response.error))
            }
        }
    }

    private fun wiltLogin(spotifyAuthCode: String) {
        firebase.login(spotifyAuthCode) {
            _state.postValue(
                it.fold({
                    Event(WalkthroughFragmentState.LoggedIn(it))
                }, {
                    it.printStackTrace()
                    Event(WalkthroughFragmentState.LoginError("Firebase signUp failed"))
                })
            )
        }
    }
}
