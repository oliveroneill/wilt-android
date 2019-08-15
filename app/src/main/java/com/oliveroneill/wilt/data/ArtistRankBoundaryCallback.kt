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
import java.time.DayOfWeek
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
     * All dates in the BigQuery database are in GMT timezone
     */
    private val gmtZone = ZoneId.of("GMT")
    /**
     * Keep track of whether the current week has been refreshed, so that we can update it once.
     * We can't always update it because we get stuck in a loop calling [onItemAtFrontLoaded].
     * This flag should be good enough to just update the current week once every time the app is started
     */
    private var refreshedCurrentWeek = false
    /**
     * We need to keep track of whether we're just refreshing the current week, so that we don't unnecessarily
     * try and load the next page
     */
    private var refreshingCurrentWeek = false

    /**
     * Database returned 0 items. We should query the backend for more items.
     */
    override fun onZeroItemsLoaded() {
        val endDate = LocalDate.now()
            // Ensure requests are always run from the start of the week to avoid missing
            // earlier plays
            .with(DayOfWeek.MONDAY)
            // The last date we'll request from is one week ahead of now. We add one week since otherwise the
            // query might not include the current week
            .plusWeeks(1)
        val end = endDate.atStartOfDay(gmtZone).toEpochSecond()
        // Each page is a week, so we subtract weeks to decide what to request
        // Increasing the page size seems to fix scrolling issues
        val weeksToRequest = pageSize * 2
        val startDate = endDate.minusWeeks(weeksToRequest)
        val start = startDate.atStartOfDay(gmtZone).toEpochSecond()
        topArtists(start, end, firstLoad = true)
    }

    /**
     * Load items that are more recent than [itemAtFront]
     */
    override fun onItemAtFrontLoaded(itemAtFront: ArtistRank) {
        // If we've just refreshed the current week then don't try and load another page
        if (refreshingCurrentWeek) {
            // Next load we can try again
            refreshingCurrentWeek = false
            return
        }
        // Ensure requests are always run from the start of the week to avoid missing
        // earlier plays
        val date = itemAtFront.date.with(DayOfWeek.MONDAY)
        // In most cases date will be the current week and we should refresh this since it will change before
        // the week ends. We'll only refresh this once to avoid constantly refreshing and after that
        // we'll skip this week
        val startDate = if (refreshedCurrentWeek) date.plusWeeks(1) else date
        // Indicate that we're refreshing the current week
        if (!refreshedCurrentWeek) refreshingCurrentWeek = true
        refreshedCurrentWeek = true
        val start = startDate.atStartOfDay(gmtZone).toEpochSecond()
        // Each page is a week, so we subtract weeks to decide what to request
        val endDate = startDate.plusWeeks(pageSize)
        val end = endDate.atStartOfDay(gmtZone).toEpochSecond()
        topArtists(start, end)
    }

    /**
     * Load items that are older than [itemAtEnd]
     */
    override fun onItemAtEndLoaded(itemAtEnd: ArtistRank) {
        // Ensure requests are always run from the start of
        // the week to avoid missing
        // earlier plays
        val date = itemAtEnd.date.with(DayOfWeek.MONDAY)
        // Subtract 1 week so that we don't include the week we've already got
        val endDate = date.minusWeeks(1)
        val end = endDate.atStartOfDay(gmtZone).toEpochSecond()
        // Each page is a week, so we subtract weeks to decide what to request
        val startDate = endDate.minusWeeks(pageSize)
        val start = startDate.atStartOfDay(gmtZone).toEpochSecond()
        topArtists(start, end, loadingFromTop = false)
    }

    private fun topArtists(start: Long, end: Long, firstLoad: Boolean = false, loadingFromTop: Boolean = true) {
        // Update state
        val state = if (loadingFromTop) {
            PlayHistoryNetworkState.LoadingFromTop
        } else {
            PlayHistoryNetworkState.LoadingFromBottom
        }
        loadingState.postValue(Data(PlayHistoryState.LoggedIn(state)))
        firebase.topArtistsPerWeek(start.toInt(), end.toInt()) { result ->
            result.onSuccess {
                if (firstLoad && it.isEmpty()) {
                    loadingState.postValue(
                        Data(
                            PlayHistoryState.LoggedIn(PlayHistoryNetworkState.NoRows)
                        )
                    )
                    return@onSuccess
                }
                // If the page size is greater than 1 then we've retrieved more than just the
                // current week, so there could be more data in the next page. Therefore
                // this isn't just a refresh of the current week
                if (it.size > 1) refreshingCurrentWeek = false
                executor.execute {
                    // We update the loading state before inserting to avoid jank from
                    // the inserted elements appearing above the loading spinner
                    loadingState.postValue(
                        Data(
                            PlayHistoryState.LoggedIn(PlayHistoryNetworkState.NotLoading)
                        )
                    )
                    dao.insert(it)
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
