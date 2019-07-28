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
import com.oliveroneill.wilt.data.TimeRange
import com.oliveroneill.wilt.testing.OpenForTesting
import java.time.LocalDateTime
import java.time.ZoneOffset

@OpenForTesting
class ProfileFragmentViewModel @JvmOverloads constructor(
    application: Application,
    private val firebase: FirebaseAPI = FirebaseAPI(),
    /**
     * This will be the list of cards to load and display
     */
    cards: List<Card> = listOf<Card>(
        Card.TopArtistCard(0, TimeRange.LongTerm)
    )
): AndroidViewModel(application) {
    /**
     * Keep track of each cards state
     */
    private var cardStates: MutableList<ProfileCardState> = mutableListOf()

    private val _state = MutableLiveData<Event<ProfileState>>()
    val state : LiveData<Event<ProfileState>>
        get() = _state

    init {
        val profileName = firebase.currentUser
        if (profileName == null) {
            _state.postValue(Event(ProfileState.LoggedOut))
        } else {
            // Set the initial state of each card to loading
            cardStates = (cards.map {
                when (it) {
                    is Card.TopArtistCard -> {
                        ProfileCardState.Loading(it.timeRange)
                    }
                    is Card.TopTrackCard -> {
                        ProfileCardState.Loading(it.timeRange)
                    }
                }
            }).toMutableList()
            // Load each card
            cards.forEachIndexed { index, card ->
                when (card) {
                    is Card.TopArtistCard -> {
                        loadTopArtist(profileName, card.timeRange, card.index, index)
                    }
                    // TODO: top track load
                }
            }
        }
    }

    /**
     * Update the state of card positioned at [index] to [newState].
     * This will return the new set of card states
     */
    private fun updatedCardStates(index: Int, newState: ProfileCardState): List<ProfileCardState> {
        synchronized(this) {
            cardStates[index] = newState
            return cardStates
        }
    }

    /**
     * Post the new state value for a specific card at [cardIndex] and new state [newState]
     */
    private fun postNewStateForCard(profileName: String, cardIndex: Int, newState: ProfileCardState) {
        _state.postValue(
            Event(
                ProfileState.LoggedIn(
                    ProfileLoggedInState(
                        profileName,
                        updatedCardStates(cardIndex, newState)
                    )
                )
            )
        )
    }

    /**
     * Get top artist for the specified [timeRange] ranked at [artistIndex] for card
     * positioned at [cardIndex].
     * [profileName] is used to update the UI
     */
    private fun loadTopArtist(profileName: String, timeRange: TimeRange, artistIndex: Int, cardIndex: Int) {
        // Signal loading state
        postNewStateForCard(
            profileName,
            cardIndex,
            ProfileCardState.Loading(timeRange)
        )
        firebase.topArtist(timeRange, artistIndex) {
            it.onSuccess { topArtist ->
                // Post successful response
                postNewStateForCard(
                    profileName,
                    cardIndex,
                    ProfileCardState.LoadedTopArtist(timeRange, topArtist)
                )
            }.onFailure { error ->
                if (error is FirebaseFunctionsException &&
                    error.code == FirebaseFunctionsException.Code.UNAUTHENTICATED) {
                    // Send back a logged out error
                    _state.postValue(Event(ProfileState.LoggedOut))
                    // Short circuit
                    return@onFailure
                }
                postNewStateForCard(
                    profileName,
                    cardIndex,
                    ProfileCardState.Failure(
                        error.message ?: "Something went wrong"
                    )
                    { loadTopArtist(profileName, timeRange, artistIndex, cardIndex) }

                )
            }
        }
    }
}

/**
 * Specification for data to request
 */
sealed class Card {
    /**
     * The top artist card request, where [index] is the position in the ranking of the artist
     */
    data class TopArtistCard(val index: Int, val timeRange: TimeRange): Card()
    /**
     * The top track card request, where [index] is the position in the ranking of the track
     */
    data class TopTrackCard(val index: Int, val timeRange: TimeRange): Card()
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
    data class Loading(val timeRange: TimeRange): ProfileCardState()
    data class LoadedTopArtist(val timeRange: TimeRange, val artist: TopArtist): ProfileCardState()
    data class Failure(val error: String, val retry: () -> Unit): ProfileCardState()

    /**
     * Convert state into a set of necessary data for displaying the view
     */
    fun toViewData(context: Context): ProfileCardViewData {
        when (this) {
            is Loading -> {
                return ProfileCardViewData(
                    loading = true,
                    tagTitle = timeRange.toReadableString(context)
                )
            }
            is LoadedTopArtist -> {
                // If lastPlayed is null then we don't have data about how often it was played and
                // when it was played
                if (artist.lastPlayed == null) {
                    return ProfileCardViewData(
                        tagTitle = timeRange.toReadableString(context),
                        artistName = artist.name,
                        // We'll leave the strings empty if the date is null. The date will be null
                        // if this artist hasn't been played since joining Wilt
                        lastListenedText = "",
                        playText = ""
                    )
                }
                val lastPlayedRelative = artist.lastPlayed.toRelative()
                return ProfileCardViewData(
                    tagTitle = timeRange.toReadableString(context),
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
 * Convert the timeRange to a string to be presented to the UI
 */
private fun TimeRange.toReadableString(context: Context): String? {
    return when (this) {
        is TimeRange.LongTerm -> context.getString(R.string.favourite_artist_title_long_term)
        is TimeRange.MediumTerm -> context.getString(R.string.favourite_artist_title_medium_term)
        is TimeRange.ShortTerm -> context.getString(R.string.favourite_artist_title_short_term)
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