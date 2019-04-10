package com.oliveroneill.wilt

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.oliveroneill.wilt.walkthrough.LoginErrorFragment
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginErrorFragmentTest {
    lateinit var scenario: FragmentScenario<LoginErrorFragment>

    @Before
    fun setup() {
        scenario = launchFragmentInContainer<LoginErrorFragment>()
    }

    @Test
    fun shouldDisplayMessage() {
        // Seems like a useless test...
        onView(withId(R.id.textView)).check(matches(withText("Sorry, the Spotify login failed.")))
    }

}