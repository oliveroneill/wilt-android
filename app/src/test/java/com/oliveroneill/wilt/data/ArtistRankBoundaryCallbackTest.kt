package com.oliveroneill.wilt.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.google.firebase.functions.FirebaseFunctionsException
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.*
import com.oliveroneill.wilt.Message
import com.oliveroneill.wilt.data.dao.ArtistRank
import com.oliveroneill.wilt.data.dao.PlayHistoryDao
import com.oliveroneill.wilt.viewmodel.PlayHistoryNetworkState
import com.oliveroneill.wilt.viewmodel.PlayHistoryState
import junit.framework.TestCase
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.LocalDate
import java.util.concurrent.Executor

class ArtistRankBoundaryCallbackTest {
    // Required to test LiveData
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val pageSize = 11L
    private lateinit var firebase: FirebaseAPI
    private lateinit var dao: PlayHistoryDao
    private lateinit var loadingState: MutableLiveData<Message<PlayHistoryState>>
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
        whenever(firebase.topArtistsPerWeek(any(), any(), any())).then {
            it.getArgument<(Result<List<ArtistRank>>) -> Unit>(2)(Result.success(listOf()))
        }
        loadingState = MutableLiveData()
        dao = mock()
        boundaryCallback = ArtistRankBoundaryCallback(
            dao, firebase, loadingState, pageSize,
            CurrentThreadExecutor()
        )
    }

    /**
     * Helper function to unwrap the message and the state. This will return null if there's no value or
     * you're not logged in
     */
    private fun Message<PlayHistoryState>.unwrapState(): PlayHistoryNetworkState? {
        val state = getContent()
        if (state is PlayHistoryState.LoggedIn) return state.state
        TestCase.fail()
        return null
    }

    @Test
    fun `should convert timestamps correctly`() {
        val item = ArtistRank(
            "09-2019",
            LocalDate.parse("2019-02-25"),
            "Pinegrove",
            99,
            "http://arandomurl.net/img.png",
            "http://anotherrandomurl.net/img.png",
            "spotify://arandomurl.net/img.png"
        )
        boundaryCallback.onItemAtEndLoaded(item)
        verify(firebase).topArtistsPerWeek(eq(1543795200), eq(1550448000), any())
    }

    @Test
    fun `should modify request based on page size`() {
        boundaryCallback = ArtistRankBoundaryCallback(dao, firebase, loadingState, 4L)
        val item = ArtistRank(
            "13-2019",
            LocalDate.parse("2019-03-25"),
            "Pinegrove",
            99,
            "http://arandomurl.net/img.png",
            "http://anotherrandomurl.net/img.png",
            "spotify://arandomurl.net/img.png"
        )
        boundaryCallback.onItemAtEndLoaded(item)
        verify(firebase).topArtistsPerWeek(eq(1550448000), eq(1552867200), any())
    }

    @Test
    fun `should insert loaded data`() {
        val expected = listOf(
            ArtistRank(
                "09-2019",
                LocalDate.parse("2019-02-25"),
                "Pinegrove",
                99,
                "http://arandomurl.net/img.png",
                "http://anotherrandomurl.net/img.png",
                "spotify://arandomurl.net/img.png"
            ),
            ArtistRank(
                "52-2018",
                LocalDate.parse("2018-12-25"),
                "Bon Iver",
                12,
                "http://anotherdomain.com/bla.png",
                "http://anotherrandomurl.net/img.png",
                "spotify://arandomurl.net/img.png"
            )
        )
        whenever(firebase.topArtistsPerWeek(any(), any(), any())).then {
            it.getArgument<(Result<List<ArtistRank>>) -> Unit>(2)(Result.success(expected))
        }
        val item = ArtistRank(
            "47-2018",
            LocalDate.parse("2018-11-25"),
            "Tyler, The Creator",
            10,
            "http://arandomurl.net/img.png",
            "http://anotherrandomurl.net/img.png",
            "spotify://arandomurl.net/img.png"
        )
        boundaryCallback.onItemAtFrontLoaded(item)
        verify(dao).insert(eq(expected))
    }

    @Test
    fun `should update loading state when loading from top`() {
        // Assertion is asynchronous and is checked once the network call is made
        whenever(firebase.topArtistsPerWeek(any(), any(), any())).then { invocation ->
            // Assert loading state is loading
            loadingState
                .test()
                .assertHasValue()
                .assertValue { it.unwrapState() is PlayHistoryNetworkState.LoadingFromTop }
            // Send success value to stop blocking
            invocation.getArgument<(Result<List<ArtistRank>>) -> Unit>(2)(Result.success(listOf()))
        }
        val item = ArtistRank(
            "09-2019",
            LocalDate.parse("2019-02-25"),
            "Pinegrove",
            99,
            "http://arandomurl.net/img.png",
            "http://anotherrandomurl.net/img.png",
            "spotify://arandomurl.net/img.png"
        )
        boundaryCallback.onItemAtFrontLoaded(item)
        // Ensure that we fail if this isn't called since otherwise the assertions are never actually run
        verify(firebase).topArtistsPerWeek(any(), any(), any())
    }

    @Test
    fun `should update loading state when loading from bottom`() {
        // Assertion is asynchronous and is checked once the network call is made
        whenever(firebase.topArtistsPerWeek(any(), any(), any())).then { invocation ->
            // Assert loading state is loading
            loadingState
                .test()
                .assertHasValue()
                .assertValue { it.unwrapState() is PlayHistoryNetworkState.LoadingFromBottom }
            // Send success value to stop blocking
            invocation.getArgument<(Result<List<ArtistRank>>) -> Unit>(2)(Result.success(listOf()))
        }
        val item = ArtistRank(
            "09-2019",
            LocalDate.parse("2019-02-25"),
            "Pinegrove",
            99,
            "http://arandomurl.net/img.png",
            "http://anotherrandomurl.net/img.png",
            "spotify://arandomurl.net/img.png"
        )
        boundaryCallback.onItemAtEndLoaded(item)
        // Ensure that we fail if this isn't called since otherwise the assertions are never actually run
        verify(firebase).topArtistsPerWeek(any(), any(), any())
    }

    @Test
    fun `should update loading state on success`() {
        // We'll make the mock send back a successful value
        whenever(firebase.topArtistsPerWeek(any(), any(), any())).then {
            it.getArgument<(Result<List<ArtistRank>>) -> Unit>(2)(Result.success(listOf()))
        }
        val item = ArtistRank(
            "09-2019",
            LocalDate.parse("2019-02-25"),
            "Pinegrove",
            99,
            "http://arandomurl.net/img.png",
            "http://anotherrandomurl.net/img.png",
            "spotify://arandomurl.net/img.png"
        )
        boundaryCallback.onItemAtFrontLoaded(item)
        loadingState
            .test()
            .assertHasValue()
            .assertValue { it.unwrapState() is PlayHistoryNetworkState.NotLoading }
    }

    @Test
    fun `should handle error when loading from top`() {
        val expected = "This is a test error for ArtistRankDataSource"
        val error = IOException(expected)
        // We'll make the mock send back an error
        whenever(firebase.topArtistsPerWeek(any(), any(), any())).then {
            it.getArgument<(Result<List<ArtistRank>>) -> Unit>(2)(Result.failure(error))
        }
        val item = ArtistRank(
            "09-2019",
            LocalDate.parse("2019-02-25"),
            "Pinegrove",
            99,
            "http://arandomurl.net/img.png",
            "http://anotherrandomurl.net/img.png",
            "spotify://arandomurl.net/img.png"
        )
        boundaryCallback.onItemAtFrontLoaded(item)
        loadingState
            .test()
            .assertHasValue()
            .assertValue {
                val state = it.unwrapState()
                state is PlayHistoryNetworkState.FailureAtTop && state.error == expected
            }
    }

    @Test
    fun `should correctly set retry when loading from top`() {
        val expected = "This is a test error for ArtistRankDataSource"
        val error = IOException(expected)
        // We'll make the mock send back an error
        whenever(firebase.topArtistsPerWeek(any(), any(), any())).then {
            it.getArgument<(Result<List<ArtistRank>>) -> Unit>(2)(Result.failure(error))
        }
        val item = ArtistRank(
            "09-2018",
            LocalDate.parse("2018-02-25"),
            "Pinegrove",
            99,
            "http://arandomurl.net/img.png",
            "http://anotherrandomurl.net/img.png",
            "spotify://arandomurl.net/img.png"
        )
        boundaryCallback.onItemAtFrontLoaded(item)
        // Get the state that was sent
        val state = loadingState.value?.unwrapState()
        when (state) {
            is PlayHistoryNetworkState.FailureAtTop -> {
                // Call retry
                state.retry()
                // Ensure that it makes the correct call. This will be the second call
                verify(firebase, times(2)).topArtistsPerWeek(
                    eq(1518998400), eq(1525651200), any()
                )
            }
            else -> {
                // Fail if we didn't get an error
                TestCase.fail()
            }
        }
    }

    @Test
    fun `should handle error when loading from bottom`() {
        val expected = "This is a test error for ArtistRankDataSource"
        val error = IOException(expected)
        // We'll make the mock send back an error
        whenever(firebase.topArtistsPerWeek(any(), any(), any())).then {
            it.getArgument<(Result<List<ArtistRank>>) -> Unit>(2)(Result.failure(error))
        }
        val item = ArtistRank(
            "09-2019",
            LocalDate.parse("2019-02-25"),
            "Pinegrove",
            99,
            "http://arandomurl.net/img.png",
            "http://anotherrandomurl.net/img.png",
            "spotify://arandomurl.net/img.png"
        )
        boundaryCallback.onItemAtEndLoaded(item)
        loadingState
            .test()
            .assertHasValue()
            .assertValue {
                val state = it.unwrapState()
                state is PlayHistoryNetworkState.FailureAtBottom && state.error == expected
            }
    }

    @Test
    fun `should correctly set retry when loading from bottom`() {
        val expected = "This is a test error for ArtistRankDataSource"
        val error = IOException(expected)
        // We'll make the mock send back an error
        whenever(firebase.topArtistsPerWeek(any(), any(), any())).then {
            it.getArgument<(Result<List<ArtistRank>>) -> Unit>(2)(Result.failure(error))
        }
        val item = ArtistRank(
            "09-2019",
            LocalDate.parse("2019-02-25"),
            "Pinegrove",
            99,
            "http://arandomurl.net/img.png",
            "http://anotherrandomurl.net/img.png",
            "spotify://arandomurl.net/img.png"
        )
        boundaryCallback.onItemAtEndLoaded(item)
        // Get the state that was sent
        val state = loadingState.value?.unwrapState()
        when (state) {
            is PlayHistoryNetworkState.FailureAtBottom -> {
                // Call retry
                state.retry()
                // Ensure that it makes the correct call. This will be the second call
                verify(firebase, times(2)).topArtistsPerWeek(
                    eq(1543795200), eq(1550448000), any()
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
        verify(firebase).topArtistsPerWeek(any(), any(), any())
    }

    @Test
    fun `should load after a specified date`() {
        val item = ArtistRank(
            "09-2018",
            LocalDate.parse("2018-02-25"),
            "Pinegrove",
            99,
            "http://arandomurl.net/img.png",
            "http://anotherrandomurl.net/img.png",
            "spotify://arandomurl.net/img.png"
        )
        boundaryCallback.onItemAtFrontLoaded(item)
        verify(firebase).topArtistsPerWeek(eq(1518998400), eq(1525651200), any())
    }

    @Test
    fun `should not refresh twice`() {
        val item = ArtistRank(
            "09-2018",
            LocalDate.parse("2018-02-25"),
            "Pinegrove",
            99,
            "http://arandomurl.net/img.png",
            "http://anotherrandomurl.net/img.png",
            "spotify://arandomurl.net/img.png"
        )
        // First we refresh the current week
        boundaryCallback.onItemAtFrontLoaded(item)
        verify(firebase).topArtistsPerWeek(eq(1518998400), eq(1525651200), any())
        // Now we won't bother updating again since this onItemAtFrontLoaded was triggered
        // from the refresh
        boundaryCallback.onItemAtFrontLoaded(item)
        verifyNoMoreInteractions(firebase)
    }

    @Test
    fun `should not refresh current week twice`() {
        val item = ArtistRank(
            "09-2018",
            LocalDate.parse("2018-02-25"),
            "Pinegrove",
            99,
            "http://arandomurl.net/img.png",
            "http://anotherrandomurl.net/img.png",
            "spotify://arandomurl.net/img.png"
        )
        // First we refresh the current week
        boundaryCallback.onItemAtFrontLoaded(item)
        verify(firebase).topArtistsPerWeek(eq(1518998400), eq(1525651200), any())
        // The callback will expect this but we won't update since this is just from
        // the update of the current week
        boundaryCallback.onItemAtFrontLoaded(item)
        verifyNoMoreInteractions(firebase)
        // If we call again it will now skip that week
        boundaryCallback.onItemAtFrontLoaded(item)
        verify(firebase).topArtistsPerWeek(eq(1519603200), eq(1526256000), any())
    }

    @Test
    fun `should update next page after refreshing not including current week`() {
        val item = ArtistRank(
            "09-2018",
            LocalDate.parse("2018-02-25"),
            "Pinegrove",
            99,
            "http://arandomurl.net/img.png",
            "http://anotherrandomurl.net/img.png",
            "spotify://arandomurl.net/img.png"
        )
        // We'll send back two values so that the callback knows we didn't just refresh the
        // current week and there may be more data to come
        whenever(firebase.topArtistsPerWeek(any(), any(), any())).then {
            it.getArgument<(Result<List<ArtistRank>>) -> Unit>(2)(Result.success(listOf(item, item)))
        }
        // First we refresh the current week
        boundaryCallback.onItemAtFrontLoaded(item)
        verify(firebase).topArtistsPerWeek(eq(1518998400), eq(1525651200), any())
        // Now we should skip the current week and move onto the next one
        boundaryCallback.onItemAtFrontLoaded(item)
        verify(firebase).topArtistsPerWeek(eq(1519603200), eq(1526256000), any())
    }

    @Test
    fun `should load before a specified date`() {
        val item = ArtistRank(
            "13-2019",
            LocalDate.parse("2019-03-25"),
            "Pinegrove",
            99,
            "http://arandomurl.net/img.png",
            "http://anotherrandomurl.net/img.png",
            "spotify://arandomurl.net/img.png"
        )
        boundaryCallback.onItemAtEndLoaded(item)
        verify(firebase).topArtistsPerWeek(eq(1546214400), eq(1552867200), any())
    }

    @Test
    fun `should handle unauthenticated error`() {
        val error = mock<FirebaseFunctionsException>()
        whenever(error.code).thenReturn(FirebaseFunctionsException.Code.UNAUTHENTICATED)
        // We'll make the mock send back an error
        whenever(firebase.topArtistsPerWeek(any(), any(), any())).then {
            it.getArgument<(Result<List<ArtistRank>>) -> Unit>(2)(Result.failure(error))
        }
        val item = ArtistRank(
            "09-2019",
            LocalDate.parse("2019-02-25"),
            "Pinegrove",
            99,
            "http://arandomurl.net/img.png",
            "http://anotherrandomurl.net/img.png",
            "spotify://arandomurl.net/img.png"
        )
        boundaryCallback.onItemAtEndLoaded(item)
        loadingState
            .test()
            .assertHasValue()
            .assertValue {
                it.getContent() is PlayHistoryState.LoggedOut
            }
    }

    @Test
    fun `should send no data message when there's nothing available`() {
        val expected = listOf<ArtistRank>()
        whenever(firebase.topArtistsPerWeek(any(), any(), any())).then {
            it.getArgument<(Result<List<ArtistRank>>) -> Unit>(2)(Result.success(expected))
        }
        boundaryCallback.onZeroItemsLoaded()
        loadingState
            .test()
            .assertHasValue()
            .assertValue {
                val state = it.unwrapState()
                state is PlayHistoryNetworkState.NoRows
            }
    }

    @Test
    fun `should update loading state correctly when retrying from top`() {
        // We'll make the mock send back an error
        whenever(firebase.topArtistsPerWeek(any(), any(), any())).then {
            it.getArgument<(Result<List<ArtistRank>>) -> Unit>(2)(Result.failure(IOException("")))
        }
        val item = ArtistRank(
            "09-2019",
            LocalDate.parse("2019-02-25"),
            "Pinegrove",
            99,
            "http://arandomurl.net/img.png",
            "http://anotherrandomurl.net/img.png",
            "spotify://arandomurl.net/img.png"
        )
        boundaryCallback.onItemAtFrontLoaded(item)
        // Get the state that was sent
        val state = loadingState.value?.unwrapState()
        when (state) {
            is PlayHistoryNetworkState.FailureAtTop -> {
                // Do nothing on the next call so that it stays in the loading state
                whenever(firebase.topArtistsPerWeek(any(), any(), any())).then { }
                // Call retry
                state.retry()
                // Ensure it goes into loading from bottom state
                loadingState
                    .test()
                    .assertHasValue()
                    .assertValue { it.unwrapState() is PlayHistoryNetworkState.LoadingFromTop }
            }
            else -> {
                // Fail if we didn't get an error
                TestCase.fail()
            }
        }
    }

    @Test
    fun `should update loading state correctly when retrying from bottom`() {
        // We'll make the mock send back an error
        whenever(firebase.topArtistsPerWeek(any(), any(), any())).then {
            it.getArgument<(Result<List<ArtistRank>>) -> Unit>(2)(Result.failure(IOException("")))
        }
        val item = ArtistRank(
            "09-2019",
            LocalDate.parse("2019-02-25"),
            "Pinegrove",
            99,
            "http://arandomurl.net/img.png",
            "http://anotherrandomurl.net/img.png",
            "spotify://arandomurl.net/img.png"
        )
        boundaryCallback.onItemAtEndLoaded(item)
        // Get the state that was sent
        val state = loadingState.value?.unwrapState()
        when (state) {
            is PlayHistoryNetworkState.FailureAtBottom -> {
                // Do nothing on the next call so that it stays in the loading state
                whenever(firebase.topArtistsPerWeek(any(), any(), any())).then { }
                // Call retry
                state.retry()
                // Ensure it goes into loading from bottom state
                loadingState
                    .test()
                    .assertHasValue()
                    .assertValue { it.unwrapState() is PlayHistoryNetworkState.LoadingFromBottom }
            }
            else -> {
                // Fail if we didn't get an error
                TestCase.fail()
            }
        }
    }

    @Test
    fun `should update no rows state correctly when retrying`() {
        // We'll make the mock send back an error
        whenever(firebase.topArtistsPerWeek(any(), any(), any())).then {
            it.getArgument<(Result<List<ArtistRank>>) -> Unit>(2)(Result.failure(IOException("")))
        }
        boundaryCallback.onZeroItemsLoaded()
        // Get the state that was sent
        val state = loadingState.value?.unwrapState()
        when (state) {
            is PlayHistoryNetworkState.FailureAtTop -> {
                // Send back an empty response to ensure we will respond with no rows state
                whenever(firebase.topArtistsPerWeek(any(), any(), any())).then {
                    it.getArgument<(Result<List<ArtistRank>>) -> Unit>(2)(Result.success(listOf()))
                }
                // Call retry
                state.retry()
                // Ensure it goes into loading from bottom state
                loadingState
                    .test()
                    .assertHasValue()
                    .assertValue { it.unwrapState() is PlayHistoryNetworkState.NoRows }
            }
            else -> {
                // Fail if we didn't get an error
                TestCase.fail()
            }
        }
    }
}
