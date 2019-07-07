package com.oliveroneill.wilt.ui

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.oliveroneill.wilt.R
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
class LoginErrorFragmentTest {
    private lateinit var scenario: FragmentScenario<LoginErrorFragment>
    private val navController = mock(NavController::class.java)

    @Before
    fun setup() {
        scenario = launchFragmentInContainer<LoginErrorFragment>()
        scenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
        }
    }

    @Test
    fun shouldDisplayMessage() {
        // Seems like a useless test...
        onView(withId(R.id.textView)).check(matches(withText("Sorry, the Spotify login failed.")))
    }

    @Test
    fun shouldGoBackOnTryAgainClick() {
        onView(withId(R.id.tryAgainButton)).perform(ViewActions.click())
        verify(navController).popBackStack()
    }
}