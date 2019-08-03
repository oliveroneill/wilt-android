package com.oliveroneill.wilt.data

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.google.firebase.functions.FirebaseFunctionsException
import com.oliveroneill.wilt.Data
import com.oliveroneill.wilt.Event
import com.oliveroneill.wilt.Message
import com.oliveroneill.wilt.data.dao.ArtistRank
import com.oliveroneill.wilt.data.dao.PlayHistoryDao
import com.oliveroneill.wilt.viewmodel.PlayHistoryNetworkState
import com.oliveroneill.wilt.viewmodel.PlayHistoryState
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class ArtistRankBoundaryCallback(
    private val dao: PlayHistoryDao,
    private val firebase: FirebaseAPI,
    private val loadingState: MutableLiveData<Message<PlayHistoryState>>,
    private val pageSize: Long,
    private val executor: Executor = Executors.newSingleThreadExecutor()
): PagedList.BoundaryCallback<ArtistRank>() {
    /**
     * Database returned 0 items. We should query the backend for more items.
     */
    override fun onZeroItemsLoaded() {
        // The last date we'll request from is one week ahead of now. We add one week since otherwise the
        // query might not include the current week
        val endDate = LocalDate.now().plusWeeks(1)
        val end = endDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        // Each page is a week, so we subtract weeks to decide what to request
        // Increasing the page size seems to fix scrolling issues
        val weeksToRequest = pageSize * 2
        val startDate = endDate.minusWeeks(weeksToRequest)
        val start = startDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        topArtists(start, end)
    }

    /**
     * Load items that are more recent than [itemAtFront]
     */
    override fun onItemAtFrontLoaded(itemAtFront: ArtistRank) {
        val date = itemAtFront.date
        // Convert request to timestamps
        // Add 1 week so that we don't include the week we've already got
        val startDate = date.plusWeeks(1)
        val start = startDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        // Each page is a week, so we subtract weeks to decide what to request
        val endDate = startDate.plusWeeks(pageSize)
        val end = endDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        topArtists(start, end)
    }

    /**
     * Load items that are older than [itemAtEnd]
     */
    override fun onItemAtEndLoaded(itemAtEnd: ArtistRank) {
        val date = itemAtEnd.date
        // Convert request to timestamps
        // Subtract 1 week so that we don't include the week we've already got
        val endDate = date.minusWeeks(1)
        val end = endDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        // Each page is a week, so we subtract weeks to decide what to request
        val startDate = endDate.minusWeeks(pageSize)
        val start = startDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        topArtists(start, end, loadingFromTop = false)
    }

    private fun topArtists(start: Long, end: Long, loadingFromTop: Boolean = true) {
        // Update state
        val state = if (loadingFromTop) {
            PlayHistoryNetworkState.LoadingFromTop
        } else {
            PlayHistoryNetworkState.LoadingFromBottom
        }
        loadingState.postValue(Data(PlayHistoryState.LoggedIn(state)))
        firebase.topArtistsPerWeek(start.toInt(), end.toInt()) { result ->
            result.onSuccess {
                executor.execute {
                    dao.insert(it)
                    loadingState.postValue(
                        Data(
                            PlayHistoryState.LoggedIn(PlayHistoryNetworkState.NotLoading)
                        )
                    )
                }
            }.onFailure {
                // Check whether we've logged out
                if (it is FirebaseFunctionsException &&
                    it.code == FirebaseFunctionsException.Code.UNAUTHENTICATED) {
                    // Send back a logged out error
                    loadingState.postValue(Event(PlayHistoryState.LoggedOut))
                    // Short circuit
                    return@onFailure
                }
                val failure = if (loadingFromTop) {
                    PlayHistoryNetworkState.FailureAtTop(
                        it.localizedMessage
                        // Retry
                    ) { topArtists(start, end) }
                } else {
                    PlayHistoryNetworkState.FailureAtBottom(
                        it.localizedMessage
                        // Retry
                    ) { topArtists(start, end) }
                }
                loadingState.postValue(Data(PlayHistoryState.LoggedIn(failure)))
            }
        }
    }
}
