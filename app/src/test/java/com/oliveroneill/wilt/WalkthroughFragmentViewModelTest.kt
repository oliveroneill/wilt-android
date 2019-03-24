package com.oliveroneill.wilt

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.jraska.livedata.test
import com.oliveroneill.wilt.walkthrough.SpotifyAuthenticationResponse
import com.oliveroneill.wilt.walkthrough.WalkthroughFragmentState
import com.oliveroneill.wilt.walkthrough.WalkthroughFragmentViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock


class WalkthroughFragmentViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var model: WalkthroughFragmentViewModel

    @Before
    fun setup() {
        val application = mock(Application::class.java)
        // Mock value so that getString doesn't throw
        `when`(application.getString(anyInt())).thenReturn("")
        model = WalkthroughFragmentViewModel(application)
    }

    @Test
    fun `should start login request when starting spotify login`() {
        // Start login process
        model.spotifySignup()
        // Assert that state gets set correctly
        model.state
            .test()
            .assertHasValue()
            .assertValue { it is WalkthroughFragmentState.LoggingIn }
    }

    @Test
    fun `should successfully login when response is successful`() {
        // Send login response
        val expected = "542781"
        model.onSpotifyLoginResponse(SpotifyAuthenticationResponse.Success(expected))
        // Assert that state gets set correctly
        model.state
            .test()
            .assertHasValue()
            .assertValue {
                when(it) {
                    is WalkthroughFragmentState.LoggedIn -> it.code.equals(expected)
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
                when(it) {
                    is WalkthroughFragmentState.LoginError -> it.error.equals(expected)
                    else -> false
                }
            }
    }
}