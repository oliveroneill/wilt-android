package com.oliveroneill.wilt.ui.profile

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.*
import com.oliveroneill.wilt.Event
import com.oliveroneill.wilt.R
import com.oliveroneill.wilt.data.TimeRange
import com.oliveroneill.wilt.viewmodel.*
import org.hamcrest.Matchers.allOf
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
        stateData.postValue(
            Event(
                ProfileState.LoggedIn(
                    ProfileLoggedInState(
                        currentUser,
                        listOf(ProfileCardState.Loading(CardType.TOP_ARTIST, TimeRange.LongTerm))
                    )
                )
            )
        )
        // Then
        onView(withText(currentUser)).check(matches(isDisplayed()))
        // The check is redundant here but this is the best way to check the view exists
        onView(allOf(withId(R.id.shimmer), isDisplayed())).check(matches(isDisplayed()))
        onView(allOf(withText("Your favourite artist ever"), isDisplayed())).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.titleText), isDisplayed())).check(doesNotExist())
    }

    @Test
    fun shouldShowLoadingSpinnerForTopTrack() {
        // When
        stateData.postValue(
            Event(
                ProfileState.LoggedIn(
                    ProfileLoggedInState(
                        currentUser,
                        listOf(ProfileCardState.Loading(CardType.TOP_TRACK, TimeRange.LongTerm))
                    )
                )
            )
        )
        // Then
        onView(allOf(withText("Your favourite song ever"), isDisplayed())).check(matches(isDisplayed()))
    }

    @Test
    fun shouldShowTopArtist() {
        val topArtist = TopArtist(
            "Death Grips",
            666,
            LocalDateTime.now().minusMonths(2),
            "notarealimageurl"
        )
        // When
        stateData.postValue(
            Event(
                ProfileState.LoggedIn(
                    ProfileLoggedInState(
                        currentUser,
                        listOf(ProfileCardState.LoadedTopArtist(TimeRange.LongTerm, topArtist))
                    )
                )
            )
        )
        // Then
        onView(withText(currentUser)).check(matches(isDisplayed()))
        onView(withText("Death Grips")).check(matches(isDisplayed()))
        onView(withText("666 plays since joining Wilt")).check(matches(isDisplayed()))
        onView(withText("Last listened to 2 months ago")).check(matches(isDisplayed()))
        // The check is redundant here but this is the best way to check the view exists
        onView(allOf(withText("Your favourite artist ever"), isDisplayed())).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.shimmer), isDisplayed())).check(doesNotExist())
    }

    @Test
    fun shouldShowTopArtistWithMissingInfo() {
        val topArtist = TopArtist(
            "Death Grips",
            666,
            null,
            "notarealimageurl"
        )
        // When
        stateData.postValue(
            Event(
                ProfileState.LoggedIn(
                    ProfileLoggedInState(
                        currentUser,
                        listOf(ProfileCardState.LoadedTopArtist(TimeRange.LongTerm, topArtist))
                    )
                )
            )
        )
        // Then
        onView(withText(currentUser)).check(matches(isDisplayed()))
        onView(withText("Death Grips")).check(matches(isDisplayed()))
        // The check is redundant here but this is the best way to check the view exists
        onView(allOf(withText("Your favourite artist ever"), isDisplayed())).check(matches(isDisplayed()))
        // Make sure that the plays and last listened to are not displayed
        onView(allOf(withId(R.id.subtitle1Text), not(withText("")))).check(doesNotExist())
        onView(allOf(withId(R.id.subtitle2Text), not(withText("")))).check(doesNotExist())
        onView(allOf(withId(R.id.shimmer), isDisplayed())).check(doesNotExist())
    }

    @Test
    fun shouldShowError() {
        val errorMessage = "Hi this is an error message for tests"
        // When
        stateData.postValue(
            Event(
                ProfileState.LoggedIn(
                    ProfileLoggedInState(
                        currentUser,
                        listOf(ProfileCardState.Failure(errorMessage, mock()))
                    )
                )
            )
        )
        // Then
        onView(withText(currentUser)).check(matches(isDisplayed()))
        onView(withText(errorMessage)).check(matches(isDisplayed()))
        onView(withId(R.id.retry_button)).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.titleText), isDisplayed())).check(doesNotExist())
        onView(allOf(withId(R.id.shimmer), isDisplayed())).check(doesNotExist())
    }

    @Test
    fun shouldRetryOnClick() {
        val errorMessage = "Hi this is an error message for tests"
        val retry = mock<() -> Unit>()
        // When
        stateData.postValue(
            Event(
                ProfileState.LoggedIn(
                    ProfileLoggedInState(
                        currentUser,
                        listOf(ProfileCardState.Failure(errorMessage, retry))
                    )
                )
            )
        )
        onView(withId(R.id.retry_button)).perform(click())
        // Then
        verify(retry).invoke()
    }

    @Test
    fun shouldShowMultipleArtists() {
        val topArtist = TopArtist(
            "Death Grips",
            666,
            LocalDateTime.now().minusMonths(2),
            "notarealimageurl"
        )
        val topArtistRecently = TopArtist(
            "(Sandy) Alex G",
            22,
            LocalDateTime.now().minusMonths(4),
            "notarealimageurl"
        )
        // When
        stateData.postValue(
            Event(
                ProfileState.LoggedIn(
                    ProfileLoggedInState(
                        currentUser,
                        listOf(
                            ProfileCardState.LoadedTopArtist(TimeRange.LongTerm, topArtist),
                            ProfileCardState.LoadedTopArtist(TimeRange.ShortTerm, topArtistRecently)
                        )
                    )
                )
            )
        )
        // Then
        onView(withText(currentUser)).check(matches(isDisplayed()))
        // First artist
        onView(withText("Death Grips")).check(matches(isDisplayed()))
        onView(withText("666 plays since joining Wilt")).check(matches(isDisplayed()))
        onView(withText("Last listened to 2 months ago")).check(matches(isDisplayed()))
        // The check is redundant here but this is the best way to check the view exists
        onView(allOf(withText("Your favourite artist ever"), isDisplayed())).check(matches(isDisplayed()))
        // Second artist
        onView(withText("(Sandy) Alex G")).check(matches(isDisplayed()))
        onView(withText("22 plays since joining Wilt")).check(matches(isDisplayed()))
        onView(withText("Last listened to 4 months ago")).check(matches(isDisplayed()))
        onView(allOf(withText("Your favourite artist recently"), isDisplayed())).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.shimmer), isDisplayed())).check(doesNotExist())
    }

    @Test
    fun shouldShowTopTrack() {
        val topArtist = TopTrack(
            "On GP by Death Grips",
            10_000,
            LocalDateTime.now().minusMonths(2),
            "notarealimageurl"
        )
        // When
        stateData.postValue(
            Event(
                ProfileState.LoggedIn(
                    ProfileLoggedInState(
                        currentUser,
                        listOf(ProfileCardState.LoadedTopTrack(TimeRange.LongTerm, topArtist))
                    )
                )
            )
        )
        // Then
        onView(withText(currentUser)).check(matches(isDisplayed()))
        onView(withText("On GP by Death Grips")).check(matches(isDisplayed()))
        onView(withText("10 seconds spent listening since joining Wilt")).check(matches(isDisplayed()))
        onView(withText("Last listened to 2 months ago")).check(matches(isDisplayed()))
        // The check is redundant here but this is the best way to check the view exists
        onView(allOf(withText("Your favourite song ever"), isDisplayed())).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.shimmer), isDisplayed())).check(doesNotExist())
    }
}
