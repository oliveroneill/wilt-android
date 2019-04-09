package com.oliveroneill.wilt

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.oliveroneill.wilt.walkthrough.SpotifyAuthenticationRequest
import com.oliveroneill.wilt.walkthrough.WalkthroughFragment
import com.oliveroneill.wilt.walkthrough.WalkthroughFragmentState
import com.oliveroneill.wilt.walkthrough.WalkthroughFragmentViewModel
import org.hamcrest.CoreMatchers.allOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class WalkthroughFragmentTest {
    // Create fake view model for sending events to the UI
    val model = mock(WalkthroughFragmentViewModel::class.java)
    val stateData = MutableLiveData<Event<WalkthroughFragmentState>>()
    // Create factory that returns the fake view model
    val factory = mock(ViewModelProvider.AndroidViewModelFactory::class.java)

    @Before
    fun setup() {
        `when`(factory.create(WalkthroughFragmentViewModel::class.java)).thenReturn(model)
        `when`(model.state).thenReturn(stateData)
    }

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
        // Set login state
        val request = SpotifyAuthenticationRequest("", "x", emptyArray())
        stateData.postValue(Event(WalkthroughFragmentState.LoggingIn(request)))
        // Specify the fragment factory in order to set the view model factory
        launchFragmentInContainer<WalkthroughFragment>(
            null,
            R.style.AppTheme,
            object : FragmentFactory() {
                @Suppress("UNCHECKED_CAST")
                override fun instantiate(classLoader: ClassLoader, className: String, args: Bundle?): Fragment {
                    return (super.instantiate(classLoader, className, args) as WalkthroughFragment).also {
                        it.viewModelFactory = factory
                    }
                }
            }
        )
        // Ensure that the walkthrough screen is hidden since the Spotify login screen should open
        onView(withId(R.id.textView)).check(doesNotExist())
        // Not sure whether there's something else to assert here since it's not my view that's opening...
    }

    @Test
    fun shouldShowLoginError() {
        // TODO
    }
}