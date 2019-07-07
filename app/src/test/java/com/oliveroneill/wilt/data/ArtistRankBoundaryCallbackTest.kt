package com.oliveroneill.wilt.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.*
import com.oliveroneill.wilt.Event
import com.oliveroneill.wilt.data.dao.ArtistRank
import com.oliveroneill.wilt.data.dao.PlayHistoryDao
import com.oliveroneill.wilt.viewmodel.PlayHistoryFragmentState
import junit.framework.TestCase
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.util.concurrent.Executor

class ArtistRankBoundaryCallbackTest {
    // Required to test LiveData
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val pageSize = 11L
    private lateinit var firebase: FirebaseAPI
    private lateinit var dao: PlayHistoryDao
    private lateinit var loadingState: MutableLiveData<Event<PlayHistoryFragmentState>>
    private lateinit var boundaryCallback: ArtistRankBoundaryCallback

    /**
     * Useful executor for serialising functions for testing
     */
    class CurrentThreadExecutor: Executor {
        override fun execute(runnable: Runnable?) {
            runnable?.run()
        }
    }

    @Before
    fun setup() {
        firebase = mock()
        // Default value of firebase will be success
        whenever(firebase.topArtists(any(), any(), any())).then {
            it.getArgument<(Result<List<ArtistRank>>) -> Unit>(2)(Result.success(listOf()))
        }
        loadingState = MutableLiveData()
        dao = mock()
        boundaryCallback = ArtistRankBoundaryCallback(
            dao, firebase, loadingState, pageSize,
            CurrentThreadExecutor()
        )
    }


    @Test
    fun `should convert timestamps correctly`() {
        val item = ArtistRank("09-2019", "2019-02-25", "Pinegrove", 99)
        boundaryCallback.onItemAtFrontLoaded(item)
        verify(firebase).topArtists(eq(1543755600), eq(1550408400), any())
    }

    @Test
    fun `should modify request based on page size`() {
        boundaryCallback = ArtistRankBoundaryCallback(dao, firebase, loadingState, 4L)
        val item = ArtistRank("13-2019", "2019-03-25", "Pinegrove", 99)
        boundaryCallback.onItemAtFrontLoaded(item)
        verify(firebase).topArtists(eq(1550408400), eq(1552827600), any())
    }

    @Test
    fun `should insert loaded data`() {
        val expected = listOf(
            ArtistRank("09-2019", "2019-02-25", "Pinegrove", 99),
            ArtistRank("52-2018", "2018-12-25", "Bon Iver", 12)
        )
        whenever(firebase.topArtists(any(), any(), any())).then {
            it.getArgument<(Result<List<ArtistRank>>) -> Unit>(2)(Result.success(expected))
        }
        val item = ArtistRank("47-2018", "2018-11-25", "Tyler, The Creator", 10)
        boundaryCallback.onItemAtFrontLoaded(item)
        verify(dao).insert(eq(expected))
    }

    @Test
    fun `should update loading state while loading`() {
        // Assertion is asynchronous and is checked once the network call is made
        whenever(firebase.topArtists(any(), any(), any())).then { invocation ->
            // Assert loading state is loading
            loadingState
                .test()
                .assertHasValue()
                .assertValue { it.getContentIfNotHandled() is PlayHistoryFragmentState.LoadingMore }
            // Send success value to stop blocking
            invocation.getArgument<(Result<List<ArtistRank>>) -> Unit>(2)(Result.success(listOf()))
        }
        val item = ArtistRank("09-2019", "2019-02-25", "Pinegrove", 99)
        boundaryCallback.onItemAtFrontLoaded(item)
        // Ensure that we fail if this isn't called since otherwise the assertions are never actually run
        verify(firebase).topArtists(any(), any(), any())
    }

    @Test
    fun `should update loading state on success`() {
        // We'll make the mock send back a successful value
        whenever(firebase.topArtists(any(), any(), any())).then {
            it.getArgument<(Result<List<ArtistRank>>) -> Unit>(2)(Result.success(listOf()))
        }
        val item = ArtistRank("09-2019", "2019-02-25", "Pinegrove", 99)
        boundaryCallback.onItemAtFrontLoaded(item)
        loadingState
            .test()
            .assertHasValue()
            .assertValue { it.getContentIfNotHandled() is PlayHistoryFragmentState.NotLoading }
    }

    @Test
    fun `should handle error`() {
        val expected = "This is a test error for ArtistRankDataSource"
        val error = IOException(expected)
        // We'll make the mock send back an error
        whenever(firebase.topArtists(any(), any(), any())).then {
            it.getArgument<(Result<List<ArtistRank>>) -> Unit>(2)(Result.failure(error))
        }
        val item = ArtistRank("09-2019", "2019-02-25", "Pinegrove", 99)
        boundaryCallback.onItemAtFrontLoaded(item)
        loadingState
            .test()
            .assertHasValue()
            .assertValue {
                val state = it.getContentIfNotHandled()
                state is PlayHistoryFragmentState.Failure && state.error == expected
            }
    }

    @Test
    fun `should correctly set retry`() {
        val expected = "This is a test error for ArtistRankDataSource"
        val error = IOException(expected)
        // We'll make the mock send back an error
        whenever(firebase.topArtists(any(), any(), any())).then {
            it.getArgument<(Result<List<ArtistRank>>) -> Unit>(2)(Result.failure(error))
        }
        val item = ArtistRank("09-2019", "2019-02-25", "Pinegrove", 99)
        boundaryCallback.onItemAtFrontLoaded(item)
        // Get the state that was sent
        val state = loadingState.value?.getContentIfNotHandled()
        when (state) {
            is PlayHistoryFragmentState.Failure -> {
                // Call retry
                state.retry()
                // Ensure that it makes the correct call. This will be the second call
                verify(firebase, times(2)).topArtists(
                    eq(1543755600), eq(1550408400), any()
                )
            }
            else -> {
                // Fail if we didn't get an error
                TestCase.fail()
            }
        }
    }

    @Test
    fun `should use current date if none specified`() {
        boundaryCallback.onZeroItemsLoaded()
        // I should mock the date somehow, but I think if I just test that it still actually makes a request then that
        // should be good enough for now...
        verify(firebase).topArtists(any(), any(), any())
    }

    @Test
    fun `should load after a specified date`() {
        val item = ArtistRank("09-2018", "2018-02-25", "Pinegrove", 99)
        boundaryCallback.onItemAtEndLoaded(item)
        verify(firebase).topArtists(eq(1520082000), eq(1526738400), any())
    }

    @Test
    fun `should load before a specified date`() {
        val item = ArtistRank("13-2019", "2019-03-25", "Pinegrove", 99)
        boundaryCallback.onItemAtFrontLoaded(item)
        verify(firebase).topArtists(eq(1546174800), eq(1552827600), any())
    }
}