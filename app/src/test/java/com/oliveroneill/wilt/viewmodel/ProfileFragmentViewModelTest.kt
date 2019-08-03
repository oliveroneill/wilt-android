package com.oliveroneill.wilt.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.firebase.functions.FirebaseFunctionsException
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.*
import com.oliveroneill.wilt.Message
import com.oliveroneill.wilt.R
import com.oliveroneill.wilt.data.FirebaseAPI
import com.oliveroneill.wilt.data.ProfileRepository
import com.oliveroneill.wilt.data.TimeRange
import junit.framework.TestCase
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyString
import java.io.IOException
import java.time.LocalDateTime

class ProfileFragmentViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val currentUser = "username123"
    private val timeRange = TimeRange.LongTerm
    private val index = 0
    private val cards = listOf<Card>(
        Card.TopArtistCard(index, timeRange)
    )
    private val application = mock<Application> {
        // Mock value so that getString doesn't throw
        on { getString(ArgumentMatchers.anyInt()) } doReturn ""
    }
    private lateinit var repository: ProfileRepository
    private lateinit var firebase: FirebaseAPI

    @Before
    fun setup() {
        repository = mock()
        firebase = mock()
        whenever(repository.currentUser).thenReturn(currentUser)
        whenever(
            application.getString(eq(R.string.favourite_artist_title_long_term))
        ).thenReturn("Your favourite artist ever")
    }

    /**
     * Helper function to unwrap the message and the state. This will return null if there's no value or
     * you're not logged in
     */
    private fun Message<ProfileState>.unwrapState(): ProfileLoggedInState? {
        val state = getContent()
        if (state is ProfileState.LoggedIn) return state.state
        TestCase.fail()
        return null
    }

    @Test
    fun `should set initial state to loading`() {
        val model = ProfileFragmentViewModel(application, firebase, repository, cards)
        // Assert that state gets set correctly
        model.state
            .test()
            .assertHasValue()
            .assertValue {
                val state = it.unwrapState()
                state is ProfileLoggedInState && state.profileName == currentUser
                        && state.cards == listOf(ProfileCardState.Loading(CardType.TOP_ARTIST, timeRange))
            }
    }

    @Test
    fun `should set state to logged out when not logged in`() {
        whenever(repository.currentUser).thenReturn(null)
        val model = ProfileFragmentViewModel(application, firebase, repository, cards)
        model.state
            .test()
            .assertHasValue()
            .assertValue { it.getContent() is ProfileState.LoggedOut }
    }

    @Test
    fun `should send top artist data`() {
        val expected = TopArtist(
            "Death Grips",
            666,
            LocalDateTime.now(),
            "http://notarealurl.com/album_img.png"
        )
        whenever(repository.topArtist(eq(timeRange), eq(index), any())).then {
            (it.getArgument(2) as (Result<TopArtist>) -> Unit).invoke(Result.success(expected))
        }
        val model = ProfileFragmentViewModel(application, firebase, repository, cards)
        model.state
            .test()
            .assertHasValue()
            .assertValue {
                val state = it.unwrapState()
                state is ProfileLoggedInState &&
                        state.profileName == currentUser &&
                        state.cards == listOf(ProfileCardState.LoadedTopArtist(timeRange, expected))
            }
    }

    @Test
    fun `should log out on unauthenticated error`() {
        val error = mock<FirebaseFunctionsException>()
        whenever(error.code).thenReturn(FirebaseFunctionsException.Code.UNAUTHENTICATED)
        whenever(repository.topArtist(eq(timeRange), eq(index), any())).then {
            (it.getArgument(2) as (Result<TopArtist>) -> Unit).invoke(Result.failure(error))
        }
        val model = ProfileFragmentViewModel(application, firebase, repository, cards)
        model.state
            .test()
            .assertHasValue()
            .assertValue {
                it.getContent() is ProfileState.LoggedOut
            }
    }

    @Test
    fun `should send error message on error`() {
        val expected = "A test error for unit tests"
        val error = IOException(expected)
        whenever(repository.topArtist(eq(timeRange), eq(index), any())).then {
            (it.getArgument(2) as (Result<TopArtist>) -> Unit).invoke(Result.failure(error))
        }
        val model = ProfileFragmentViewModel(application, firebase, repository, cards)
        model.state
            .test()
            .assertHasValue()
            .assertValue {
                val state = it.unwrapState()
                state is ProfileLoggedInState &&
                        state.profileName == currentUser &&
                        state.cards.size == 1 && state.cards[0] is ProfileCardState.Failure &&
                        (state.cards[0] as ProfileCardState.Failure).error == expected
            }
    }

    @Test
    fun `should set retry correctly`() {
        val expected = "A test error for unit tests"
        val error = IOException(expected)
        whenever(repository.topArtist(eq(timeRange), eq(index), any())).then {
            (it.getArgument(2) as (Result<TopArtist>) -> Unit).invoke(Result.failure(error))
        }
        val model = ProfileFragmentViewModel(application, firebase, repository, cards)
        val state = model.state.value?.unwrapState()
        when (state) {
            is ProfileLoggedInState -> {
                assertEquals(1, state.cards.size)
                val card = state.cards[0]
                when (card) {
                    is ProfileCardState.Failure -> {
                        card.retry()
                        // Ensure that it makes the correct call. This will be the second call
                        verify(repository, times(2)).topArtist(eq(timeRange), eq(index), any())
                    }
                    else -> {
                        fail()
                    }
                }
            }
            else -> {
                fail()
            }
        }
    }

    @Test
    fun `should convert view data correctly when loading`() {
        val state = ProfileCardState.Loading(CardType.TOP_ARTIST, timeRange)
        val expected = ProfileCardViewData(
            loading = true,
            tagTitle = "Your favourite artist ever"
        )
        assertEquals(expected, state.toViewData(application))
    }

    @Test
    fun `should convert view data correctly when loaded`() {
        val topArtist = TopArtist(
            "Death Grips",
            666,
            LocalDateTime.now(),
            "http://notarealurl.com/album_img.png"
        )
        val state = ProfileCardState.LoadedTopArtist(timeRange, topArtist)
        val expected = ProfileCardViewData(
            title = "Death Grips",
            subtitleFirstLine = "666 plays",
            subtitleSecondLine = "Last listened to 10 days ago",
            tagTitle = "Your favourite artist ever",
            imageUrl = "http://notarealurl.com/album_img.png"
        )
        whenever(
            application.getString(eq(R.string.plays_format), eq(666))
        ).thenReturn("666 plays")
        whenever(
            application.getString(eq(R.string.last_listened_format), anyString())
        ).thenReturn("Last listened to 10 days ago")
        assertEquals(expected, state.toViewData(application))
    }

    @Test
    fun `should convert view data correctly when loaded with null date`() {
        val topArtist = TopArtist(
            "Death Grips",
            666,
            null,
            "http://notarealurl.com/album_img.png"
        )
        val state = ProfileCardState.LoadedTopArtist(timeRange, topArtist)
        val expected = ProfileCardViewData(
            title = "Death Grips",
            subtitleFirstLine = "",
            subtitleSecondLine = "",
            tagTitle = "Your favourite artist ever",
            imageUrl = "http://notarealurl.com/album_img.png"
        )
        assertEquals(expected, state.toViewData(application))
    }

    @Test
    fun `should convert error correctly`() {
        val retryMock = mock<() -> Unit>()
        val state = ProfileCardState.Failure(
            "A test error for unit tests", retryMock
        )
        val expected = ProfileCardViewData(
            errorMessage = "A test error for unit tests",
            retry = retryMock
        )
        assertEquals(expected, state.toViewData(application))
    }

    @Test
    fun `should send top track data`() {
        val expected = TopTrack(
            "On GP by Death Grips",
            10_000,
            LocalDateTime.now(),
            "http://notarealurl.com/album_img.png"
        )
        whenever(repository.topTrack(eq(timeRange), eq(index), any())).then {
            (it.getArgument(2) as (Result<TopTrack>) -> Unit).invoke(Result.success(expected))
        }
        // Top track card
        val cards = listOf<Card>(
            Card.TopTrackCard(index, timeRange)
        )
        val model = ProfileFragmentViewModel(application, firebase, repository, cards)
        model.state
            .test()
            .assertHasValue()
            .assertValue {
                val state = it.unwrapState()
                state is ProfileLoggedInState &&
                        state.profileName == currentUser &&
                        state.cards == listOf(ProfileCardState.LoadedTopTrack(timeRange, expected))
            }
    }

    @Test
    fun `should convert track data correctly when loaded`() {
        val topArtist = TopTrack(
            "On GP by Death Grips",
            10_000,
            LocalDateTime.now(),
            "http://notarealurl.com/album_img.png"
        )
        val state = ProfileCardState.LoadedTopTrack(timeRange, topArtist)
        val expected = ProfileCardViewData(
            title = "On GP by Death Grips",
            subtitleFirstLine = "10 seconds played",
            subtitleSecondLine = "Last listened to 10 days ago",
            tagTitle = "Your favourite song ever",
            imageUrl = "http://notarealurl.com/album_img.png"
        )
        whenever(
            application.getString(eq(R.string.play_duration_format), eq("10 seconds"))
        ).thenReturn("10 seconds played")
        whenever(
            application.getString(eq(R.string.last_listened_format), anyString())
        ).thenReturn("Last listened to 10 days ago")
        whenever(
            application.getString(eq(R.string.favourite_track_title_long_term))
        ).thenReturn("Your favourite song ever")
        assertEquals(expected, state.toViewData(application))
    }


    @Test
    fun `should logout`() {
        val model = ProfileFragmentViewModel(application, firebase, repository, cards)
        model.logout()
        verify(firebase).logout()
        model.state
            .test()
            .assertHasValue()
            .assertValue { it.getContent() is ProfileState.LoggedOut }
    }
}
