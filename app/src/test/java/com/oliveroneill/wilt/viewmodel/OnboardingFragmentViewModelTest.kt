package com.oliveroneill.wilt.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.*
import com.oliveroneill.wilt.data.ArtistRankBoundaryCallbackTest
import com.oliveroneill.wilt.data.FirebaseAuthentication
import com.oliveroneill.wilt.data.SpotifyAuthenticationResponse
import com.oliveroneill.wilt.data.dao.PlayHistoryDao
import com.oliveroneill.wilt.data.dao.TopArtistDao
import com.oliveroneill.wilt.data.dao.TopTrackDao
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt

class OnboardingFragmentViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val application = mock<Application> {
        // Mock value so that getString doesn't throw
        on { getString(anyInt()) } doReturn ""
    }
    private lateinit var model: OnboardingFragmentViewModel
    private lateinit var firebase: FirebaseAuthentication
    private lateinit var feedDao: PlayHistoryDao
    private lateinit var trackCache: TopTrackDao
    private lateinit var artistCache: TopArtistDao

    @Before
    fun setup() {
        firebase = mock()
        feedDao = mock()
        trackCache = mock()
        artistCache = mock()
        model = OnboardingFragmentViewModel(
            application, firebase,
            feedDao = feedDao, artistCache = artistCache, trackCache = trackCache,
            // Run login tasks on current thread
            executor = ArtistRankBoundaryCallbackTest.CurrentThreadExecutor()
        )
    }

    @Test
    fun `should set initial state to walkthrough`() {
        // Assert that state gets set correctly
        model.state
            .test()
            .assertHasValue()
            .assertValue { it.getContent() is OnboardingFragmentState.Onboarding }
    }

    @Test
    fun `should set initial state to logged in if the user is logged in`() {
        val expected = "username123"
        whenever(firebase.currentUser).thenReturn(expected)
        model = OnboardingFragmentViewModel(application, firebase, feedDao, artistCache, trackCache)
        // Assert that state gets set correctly
        model.state
            .test()
            .assertHasValue()
            .assertValue {
                val state = it.getContent()
                when(state) {
                    is OnboardingFragmentState.LoggedIn -> state.username == expected
                    else -> false
                }
            }
    }

    @Test
    fun `should start login request when starting spotify login`() {
        // Start login process
        model.spotifySignup()
        // Assert that state gets set correctly
        model.state
            .test()
            .assertHasValue()
            .assertValue { it.getContent() is OnboardingFragmentState.AuthenticatingSpotify }
    }

    @Test
    fun `should successfully login when response is successful`() {
        // Send login response
        val expected = "username123"
        val token = "43543"
        val spotifyAuthCode = "542781"
        whenever(firebase.signUp(eq(spotifyAuthCode), any(), any())).then {
            it.getArgument<(Result<String>) -> Unit>(2)(Result.success(token))
        }
        whenever(firebase.login(eq(token), any())).then {
            it.getArgument<(Result<String>) -> Unit>(1)(Result.success(expected))
        }
        model.onSpotifyLoginResponse(SpotifyAuthenticationResponse.Success(spotifyAuthCode))
        // Assert that state gets set correctly
        model.state
            .test()
            .assertHasValue()
            .assertValue {
                val state = it.getContent()
                when(state) {
                    is OnboardingFragmentState.LoggedIn -> state.username == expected
                    else -> false
                }
            }
    }

    @Test
    fun `should clear cache when signing in`() {
        val spotifyAuthCode = "542781"
        model.onSpotifyLoginResponse(SpotifyAuthenticationResponse.Success(spotifyAuthCode))
        verify(feedDao).deleteAll()
        verify(trackCache).deleteAll()
        verify(artistCache).deleteAll()
    }

    @Test
    fun `should fail to login when response is an error`() {
        // Send login response
        val expected = "Something went wrong while logging in"
        model.onSpotifyLoginResponse(SpotifyAuthenticationResponse.Failure(expected))
        // Assert that state gets set correctly
        model.state
            .test()
            .assertHasValue()
            .assertValue {
                val state = it.getContent()
                when(state) {
                    is OnboardingFragmentState.LoginError -> state.error == expected
                    else -> false
                }
            }
    }

    @Test
    fun `should fail to login when the firebase function fails`() {
        // Send login response
        val spotifyAuthCode = "542781"
        val expected = "Firebase signUp failed"
        whenever(firebase.signUp(eq(spotifyAuthCode), any(), any())).then {
            // Throw an error when attempting to sign up
            it.getArgument<(Result<String>) -> Unit>(2)(Result.failure(Exception()))
        }
        model.onSpotifyLoginResponse(SpotifyAuthenticationResponse.Success(spotifyAuthCode))
        // Assert that state gets set correctly
        model.state
            .test()
            .assertHasValue()
            .assertValue {
                val state = it.getContent()
                when(state) {
                    is OnboardingFragmentState.LoginError -> state.error == expected
                    else -> false
                }
            }
    }

    @Test
    fun `should fail to login when the firebase auth fails`() {
        // Send login response
        val expected = "Firebase auth failed"
        val token = "43543"
        val spotifyAuthCode = "542781"
        whenever(firebase.signUp(eq(spotifyAuthCode), any(), any())).then {
            it.getArgument<(Result<String>) -> Unit>(2)(Result.success(token))
        }
        whenever(firebase.login(eq(token), any())).then {
            it.getArgument<(Result<String>) -> Unit>(1)(Result.failure(Exception()))
        }
        model.onSpotifyLoginResponse(SpotifyAuthenticationResponse.Success(spotifyAuthCode))
        // Assert that state gets set correctly
        model.state
            .test()
            .assertHasValue()
            .assertValue {
                val state = it.getContent()
                when(state) {
                    is OnboardingFragmentState.LoginError -> state.error == expected
                    else -> false
                }
            }
    }
}