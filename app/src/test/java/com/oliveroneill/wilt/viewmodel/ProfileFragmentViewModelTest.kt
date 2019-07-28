package com.oliveroneill.wilt.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.firebase.functions.FirebaseFunctionsException
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.*
import com.oliveroneill.wilt.Event
import com.oliveroneill.wilt.R
import com.oliveroneill.wilt.data.FirebaseAPI
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
    private val application = mock<Application> {
        // Mock value so that getString doesn't throw
        on { getString(ArgumentMatchers.anyInt()) } doReturn ""
    }
    private lateinit var firebase: FirebaseAPI

    @Before
    fun setup() {
        firebase = mock()
        whenever(firebase.currentUser).thenReturn(currentUser)
        whenever(
            application.getString(eq(R.string.favourite_artist_title))
        ).thenReturn("Your favourite artist")
    }

    /**
     * Helper function to unwrap the event and the state. This will return null if there's no value or
     * you're not logged in
     */
    private fun Event<ProfileState>.unwrapState(): ProfileLoggedInState? {
        val state = getContentIfNotHandled()
        if (state is ProfileState.LoggedIn) return state.state
        TestCase.fail()
        return null
    }

    @Test
    fun `should set initial state to loading`() {
        val model = ProfileFragmentViewModel(application, firebase)
        // Assert that state gets set correctly
        model.state
            .test()
            .assertHasValue()
            .assertValue {
                val state = it.unwrapState()
                state is ProfileLoggedInState && state.profileName == currentUser
                        && state.cards == listOf(ProfileCardState.Loading)
            }
    }

    @Test
    fun `should set state to logged out when not logged in`() {
        whenever(firebase.currentUser).thenReturn(null)
        val model = ProfileFragmentViewModel(application, firebase)
        model.state
            .test()
            .assertHasValue()
            .assertValue { it.getContentIfNotHandled() is ProfileState.LoggedOut }
    }

    @Test
    fun `should send top artist data`() {
        val expected = TopArtist("Death Grips", 666, LocalDateTime.now())
        whenever(firebase.topArtist(any())).then {
            (it.getArgument(0) as (Result<TopArtist>) -> Unit).invoke(Result.success(expected))
        }
        val model = ProfileFragmentViewModel(application, firebase)
        model.state
            .test()
            .assertHasValue()
            .assertValue {
                val state = it.unwrapState()
                state is ProfileLoggedInState &&
                        state.profileName == currentUser &&
                        state.cards == listOf(ProfileCardState.LoadedTopArtist(expected))
            }
    }

    @Test
    fun `should log out on unauthenticated error`() {
        val error = mock<FirebaseFunctionsException>()
        whenever(error.code).thenReturn(FirebaseFunctionsException.Code.UNAUTHENTICATED)
        whenever(firebase.topArtist(any())).then {
            (it.getArgument(0) as (Result<TopArtist>) -> Unit).invoke(Result.failure(error))
        }
        val model = ProfileFragmentViewModel(application, firebase)
        model.state
            .test()
            .assertHasValue()
            .assertValue {
                it.getContentIfNotHandled() is ProfileState.LoggedOut
            }
    }

    @Test
    fun `should send error message on error`() {
        val expected = "A test error for unit tests"
        val error = IOException(expected)
        whenever(firebase.topArtist(any())).then {
            (it.getArgument(0) as (Result<TopArtist>) -> Unit).invoke(Result.failure(error))
        }
        val model = ProfileFragmentViewModel(application, firebase)
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
        whenever(firebase.topArtist(any())).then {
            (it.getArgument(0) as (Result<TopArtist>) -> Unit).invoke(Result.failure(error))
        }
        val model = ProfileFragmentViewModel(application, firebase)
        val state = model.state.value?.unwrapState()
        when (state) {
            is ProfileLoggedInState -> {
                assertEquals(1, state.cards.size)
                val card = state.cards[0]
                when (card) {
                    is ProfileCardState.Failure -> {
                        card.retry()
                        // Ensure that it makes the correct call. This will be the second call
                        verify(firebase, times(2)).topArtist(any())
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
        val state = ProfileCardState.Loading
        val expected = ProfileCardViewData(
            loading = true,
            tagTitle = "Your favourite artist"
        )
        assertEquals(expected, state.toViewData(application))
    }

    @Test
    fun `should convert view data correctly when loaded`() {
        val topArtist = TopArtist("Death Grips", 666, LocalDateTime.now())
        val state = ProfileCardState.LoadedTopArtist(topArtist)
        val expected = ProfileCardViewData(
            artistName = "Death Grips",
            playText = "666 plays",
            lastListenedText = "Last listened to 10 days ago",
            tagTitle = "Your favourite artist"
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
        val topArtist = TopArtist("Death Grips", 666, null)
        val state = ProfileCardState.LoadedTopArtist(topArtist)
        val expected = ProfileCardViewData(
            artistName = "Death Grips",
            playText = "",
            lastListenedText = "",
            tagTitle = "Your favourite artist"
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
}
