package com.oliveroneill.wilt.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.google.firebase.functions.FirebaseFunctionsException
import com.oliveroneill.wilt.Event
import com.oliveroneill.wilt.R
import com.oliveroneill.wilt.data.FirebaseAPI
import com.oliveroneill.wilt.testing.OpenForTesting
import java.time.LocalDateTime
import java.time.ZoneOffset

@OpenForTesting
class ProfileFragmentViewModel @JvmOverloads constructor(
    application: Application, private val firebase: FirebaseAPI = FirebaseAPI()
): AndroidViewModel(application) {
    /**
     * This will be the list of cards to load and display
     */
    private val cards = listOf<Card>(
        Card.TopArtistCard(0, Timeframe.LongTerm)
    )

    private val _state = MutableLiveData<Event<ProfileState>>()
    val state : LiveData<Event<ProfileState>>
        get() = _state

    init {
        val profileName = firebase.currentUser
        if (profileName == null) {
            _state.postValue(Event(ProfileState.LoggedOut))
        } else {
            cards.forEach {
                when (it) {
                    is Card.TopArtistCard -> {
                        loadTopArtist(profileName)
                    }
                }
            }
        }
    }

    private fun loadTopArtist(profileName: String) {
        _state.postValue(
            // Signal loading state
            Event(
                ProfileState.LoggedIn(
                    ProfileLoggedInState(
                        profileName,
                        listOf(ProfileCardState.Loading)
                    )
                )
            )
        )
        firebase.topArtist {
            it.onSuccess { topArtist ->
                // Post successful response
                _state.postValue(
                    Event(
                        ProfileState.LoggedIn(
                            ProfileLoggedInState(
                                profileName,
                                listOf(ProfileCardState.LoadedTopArtist(topArtist))
                            )
                        )
                    )
                )
            }.onFailure { error ->
                if (error is FirebaseFunctionsException &&
                    error.code == FirebaseFunctionsException.Code.UNAUTHENTICATED) {
                    // Send back a logged out error
                    _state.postValue(Event(ProfileState.LoggedOut))
                    // Short circuit
                    return@onFailure
                }
                _state.postValue(
                    Event(
                        ProfileState.LoggedIn(
                            ProfileLoggedInState(
                                profileName,
                                listOf(
                                    ProfileCardState.Failure(
                                        error.message ?: "Something went wrong"
                                    ) { loadTopArtist(profileName) }
                                )
                            )
                        )
                    )
                )
            }
        }
    }
}

/**
 * Specification for data to request
 */
sealed class Card {
    data class TopArtistCard(val index: Int, val timeframe: Timeframe): Card()
    data class TopTrackCard(val index: Int, val timeframe: Timeframe): Card()
}

/**
 * Timeframe for requests based on Spotify API. See time_range from here:
 * https://developer.spotify.com/documentation/web-api/reference/personalization/get-users-top-artists-and-tracks/
 */
sealed class Timeframe {
    object LongTerm: Timeframe()
    object MediumTerm: Timeframe()
    object ShortTerm: Timeframe()
}

/**
 * The state of the profile fragment. You can either be logged in or logged out. There are
 * extra fields for the logged in state to communicate the response
 */
sealed class ProfileState {
    data class LoggedIn(val state: ProfileLoggedInState) : ProfileState()
    object LoggedOut : ProfileState()
}

/**
 * A user's favourite artist. [lastPlayed] will be null if it has never been played since joining Wilt
 */
data class TopArtist(val name: String, val totalPlays: Int, val lastPlayed: LocalDateTime?)

/**
 * The viewmodel state when logged in
 */
data class ProfileLoggedInState(val profileName: String, val cards: List<ProfileCardState>)

/**
 * The states available when the profile screen is logged in
 */
sealed class ProfileCardState {
    object Loading: ProfileCardState()
    data class LoadedTopArtist(val artist: TopArtist): ProfileCardState()
    data class Failure(val error: String, val retry: () -> Unit): ProfileCardState()

    /**
     * Convert state into a set of necessary data for displaying the view
     */
    fun toViewData(context: Context): ProfileCardViewData {
        when (this) {
            is Loading -> {
                return ProfileCardViewData(
                    loading = true,
                    tagTitle = context.getString(R.string.favourite_artist_title)
                )
            }
            is LoadedTopArtist -> {
                // If lastPlayed is null then we don't have data about how often it was played and
                // when it was played
                if (artist.lastPlayed == null) {
                    return ProfileCardViewData(
                        tagTitle = context.getString(R.string.favourite_artist_title),
                        artistName = artist.name,
                        // We'll leave the strings empty if the date is null. The date will be null
                        // if this artist hasn't been played since joining Wilt
                        lastListenedText = "",
                        playText = ""
                    )
                }
                val lastPlayedRelative = artist.lastPlayed.toRelative()
                return ProfileCardViewData(
                    tagTitle = context.getString(R.string.favourite_artist_title),
                    artistName = artist.name,
                    lastListenedText = context.getString(R.string.last_listened_format, lastPlayedRelative),
                    playText = context.getString(R.string.plays_format, artist.totalPlays)
                )
            }
            is Failure -> {
                return ProfileCardViewData(
                    errorMessage = error,
                    retry = retry
                )
            }
        }
    }
}

/**
 * Convert date into relative timespan since now. For example, "2 days ago"
 */
private fun LocalDateTime.toRelative() = TimeAgo.using(toEpochSecond(ZoneOffset.UTC) * 1000)

/**
 * The data necessary to display the network state for this view model.
 * By default nothing is displayed
 */
data class ProfileCardViewData(
    val loading: Boolean = false,
    val tagTitle: String? = null,
    val artistName: String? = null,
    val lastListenedText: String? = null,
    val playText: String? = null,
    val errorMessage: String? = null,
    val retry: (() -> Unit)? = null
)