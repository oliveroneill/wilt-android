package com.oliveroneill.wilt.viewmodel

import android.app.Application
import android.content.Context
import androidx.annotation.VisibleForTesting
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
    cards: List<Card> = listOf(
        Card.TopArtistCard(0, TimeRange.LongTerm),
        Card.TopTrackCard(0, TimeRange.LongTerm),
        Card.TopArtistCard(0, TimeRange.ShortTerm),
        Card.TopTrackCard(0, TimeRange.ShortTerm),
        Card.TopArtistCard(0, TimeRange.MediumTerm),
        Card.TopTrackCard(0, TimeRange.MediumTerm)
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
                        ProfileCardState.Loading(CardType.TOP_ARTIST, it.timeRange)
                    }
                    is Card.TopTrackCard -> {
                        ProfileCardState.Loading(CardType.TOP_TRACK, it.timeRange)
                    }
                }
            }).toMutableList()
            // Load each card
            cards.forEachIndexed { index, card ->
                when (card) {
                    is Card.TopArtistCard -> {
                        loadTopArtist(profileName, card.timeRange, card.index, index)
                    }
                    is Card.TopTrackCard -> {
                        loadTopTrack(profileName, card.timeRange, card.index, index)
                    }
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
            ProfileCardState.Loading(CardType.TOP_ARTIST, timeRange)
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

    /**
     * Get top track for the specified [timeRange] ranked at [trackIndex] for card
     * positioned at [cardIndex].
     * [profileName] is used to update the UI
     */
    private fun loadTopTrack(profileName: String, timeRange: TimeRange, trackIndex: Int, cardIndex: Int) {
        // Signal loading state
        postNewStateForCard(
            profileName,
            cardIndex,
            ProfileCardState.Loading(CardType.TOP_TRACK, timeRange)
        )
        firebase.topTrack(timeRange, trackIndex) {
            it.onSuccess { topTrack ->
                // Post successful response
                postNewStateForCard(
                    profileName,
                    cardIndex,
                    ProfileCardState.LoadedTopTrack(timeRange, topTrack)
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
                    { loadTopTrack(profileName, timeRange, trackIndex, cardIndex) }

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

data class TopTrack(val name: String, val totalPlayTimeMs: Long, val lastPlayed: LocalDateTime?)

/**
 * The viewmodel state when logged in
 */
data class ProfileLoggedInState(val profileName: String, val cards: List<ProfileCardState>)

/**
 * The states available when the profile screen is logged in
 */
sealed class ProfileCardState {
    data class Loading(val cardType: CardType, val timeRange: TimeRange): ProfileCardState()
    data class LoadedTopArtist(val timeRange: TimeRange, val artist: TopArtist): ProfileCardState()
    data class LoadedTopTrack(val timeRange: TimeRange, val track: TopTrack) : ProfileCardState()
    data class Failure(val error: String, val retry: () -> Unit): ProfileCardState()

    /**
     * Convert state into a set of necessary data for displaying the view
     */
    fun toViewData(context: Context): ProfileCardViewData {
        when (this) {
            is Loading -> {
                return ProfileCardViewData(
                    loading = true,
                    tagTitle = timeRange.toReadableString(cardType, context)
                )
            }
            is LoadedTopArtist -> {
                // If lastPlayed is null then we don't have data about how often it was played and
                // when it was played
                if (artist.lastPlayed == null) {
                    return ProfileCardViewData(
                        tagTitle = timeRange.toReadableString(CardType.TOP_ARTIST, context),
                        title = artist.name,
                        // We'll leave the strings empty if the date is null. The date will be null
                        // if this artist hasn't been played since joining Wilt
                        subtitleFirstLine = "",
                        subtitleSecondLine = ""
                    )
                }
                val lastPlayedRelative = artist.lastPlayed.toRelative()
                return ProfileCardViewData(
                    tagTitle = timeRange.toReadableString(CardType.TOP_ARTIST, context),
                    title = artist.name,
                    subtitleFirstLine = context.getString(R.string.plays_format, artist.totalPlays),
                    subtitleSecondLine = context.getString(R.string.last_listened_format, lastPlayedRelative)
                )
            }
            is LoadedTopTrack -> {
                // If lastPlayed is null then we don't have data about how often it was played and
                // when it was played
                if (track.lastPlayed == null) {
                    return ProfileCardViewData(
                        tagTitle = timeRange.toReadableString(CardType.TOP_TRACK, context),
                        title = track.name,
                        // We'll leave the strings empty if the date is null. The date will be null
                        // if this track hasn't been played since joining Wilt
                        subtitleFirstLine = "",
                        subtitleSecondLine = ""
                    )
                }
                val lastPlayedRelative = track.lastPlayed.toRelative()
                return ProfileCardViewData(
                    tagTitle = timeRange.toReadableString(CardType.TOP_TRACK, context),
                    title = track.name,
                    subtitleFirstLine = context.getString(
                        R.string.play_duration_format,
                        track.totalPlayTimeMs.toDurationFromMilliseconds()
                    ),
                    subtitleSecondLine = context.getString(R.string.last_listened_format, lastPlayedRelative)
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
 * Specify the card type. Specifically used for [toReadableString]
 */
enum class CardType {
    TOP_ARTIST, TOP_TRACK
}

/**
 * Convert the timeRange to a string to be presented to the UI
 */
private fun TimeRange.toReadableString(cardType: CardType, context: Context): String? {
    return if (cardType == CardType.TOP_ARTIST) {
        when (this) {
            is TimeRange.LongTerm -> context.getString(R.string.favourite_artist_title_long_term)
            is TimeRange.MediumTerm -> context.getString(R.string.favourite_artist_title_medium_term)
            is TimeRange.ShortTerm -> context.getString(R.string.favourite_artist_title_short_term)
        }
    } else {
        when (this) {
            is TimeRange.LongTerm -> context.getString(R.string.favourite_track_title_long_term)
            is TimeRange.MediumTerm -> context.getString(R.string.favourite_track_title_medium_term)
            is TimeRange.ShortTerm -> context.getString(R.string.favourite_track_title_short_term)
        }
    }
}

/**
 * Convert milliseconds into a duration as a human readable string
 */
@VisibleForTesting
fun Long.toDurationFromMilliseconds(): String {
    if (this < 0) return ""
    if (this == 0L) return "0 seconds"
    // Don't worry about milliseconds so we'll just remove them
    val x = this / 1000
    var divisor = 7 * 24 * 60 * 60
    // Calculate weeks
    val weeks = x / divisor
    var remainder= x % divisor
    divisor /= 7
    // Calculate days
    val days= remainder / divisor
    remainder %= divisor
    divisor /= 24
    // Calculate hours
    val hours = remainder / divisor
    remainder %= divisor
    divisor /= 60
    // Calculate minutes and seconds
    val minutes = remainder / divisor
    val seconds = remainder % divisor
    // Create string
    var result =
        if (weeks > 1) "$weeks weeks "
        else if (weeks > 0) "$weeks week "
        else "" +
        if (days > 1) "$days days "
        else if (days > 0) "$days day "
        else "" +
        if (hours > 1) "$hours hours "
        else if (hours > 0) "$hours hour "
        else "" +
        if (minutes > 1) "$minutes minutes "
        else if (minutes > 0) "$minutes minute "
        else ""
    if (seconds > 1)
        result += "$seconds seconds"
    else if (seconds > 0)
        result += "$seconds second"
    else {
        // Delete the last space if there are no seconds
        result = result.substring(0, result.length - 1)
    }
    return result
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
    val title: String? = null,
    val subtitleFirstLine: String? = null,
    val subtitleSecondLine: String? = null,
    val errorMessage: String? = null,
    val retry: (() -> Unit)? = null
)