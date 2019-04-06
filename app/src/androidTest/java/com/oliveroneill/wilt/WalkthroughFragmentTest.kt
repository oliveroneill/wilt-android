package com.oliveroneill.wilt

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.oliveroneill.wilt.walkthrough.WalkthroughFragment
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WalkthroughFragmentTest {
    @Test
    fun shouldDisplayWalkthrough() {
        launchFragmentInContainer<WalkthroughFragment>()
        onView(
            allOf(
                withId(R.id.textView),
                withText("Welcome to Wilt. We\'ll keep track of what you listen to.")
            )
        ).check(matches(isDisplayed()))
    }

    @Test
    fun shouldOpenSpotifyLoginOnClick() {
        // TODO
    }

    @Test
    fun shouldShowLoginError() {
        // TODO
    }
}