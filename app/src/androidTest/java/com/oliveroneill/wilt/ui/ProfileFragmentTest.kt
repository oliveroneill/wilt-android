package com.oliveroneill.wilt.ui

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.*
import com.oliveroneill.wilt.Event
import com.oliveroneill.wilt.R
import com.oliveroneill.wilt.viewmodel.ProfileFragmentViewModel
import com.oliveroneill.wilt.viewmodel.ProfileNetworkState
import com.oliveroneill.wilt.viewmodel.ProfileState
import com.oliveroneill.wilt.viewmodel.TopArtist
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime


@RunWith(AndroidJUnit4::class)
class ProfileFragmentTest {
    private lateinit var scenario: FragmentScenario<ProfileFragment>
    // Create fake view model for sending events to the UI
    private val viewModel = mock<ProfileFragmentViewModel>()
    private val stateData = MutableLiveData<Event<ProfileState>>()
    // Create factory that returns the fake view model
    private val factory = mock<ViewModelProvider.AndroidViewModelFactory>()
    private val navController = mock<NavController>()
    private val currentUser = "username123"

    @Before
    fun setup() {
        whenever(factory.create(ProfileFragmentViewModel::class.java)).thenReturn(viewModel)
        whenever(viewModel.state).thenReturn(stateData)
        // Specify the fragment factory in order to set the view model factory
        scenario = launchFragmentInContainer<ProfileFragment>(
            null,
            R.style.AppTheme
        ) {
            ProfileFragment().also {
                it.viewModelFactory = factory
                it.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        // The fragment’s view has just been created
                        Navigation.setViewNavController(it.requireView(), navController)
                    }
                }
            }
        }
    }

    @Test
    fun shouldLogout() {
        // When
        stateData.postValue(Event(ProfileState.LoggedOut))
        // Then
        verify(navController, timeout(1000)).navigate(eq(ProfileFragmentDirections.logout()))
    }

    @Test
    fun shouldShowLoadingSpinner() {
        // When
        stateData.postValue(Event(ProfileState.LoggedIn(ProfileNetworkState.Loading(currentUser))))
        // Then
        onView(withText(currentUser)).check(matches(isDisplayed()))
        onView(withId(R.id.loading_spinner)).check(matches(isDisplayed()))
    }

    @Test
    fun shouldShowTopArtist() {
        val topArtist = TopArtist(
            "Death Grips",
            666,
            LocalDateTime.now().minusMonths(2)
        )
        // When
        stateData.postValue(
            Event(
                ProfileState.LoggedIn(
                    ProfileNetworkState.LoadedTopArtist(currentUser, topArtist)
                )
            )
        )
        // Then
        onView(withText(currentUser)).check(matches(isDisplayed()))
        onView(withText("Death Grips")).check(matches(isDisplayed()))
        onView(withText("666 plays since joining Wilt")).check(matches(isDisplayed()))
        onView(withText("Last listened to 2 months ago")).check(matches(isDisplayed()))
        onView(withId(R.id.loading_spinner)).check(matches(not(isDisplayed())))
    }

    @Test
    fun shouldShowTopArtistWithMissingInfo() {
        val topArtist = TopArtist(
            "Death Grips",
            666,
            null
        )
        // When
        stateData.postValue(
            Event(
                ProfileState.LoggedIn(
                    ProfileNetworkState.LoadedTopArtist(currentUser, topArtist)
                )
            )
        )
        // Then
        onView(withText(currentUser)).check(matches(isDisplayed()))
        onView(withText("Death Grips")).check(matches(isDisplayed()))
        // Make sure that the plays and last listened to are not displayed
        onView(withId(R.id.playsText)).check(matches(withText("")))
        onView(withId(R.id.lastListenText)).check(matches(withText("")))
        onView(withId(R.id.loading_spinner)).check(matches(not(isDisplayed())))
    }
}
