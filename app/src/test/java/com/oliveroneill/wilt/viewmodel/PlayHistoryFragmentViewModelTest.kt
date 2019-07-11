package com.oliveroneill.wilt.viewmodel

import junit.framework.TestCase.*
import org.junit.Test

class PlayHistoryFragmentViewModelTest {
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
}