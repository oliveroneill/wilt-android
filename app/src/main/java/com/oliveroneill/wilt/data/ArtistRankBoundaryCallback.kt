package com.oliveroneill.wilt.data

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.oliveroneill.wilt.Event
import com.oliveroneill.wilt.data.dao.ArtistRank
import com.oliveroneill.wilt.data.dao.PlayHistoryDao
import com.oliveroneill.wilt.viewmodel.PlayHistoryFragmentState
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class ArtistRankBoundaryCallback(
    private val dao: PlayHistoryDao,
    private val firebase: FirebaseAPI,
    private val loadingState: MutableLiveData<Event<PlayHistoryFragmentState>>,
    private val pageSize: Long,
    private val executor: Executor = Executors.newSingleThreadExecutor()
): PagedList.BoundaryCallback<ArtistRank>() {
    /**
     * Database returned 0 items. We should query the backend for more items.
     */
    override fun onZeroItemsLoaded() {
        // Convert request to timestamps. Use now if no date was specified
        // We ignore the params initial requested key since it's just a hint
        val endDate = LocalDate.now()
        val end = endDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        // Each page is a week, so we subtract weeks to decide what to request
        val startDate = endDate.minusWeeks(pageSize)
        val start = startDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        topArtists(start, end)
    }

    /**
     * Load items that are more recent than [itemAtFront]
     */
    override fun onItemAtFrontLoaded(itemAtFront: ArtistRank) {
        val date = LocalDate.parse(itemAtFront.date)
        // Convert request to timestamps
        // Subtract 1 week so that we don't include the week we've already got
        val endDate = date.minusWeeks(1)
        val end = endDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        // Each page is a week, so we subtract weeks to decide what to request
        val startDate = endDate.minusWeeks(pageSize)
        val start = startDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        topArtists(start, end)
    }

    /**
     * Load items that are older than [itemAtEnd]
     */
    override fun onItemAtEndLoaded(itemAtEnd: ArtistRank) {
        val date = LocalDate.parse(itemAtEnd.date)
        // Convert request to timestamps
        // Add 1 week so that we don't include the week we've already got
        val startDate = date.plusWeeks(1)
        val start = startDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        // Each page is a week, so we subtract weeks to decide what to request
        val endDate = startDate.plusWeeks(pageSize)
        val end = endDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        topArtists(start, end)
    }

    private fun topArtists(start: Long, end: Long) {
        // Update state
        loadingState.postValue(Event(PlayHistoryFragmentState.LoadingMore))
        firebase.topArtists(start.toInt(), end.toInt()) { result ->
            result.onSuccess {
                executor.execute {
                    dao.insert(it)
                    loadingState.postValue(Event(PlayHistoryFragmentState.NotLoading))
                }
            }.onFailure {
                loadingState.postValue(
                    Event(
                        PlayHistoryFragmentState.Failure(
                            it.localizedMessage
                            // Retry
                        ) { topArtists(start, end) }
                    )
                )
            }
        }
    }
}
