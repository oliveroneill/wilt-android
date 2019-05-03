package com.oliveroneill.wilt.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.*
import com.oliveroneill.wilt.data.FirebaseAuthentication
import com.oliveroneill.wilt.data.SpotifyAuthenticationResponse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt

class WalkthroughFragmentViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val application = mock<Application> {
        // Mock value so that getString doesn't throw
        on { getString(anyInt()) } doReturn ""
    }
    private lateinit var model: WalkthroughFragmentViewModel
    private lateinit var firebase: FirebaseAuthentication

    @Before
    fun setup() {
        firebase = mock()
        model = WalkthroughFragmentViewModel(application, firebase)
    }

    @Test
    fun `should set initial state to walkthrough`() {
        // Assert that state gets set correctly
        model.state
            .test()
            .assertHasValue()
            .assertValue { it.getContentIfNotHandled() is WalkthroughFragmentState.Walkthrough }
    }

    @Test
    fun `should set initial state to logged in if the user is logged in`() {
        val expected = "username123"
        whenever(firebase.currentUser).thenReturn(expected)
        model = WalkthroughFragmentViewModel(application, firebase)
        // Assert that state gets set correctly
        model.state
            .test()
            .assertHasValue()
            .assertValue {
                val state = it.getContentIfNotHandled()
                when(state) {
                    is WalkthroughFragmentState.LoggedIn -> state.username == expected
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
            .assertValue { it.getContentIfNotHandled() is WalkthroughFragmentState.AuthenticatingSpotify }
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
                val state = it.getContentIfNotHandled()
                when(state) {
                    is WalkthroughFragmentState.LoggedIn -> state.username == expected
                    else -> false
                }
            }
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
                val state = it.getContentIfNotHandled()
                when(state) {
                    is WalkthroughFragmentState.LoginError -> state.error == expected
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
                val state = it.getContentIfNotHandled()
                when(state) {
                    is WalkthroughFragmentState.LoginError -> state.error == expected
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
                val state = it.getContentIfNotHandled()
                when(state) {
                    is WalkthroughFragmentState.LoginError -> state.error == expected
                    else -> false
                }
            }
    }
}