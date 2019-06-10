package com.oliveroneill.wilt.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.paging.PagedList
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.oliveroneill.wilt.Event
import com.oliveroneill.wilt.R
import com.oliveroneill.wilt.viewmodel.ArtistRank
import com.oliveroneill.wilt.viewmodel.PlayHistoryFragmentState
import com.oliveroneill.wilt.viewmodel.PlayHistoryFragmentViewModel
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.`when`

@RunWith(AndroidJUnit4::class)
class PlayHistoryFragmentTest {
    lateinit var scenario: FragmentScenario<PlayHistoryFragment>
    // Create fake view model for sending events to the UI
    val viewModel = mock<PlayHistoryFragmentViewModel>()
    val itemStateData = MutableLiveData<PagedList<ArtistRank>>()
    val loadingStateData = MutableLiveData<Event<PlayHistoryFragmentState>>()
    // Create factory that returns the fake view model
    val factory = mock<ViewModelProvider.AndroidViewModelFactory>()
    val navController = mock<NavController>()

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
                override fun instantiate(classLoader: ClassLoader, className: String, args: Bundle?): Fragment {
                    return (super.instantiate(classLoader, className, args) as PlayHistoryFragment).also {
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
    fun shouldDisplayRow() {
        // Given
        val list = listOf(ArtistRank("Feb 2019", "Pinegrove", 0))
        val pagedList = mock<PagedList<ArtistRank>>()
        `when`(pagedList.get(ArgumentMatchers.anyInt())).then { invocation ->
            val index = invocation.arguments.first() as Int
            list[index]
        }
        `when`(pagedList.size).thenReturn(list.size)
        // When
        itemStateData.postValue(pagedList)
        // Then
        onView(withText("Pinegrove")).check(matches(isDisplayed()));

    }

    @Test
    fun shouldDisplayLoadingSpinner() {
        loadingStateData.postValue(Event(PlayHistoryFragmentState.LoadingMore))
        // Then
        onView(withId(R.id.progress_bar)).check(matches(isDisplayed()))
    }

    @Test
    fun shouldHideLoadingSpinner() {
        loadingStateData.postValue(Event(PlayHistoryFragmentState.NotLoading))
        // Then
        onView(withId(R.id.progress_bar)).check(doesNotExist())
    }

    @Test
    fun shouldShowError() {
        val error = "Some random error message string"
        loadingStateData.postValue(Event(PlayHistoryFragmentState.Failure(error)))
        // Then
        onView(withText(error)).check(matches(isDisplayed()))
        onView(withId(R.id.progress_bar)).check(matches(not(isDisplayed())))
    }
}
