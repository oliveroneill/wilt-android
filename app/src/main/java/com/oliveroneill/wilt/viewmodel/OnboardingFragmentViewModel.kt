package com.oliveroneill.wilt.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.oliveroneill.wilt.Data
import com.oliveroneill.wilt.Event
import com.oliveroneill.wilt.Message
import com.oliveroneill.wilt.R
import com.oliveroneill.wilt.data.FirebaseAuthentication
import com.oliveroneill.wilt.data.SpotifyAuthenticationRequest
import com.oliveroneill.wilt.data.SpotifyAuthenticationResponse
import com.oliveroneill.wilt.data.dao.*
import com.oliveroneill.wilt.testing.OpenForTesting
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * States that the onboarding screen can be in
 */
sealed class OnboardingFragmentState {
    // Initial state for displaying onboarding
    object Onboarding : OnboardingFragmentState()
    data class AuthenticatingSpotify(val request: SpotifyAuthenticationRequest): OnboardingFragmentState()
    data class LoggedIn(val username: String): OnboardingFragmentState()
    data class LoginError(val error: String): OnboardingFragmentState()
}

/**
 * A page that the user will swipe through to get an intro to the app
 */
data class OnboardingPage(
    /**
     * The subtitle to the image that will displayed on this page
     */
    val title: String,
    /**
     * An ID for the image to be displayed
     */
    val imageResID: Int
)

/**
 * ViewModel for onboarding. This primarily handles login
 */
@OpenForTesting
class OnboardingFragmentViewModel
@JvmOverloads
constructor(application: Application,
            private val firebase: FirebaseAuthentication = FirebaseAuthentication(),
            // Database objects are passed in so we can clear them between each user
            private val feedDao: PlayHistoryDao = PlayHistoryDatabase.getDatabase(application).historyDao(),
            private val artistCache: TopArtistDao = TopArtistDatabase.getDatabase(application).topArtistCache(),
            private val trackCache: TopTrackDao = TopTrackDatabase.getDatabase(application).topTrackCache(),
            // Used to complete the login task in the background
            private val executor: Executor = Executors.newSingleThreadExecutor()
): AndroidViewModel(application) {
    private val redirectUri = application.getString(R.string.spotify_redirect_uri)
    private val clientID: String = application.getString(R.string.spotify_client_id)
    /**
     * The pages to display
     */
    val pages = listOf(
        OnboardingPage(title = application.getString(R.string.walkthrough1_text), imageResID = R.drawable.walkthrough1),
        OnboardingPage(title = application.getString(R.string.walkthrough2_text), imageResID = R.drawable.walkthrough2)
    )

    /**
     * Set this value to receive Spotify sign in events
     */
    private val _state = MutableLiveData<Message<OnboardingFragmentState>>()
    val state : LiveData<Message<OnboardingFragmentState>>
        get() = _state

    init {
        // Set initial state
        _state.value = firebase.currentUser?.let {
            Event(OnboardingFragmentState.LoggedIn(it))
        } ?: run {
            Data(OnboardingFragmentState.Onboarding)
        }
    }

    /**
     * Start spotify sign up process
     */
    fun spotifySignup() {
        _state.postValue(
            Event(
                OnboardingFragmentState.AuthenticatingSpotify(
                    SpotifyAuthenticationRequest(
                        clientID,
                        redirectUri,
                        arrayOf("user-read-email", "user-read-recently-played", "user-top-read")
                    )
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
                // In the background, we'll clear the cache and login to wilt
                executor.execute {
                    clearCache()
                    wiltLogin(response.code)
                }
            }
            is SpotifyAuthenticationResponse.Failure -> {
                _state.postValue(Event(OnboardingFragmentState.LoginError(response.error)))
            }
        }
    }

    /**
     * Since this user hasn't logged in before (or possibly got logged out), it's safest to empty
     * the database since it's being used as a cache. This is a necessary step to avoid storing
     * data of a different user. Potentially a better way to handle this would be to store the user
     * and check whether it's different...
     */
    private fun clearCache() {
        feedDao.deleteAll()
        trackCache.deleteAll()
        artistCache.deleteAll()
    }

    private fun wiltLogin(spotifyAuthCode: String) {
        firebase.signUp(spotifyAuthCode, redirectUri) {
            it.onSuccess { firebaseToken ->
                firebase.login(firebaseToken) { result ->
                    _state.postValue(
                        result.fold({userId ->
                            Event(OnboardingFragmentState.LoggedIn(userId))
                        }, { error ->
                            error.printStackTrace()
                            Event(OnboardingFragmentState.LoginError("Firebase auth failed"))
                        })
                    )
                }
            }.onFailure { error ->
                error.printStackTrace()
                _state.postValue(Event(OnboardingFragmentState.LoginError("Firebase signUp failed")))
            }
        }
    }
}
