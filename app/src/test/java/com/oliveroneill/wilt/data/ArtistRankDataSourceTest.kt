package com.oliveroneill.wilt.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.ItemKeyedDataSource
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.*
import com.oliveroneill.wilt.Event
import com.oliveroneill.wilt.viewmodel.ArtistRank
import com.oliveroneill.wilt.viewmodel.PlayHistoryFragmentState
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.LocalDate

class ArtistRankDataSourceTest {
    // Required to test LiveData
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var firebase: FirebaseAPI
    private lateinit var loadingState: MutableLiveData<Event<PlayHistoryFragmentState>>
    private lateinit var dataSource: ArtistRankDataSource

    @Before
    fun setup() {
        firebase = mock()
        // Default value of firebase will be success
        whenever(firebase.topArtists(any(), any(), any())).then {
            it.getArgument<(Result<List<ArtistRank>>) -> Unit>(2)(Result.success(listOf()))
        }
        loadingState = MutableLiveData()
        dataSource = ArtistRankDataSource(loadingState, firebase)
    }

    /**
     * Helpful class to wrap callbacks with lambdas so tests aren't ugly
     */
    class TestCallback(val callback: (List<ArtistRank>) -> Unit): ItemKeyedDataSource.LoadInitialCallback<ArtistRank>() {
        override fun onResult(data: MutableList<ArtistRank>) {
            callback(data)
        }
        override fun onResult(data: MutableList<ArtistRank>, position: Int, totalCount: Int) {
            callback(data)
        }
    }

    @Test
    fun `should convert timestamps correctly`() {
        val date = LocalDate.parse("2019-02-25")
        val params = ItemKeyedDataSource.LoadParams(date, 11)
        dataSource.loadAfter(params, TestCallback {})
        verify(firebase).topArtists(eq(1519477200), eq(1548334800), any())
    }

    @Test
    fun `should modify request based on page size`() {
        val date = LocalDate.parse("2019-03-25")
        val params = ItemKeyedDataSource.LoadParams(date, 4)
        dataSource.loadAfter(params, TestCallback {})
        verify(firebase).topArtists(eq(1540386000), eq(1551013200), any())
    }

    @Test
    fun `should send loaded data through data source`() {
        val expected = listOf(
            ArtistRank("2019-02-25", "Pinegrove", 99),
            ArtistRank("2018-12-25", "Bon Iver", 12)
        )
        whenever(firebase.topArtists(any(), any(), any())).then {
            it.getArgument<(Result<List<ArtistRank>>) -> Unit>(2)(Result.success(expected))
        }
        val date = LocalDate.now()
        val params = ItemKeyedDataSource.LoadInitialParams(date, 11, false)
        var result: List<ArtistRank>? = null
        dataSource.loadInitial(params, TestCallback {
            result = it
        })
        assertEquals(expected, result)
    }

    @Test
    fun `should update loading state while loading`() {
        // Assertion is asynchronous and is checked once the network call is made
        whenever(firebase.topArtists(any(), any(), any())).then {
            // Assert loading state is loading
            loadingState
                .test()
                .assertHasValue()
                .assertValue { it.getContentIfNotHandled() is PlayHistoryFragmentState.LoadingMore }
            // Send success value to stop blocking
            it.getArgument<(Result<List<ArtistRank>>) -> Unit>(2)(Result.success(listOf()))
        }
        val date = LocalDate.parse("2019-02-25")
        val params = ItemKeyedDataSource.LoadInitialParams(date, 11, false)
        dataSource.loadInitial(params, TestCallback {})
        // Ensure that we fail if this isn't called since otherwise the assertions are never actually run
        verify(firebase).topArtists(any(), any(), any())
    }

    @Test
    fun `should update loading state on success`() {
        val date = LocalDate.parse("2019-02-25")
        // We'll make the mock send back a successful value
        whenever(firebase.topArtists(any(), any(), any())).then {
            it.getArgument<(Result<List<ArtistRank>>) -> Unit>(2)(Result.success(listOf()))
        }
        val params = ItemKeyedDataSource.LoadInitialParams(date, 11, false)
        dataSource.loadInitial(params, TestCallback {})
        loadingState
            .test()
            .assertHasValue()
            .assertValue { it.getContentIfNotHandled() is PlayHistoryFragmentState.NotLoading }
    }

    @Test
    fun `should handle error`() {
        val expected = "This is a test error for ArtistRankDataSource"
        val error = IOException(expected)
        val date = LocalDate.parse("2019-02-25")
        // We'll make the mock send back an error
        whenever(firebase.topArtists(any(), any(), any())).then {
            it.getArgument<(Result<List<ArtistRank>>) -> Unit>(2)(Result.failure(error))
        }
        val params = ItemKeyedDataSource.LoadInitialParams(date, 11, false)
        dataSource.loadInitial(params, TestCallback {})
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
        val date = LocalDate.parse("2019-02-25")
        // We'll make the mock send back an error
        whenever(firebase.topArtists(any(), any(), any())).then {
            it.getArgument<(Result<List<ArtistRank>>) -> Unit>(2)(Result.failure(error))
        }
        val params = ItemKeyedDataSource.LoadParams(date, 11)
        dataSource.loadAfter(params, TestCallback {})
        // Get the state that was sent
        val state = loadingState.value?.getContentIfNotHandled()
        when (state) {
            is PlayHistoryFragmentState.Failure -> {
                // Call retry
                state.retry()
                // Ensure that it makes the correct call. This will be the second call
                verify(firebase, times(2)).topArtists(
                    eq(1519477200), eq(1548334800), any()
                )
            }
            else -> {
                // Fail if we didn't get an error
                fail()
            }
        }
    }

    @Test
    fun `should use current date if none specified`() {
        val params = ItemKeyedDataSource.LoadInitialParams<LocalDate>(
            null, 11, false
        )
        dataSource.loadInitial(params, TestCallback {})
        // I should mock the date somehow, but I think if I just test that it still actually makes a request then that
        // should be good enough for now...
        verify(firebase).topArtists(any(), any(), any())
    }

    @Test
    fun `should load after a specified date`() {
        val date = LocalDate.parse("2019-03-25")
        val params = ItemKeyedDataSource.LoadParams(date, 11)
        dataSource.loadAfter(params, TestCallback {})
        verify(firebase).topArtists(eq(1521896400), eq(1551013200), any())
    }

    @Test
    fun `should load before a specified date`() {
        val date = LocalDate.parse("2018-02-25")
        val params = ItemKeyedDataSource.LoadParams(date, 11)
        dataSource.loadBefore(params, TestCallback {})
        verify(firebase).topArtists(eq(1521896400), eq(1551013200), any())
    }
}
