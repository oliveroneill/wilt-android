package com.oliveroneill.wilt.ui.walkthrough

import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.*
import com.oliveroneill.wilt.Event
import com.oliveroneill.wilt.R
import com.oliveroneill.wilt.data.SpotifyAuthenticationRequest
import com.oliveroneill.wilt.viewmodel.WalkthroughFragmentState
import com.oliveroneill.wilt.viewmodel.WalkthroughFragmentViewModel
import com.oliveroneill.wilt.viewmodel.WalkthroughPage
import org.hamcrest.CoreMatchers.allOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WalkthroughFragmentTest {
    private lateinit var scenario: FragmentScenario<WalkthroughFragment>
    // Create fake view model for sending events to the UI
    private val viewModel = mock<WalkthroughFragmentViewModel>()
    private val stateData = MutableLiveData<Event<WalkthroughFragmentState>>()
    // Create factory that returns the fake view model
    private val factory = mock<ViewModelProvider.AndroidViewModelFactory>()
    private val navController = mock<NavController>()
    /**
     * These pages are exactly the same as the ones in the real view model. I'm not sure whether this is
     * the best idea because this wouldn't catch an edge case like the view not using the viewmodel's
     * pages. On the other hand, it does mean that our tests ensure the text it shows is exactly
     * what I'd expect from the view as opposed to some other text that means nothing
     */
    val pages = listOf(
        WalkthroughPage(title = "Welcome to page 1, yeah!", imageResID = R.drawable.walkthrough1),
        WalkthroughPage(title = "Welcome to the second page!!", imageResID = R.drawable.walkthrough2)
    )

    @Before
    fun setup() {
        whenever(factory.create(WalkthroughFragmentViewModel::class.java)).thenReturn(viewModel)
        whenever(viewModel.state).thenReturn(stateData)
        whenever(viewModel.pages).thenReturn(pages)
        // Specify the fragment factory in order to set the view model factory
        scenario = launchFragmentInContainer<WalkthroughFragment>(
            null,
            R.style.AppTheme,
            object : FragmentFactory() {
                @Suppress("UNCHECKED_CAST")
                override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
                    return (super.instantiate(classLoader, className) as WalkthroughFragment).also {
                        it.viewModelFactory = factory
                    }
                }
            }
        )
        scenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
        }
    }

    @Test
    fun shouldDisplayWalkthrough() {
        onView(
            allOf(
                withId(R.id.textView),
                withText(pages[0].title)
            )
        ).check(matches(isDisplayed()))
    }

    @Test
    fun shouldDisplaySecondPageOnSwipe() {
        // Swipe left
        onView(withId(R.id.viewPager)).perform(swipeLeft())
        // Ensure the second page is displayed
        onView(
            allOf(
                withId(R.id.textView),
                withText(pages[1].title)
            )
        ).check(matches(isDisplayed()))
    }

    @Test
    fun shouldDisplayFirstPageWhenSwipingLeftThenRight() {
        // Swipe left
        onView(withId(R.id.viewPager)).perform(swipeLeft())
        // Swipe back
        onView(withId(R.id.viewPager)).perform(swipeRight())
        // Ensure the second page is displayed
        onView(
            allOf(
                withId(R.id.textView),
                withText(pages[0].title)
            )
        ).check(matches(isDisplayed()))
    }

    @Test
    fun shouldOpenSpotifyLoginEvent() {
        // Set login state
        val request = SpotifyAuthenticationRequest("", "x", emptyArray())
        stateData.postValue(Event(WalkthroughFragmentState.AuthenticatingSpotify(request)))
        // Ensure that the walkthrough screen is hidden since the Spotify login screen should open
        onView(withId(R.id.textView)).check(doesNotExist())
        // Not sure whether there's something else to assert here since it's not my view that's opening...
    }

    @Test
    fun shouldShowLoginError() {
        // Set error state
        stateData.postValue(Event(WalkthroughFragmentState.LoginError("Something bad happened")))
        verify(navController, timeout(1000)).navigate(eq(WalkthroughFragmentDirections.showLoginError()))
    }

    @Test
    fun shouldShowPlayHistory() {
        stateData.postValue(Event(WalkthroughFragmentState.LoggedIn("Code_Example")))
        verify(navController, timeout(1000)).navigate(eq(WalkthroughFragmentDirections.showPlayHistory()))
    }

    @Test
    fun shouldOpenSpotifyLoginOnClick() {
        onView(withId(R.id.signInButton)).perform(click())
        verify(viewModel).spotifySignup()
    }

    @Test
    fun shouldNavigateToInfoScreen() {
        val item = mock<MenuItem> {
            on { itemId } doReturn R.id.action_info
        }
        scenario.onFragment { fragment ->
            fragment.onOptionsItemSelected(item)
        }
        verify(navController, timeout(1000)).navigate(eq(WalkthroughFragmentDirections.showInfo()))
    }

    // TODO: test that loading spinner is displayed - this isn't easy to do since the Spotify login page opens on top
}