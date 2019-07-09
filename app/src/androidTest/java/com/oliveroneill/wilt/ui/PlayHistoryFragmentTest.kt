package com.oliveroneill.wilt.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyAbove
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyBelow
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.oliveroneill.wilt.Event
import com.oliveroneill.wilt.R
import com.oliveroneill.wilt.data.dao.ArtistRank
import com.oliveroneill.wilt.viewmodel.PlayHistoryFragmentState
import com.oliveroneill.wilt.viewmodel.PlayHistoryFragmentViewModel
import junit.framework.TestCase.assertEquals
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.`when`
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class PlayHistoryFragmentTest {
    private lateinit var scenario: FragmentScenario<PlayHistoryFragment>
    // Create fake view model for sending events to the UI
    private val viewModel = mock<PlayHistoryFragmentViewModel>()
    private val itemStateData = MutableLiveData<PagedList<ArtistRank>>()
    private val loadingStateData = MutableLiveData<Event<PlayHistoryFragmentState>>()
    // Create factory that returns the fake view model
    private val factory = mock<ViewModelProvider.AndroidViewModelFactory>()
    private val navController = mock<NavController>()

    @Before
    fun setup() {
        whenever(factory.create(PlayHistoryFragmentViewModel::class.java)).thenReturn(viewModel)
        whenever(viewModel.itemDataSource).thenReturn(itemStateData)
        whenever(viewModel.loadingState).thenReturn(loadingStateData)
        // Specify the fragment factory in order to set the view model factory
        scenario = launchFragmentInContainer<PlayHistoryFragment>(
            null,
            R.style.AppTheme,
            object : FragmentFactory() {
                @Suppress("UNCHECKED_CAST")
                override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
                    return (super.instantiate(classLoader, className) as PlayHistoryFragment).also {
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
    fun shouldDisplayRows() {
        // Given
        val list = listOf(
            ArtistRank("09-2019", LocalDate.parse("2019-02-25"), "Pinegrove", 99),
            ArtistRank("52-2018", LocalDate.parse("2018-12-25"), "Bon Iver", 12)
        )
        val pagedList = mock<PagedList<ArtistRank>>()
        `when`(pagedList[ArgumentMatchers.anyInt()]).then { invocation ->
            val index = invocation.arguments.first() as Int
            list[index]
        }
        `when`(pagedList.size).thenReturn(list.size)
        // When
        itemStateData.postValue(pagedList)
        // Then
        onView(withText("Pinegrove")).check(matches(isDisplayed()))
        onView(withText("Bon Iver")).check(matches(isDisplayed()))
        onView(withText("99 plays")).check(matches(isDisplayed()))
        onView(withText("Feb 2019")).check(matches(isDisplayed()))
        onView(withText("12 plays")).check(matches(isDisplayed()))
        onView(withText("Dec 2018")).check(matches(isDisplayed()))
    }

    @Test
    fun shouldDisplayLoadingSpinnerAtBottom() {
        loadingStateData.postValue(Event(PlayHistoryFragmentState.LoadingFromBottom))
        // Then
        onView(withId(R.id.progress_bar)).check(matches(isDisplayed()))
        onView(withId(R.id.loading_txt)).check(matches(isDisplayed()))
    }

    @Test
    fun shouldDisplayLoadingSpinnerAtTop() {
        loadingStateData.postValue(Event(PlayHistoryFragmentState.LoadingFromTop))
        // Then
        onView(withId(R.id.progress_bar)).check(matches(isDisplayed()))
        onView(withId(R.id.loading_txt)).check(matches(isDisplayed()))
    }

    @Test
    fun shouldHideLoadingSpinner() {
        loadingStateData.postValue(Event(PlayHistoryFragmentState.NotLoading))
        // Then
        onView(withId(R.id.progress_bar)).check(doesNotExist())
        onView(withId(R.id.loading_txt)).check(doesNotExist())
    }

    @Test
    fun shouldShowErrorAtBottom() {
        val error = "Some random error message string"
        loadingStateData.postValue(Event(PlayHistoryFragmentState.FailureAtBottom(error) {}))
        // Then
        onView(withText(error)).check(matches(isDisplayed()))
        onView(withId(R.id.progress_bar)).check(matches(not(isDisplayed())))
        onView(withId(R.id.loading_txt)).check(matches(not(isDisplayed())))
    }

    @Test
    fun shouldShowErrorAtTop() {
        val error = "Some random error message string"
        loadingStateData.postValue(Event(PlayHistoryFragmentState.FailureAtTop(error) {}))
        // Then
        onView(withText(error)).check(matches(isDisplayed()))
        onView(withId(R.id.progress_bar)).check(matches(not(isDisplayed())))
        onView(withId(R.id.loading_txt)).check(matches(not(isDisplayed())))
    }

    @Test
    fun shouldShowRowsWhileLoadingFromBottom() {
        // Given
        val list = listOf(
            ArtistRank("09-2019", LocalDate.parse("2019-02-25"), "Pinegrove", 99),
            ArtistRank("52-2018", LocalDate.parse("2018-12-25"), "Bon Iver", 12)
        )
        val pagedList = mock<PagedList<ArtistRank>>()
        `when`(pagedList[ArgumentMatchers.anyInt()]).then { invocation ->
            val index = invocation.arguments.first() as Int
            list[index]
        }
        `when`(pagedList.size).thenReturn(list.size)
        loadingStateData.postValue(Event(PlayHistoryFragmentState.LoadingFromBottom))
        // When
        itemStateData.postValue(pagedList)
        // Then
        onView(withText("Pinegrove")).check(matches(isDisplayed()))
        onView(withText("Bon Iver")).check(matches(isDisplayed()))
        onView(withText("99 plays")).check(matches(isDisplayed()))
        onView(withText("Feb 2019")).check(matches(isDisplayed()))
        onView(withText("12 plays")).check(matches(isDisplayed()))
        onView(withText("Dec 2018")).check(matches(isDisplayed()))
        onView(withId(R.id.progress_bar)).check(matches(isDisplayed()))
        onView(withId(R.id.loading_txt)).check(matches(isDisplayed()))
        // Check that the spinner is below the rows
        onView(withId(R.id.loading_txt)).check(isCompletelyBelow(withText("Bon Iver")))
    }

    @Test
    fun shouldShowRowsWhileLoadingFromTop() {
        // Given
        val list = listOf(
            ArtistRank("09-2019", LocalDate.parse("2019-02-25"), "Pinegrove", 99),
            ArtistRank("52-2018", LocalDate.parse("2018-12-25"), "Bon Iver", 12)
        )
        val pagedList = mock<PagedList<ArtistRank>>()
        `when`(pagedList[ArgumentMatchers.anyInt()]).then { invocation ->
            val index = invocation.arguments.first() as Int
            list[index]
        }
        `when`(pagedList.size).thenReturn(list.size)
        loadingStateData.postValue(Event(PlayHistoryFragmentState.LoadingFromTop))
        // When
        itemStateData.postValue(pagedList)
        // Then
        onView(withText("Pinegrove")).check(matches(isDisplayed()))
        onView(withText("Bon Iver")).check(matches(isDisplayed()))
        onView(withText("99 plays")).check(matches(isDisplayed()))
        onView(withText("Feb 2019")).check(matches(isDisplayed()))
        onView(withText("12 plays")).check(matches(isDisplayed()))
        onView(withText("Dec 2018")).check(matches(isDisplayed()))
        onView(withId(R.id.progress_bar)).check(matches(isDisplayed()))
        onView(withId(R.id.loading_txt)).check(matches(isDisplayed()))
        // Check that the spinner is above the rows
        onView(withId(R.id.loading_txt)).check(isCompletelyAbove(withText("Pinegrove")))
    }

    @Test
    fun shouldShowRowsWithErrorAtBottom() {
        // Given
        val list = listOf(
            ArtistRank("09-2019", LocalDate.parse("2019-02-25"), "Pinegrove", 99),
            ArtistRank("52-2018", LocalDate.parse("2018-12-25"), "Bon Iver", 12)
        )
        val pagedList = mock<PagedList<ArtistRank>>()
        `when`(pagedList[ArgumentMatchers.anyInt()]).then { invocation ->
            val index = invocation.arguments.first() as Int
            list[index]
        }
        `when`(pagedList.size).thenReturn(list.size)
        val error = "Some random error message string"
        loadingStateData.postValue(Event(PlayHistoryFragmentState.FailureAtBottom(error) {}))
        // When
        itemStateData.postValue(pagedList)
        // Then
        onView(withText("Pinegrove")).check(matches(isDisplayed()))
        onView(withText("Bon Iver")).check(matches(isDisplayed()))
        onView(withText("99 plays")).check(matches(isDisplayed()))
        onView(withText("Feb 2019")).check(matches(isDisplayed()))
        onView(withText("12 plays")).check(matches(isDisplayed()))
        onView(withText("Dec 2018")).check(matches(isDisplayed()))
        onView(withText(error)).check(matches(isDisplayed()))
        onView(withId(R.id.progress_bar)).check(matches(not(isDisplayed())))
        onView(withId(R.id.loading_txt)).check(matches(not(isDisplayed())))
        // Check that the error is below the rows
        onView(withText(error)).check(isCompletelyBelow(withText("Bon Iver")))
    }

    @Test
    fun shouldShowRowsWithErrorAtTop() {
        // Given
        val list = listOf(
            ArtistRank("09-2019", LocalDate.parse("2019-02-25"), "Pinegrove", 99),
            ArtistRank("52-2018", LocalDate.parse("2018-12-25"), "Bon Iver", 12)
        )
        val pagedList = mock<PagedList<ArtistRank>>()
        `when`(pagedList[ArgumentMatchers.anyInt()]).then { invocation ->
            val index = invocation.arguments.first() as Int
            list[index]
        }
        `when`(pagedList.size).thenReturn(list.size)
        val error = "Some random error message string"
        loadingStateData.postValue(Event(PlayHistoryFragmentState.FailureAtTop(error) {}))
        // When
        itemStateData.postValue(pagedList)
        // Then
        onView(withText("Pinegrove")).check(matches(isDisplayed()))
        onView(withText("Bon Iver")).check(matches(isDisplayed()))
        onView(withText("99 plays")).check(matches(isDisplayed()))
        onView(withText("Feb 2019")).check(matches(isDisplayed()))
        onView(withText("12 plays")).check(matches(isDisplayed()))
        onView(withText("Dec 2018")).check(matches(isDisplayed()))
        onView(withText(error)).check(matches(isDisplayed()))
        onView(withId(R.id.progress_bar)).check(matches(not(isDisplayed())))
        onView(withId(R.id.loading_txt)).check(matches(not(isDisplayed())))
        // Check that the error is above the rows
        onView(withText(error)).check(isCompletelyAbove(withText("Pinegrove")))
    }

    @Test
    fun shouldRetryOnPressErrorAtBottom() {
        val error = "Some random error message string"
        var retryCallCount = 0
        // Given
        loadingStateData.postValue(
            Event(
                PlayHistoryFragmentState.FailureAtBottom(error) {
                    retryCallCount += 1
                }
            )
        )
        // When
        onView(withId(R.id.retry_button)).perform(click())
        // Then
        assertEquals(1, retryCallCount)
    }

    @Test
    fun shouldRetryOnPressErrorAtTop() {
        val error = "Some random error message string"
        var retryCallCount = 0
        // Given
        loadingStateData.postValue(
            Event(
                PlayHistoryFragmentState.FailureAtTop(error) {
                    retryCallCount += 1
                }
            )
        )
        // When
        onView(withId(R.id.retry_button)).perform(click())
        // Then
        assertEquals(1, retryCallCount)
    }

    @Test
    fun shouldRefreshOnSwipe() {
        val mockDataSource = mock<DataSource<*, ArtistRank>>()
        val pagedList = mock<PagedList<ArtistRank>>()
        whenever(pagedList.dataSource).then { mockDataSource }
        // Given
        itemStateData.postValue(pagedList)
        // When
        onView(withId(R.id.swipe_refresh)).perform(swipeDown())
        // Then
        verify(mockDataSource).invalidate()
    }
}
