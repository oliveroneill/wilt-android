package com.oliveroneill.wilt.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.oliveroneill.wilt.data.FirebaseAPI
import junit.framework.TestCase.*
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.RETURNS_MOCKS

class PlayHistoryFragmentViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val application = mock<Application>(defaultAnswer = RETURNS_MOCKS)

    @Test
    fun `should convert state to view data correctly for loading from top`() {
        val data = PlayHistoryNetworkState.LoadingFromTop.toViewData()
        assert(data.loadingMessageVisible)
        assert(data.progressBarVisible)
        assertNull(data.errorMessage)
        assertNull(data.retry)
        assert(!data.retryButtonVisible)
    }

    @Test
    fun `should convert state to view data correctly for loading from bottom`() {
        val data = PlayHistoryNetworkState.LoadingFromBottom.toViewData()
        assert(data.loadingMessageVisible)
        assert(data.progressBarVisible)
        assertNull(data.errorMessage)
        assertNull(data.retry)
        assert(!data.retryButtonVisible)
    }

    @Test
    fun `should convert state to view data correctly for failure from top`() {
        val errorMessage = "Hi this is a random error message"
        var retryCallCount = 0
        val retry = {
            retryCallCount += 1
        }
        val data = PlayHistoryNetworkState.FailureAtTop(errorMessage, retry).toViewData()
        assertEquals(errorMessage, data.errorMessage)
        assert(data.retryButtonVisible)
        assert(!data.loadingMessageVisible)
        assert(!data.progressBarVisible)
        assertNotNull(data.retry)
        // Check retry is setup correctly
        assertEquals(0, retryCallCount)
        data.retry!!()
        assertEquals(1, retryCallCount)
    }

    @Test
    fun `should convert state to view data correctly for failure from bottom`() {
        val errorMessage = "Hi this is a random error message"
        var retryCallCount = 0
        val retry = {
            retryCallCount += 1
        }
        val data = PlayHistoryNetworkState.FailureAtBottom(errorMessage, retry).toViewData()
        assertEquals(errorMessage, data.errorMessage)
        assert(data.retryButtonVisible)
        assert(!data.loadingMessageVisible)
        assert(!data.progressBarVisible)
        assertNotNull(data.retry)
        // Check retry is setup correctly
        assertEquals(0, retryCallCount)
        data.retry!!()
        assertEquals(1, retryCallCount)
    }

    @Test
    fun `should logout`() {
        val firebase = mock<FirebaseAPI>()
        val model = PlayHistoryFragmentViewModel(application, firebase)
        model.logout()
        verify(firebase).logout()
        model.loadingState
            .test()
            .assertHasValue()
            .assertValue { it.getContent() is PlayHistoryState.LoggedOut }
    }

    @Test
    fun `should convert state to view data correctly when no data available`() {
        val data = PlayHistoryNetworkState.NoRows.toViewData()
        assert(data.noDataMessageVisible)
        assert(!data.loadingMessageVisible)
        assert(!data.progressBarVisible)
        assertNull(data.errorMessage)
        assertNull(data.retry)
        assert(!data.retryButtonVisible)
    }
}