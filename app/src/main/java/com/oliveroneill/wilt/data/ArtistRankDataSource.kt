package com.oliveroneill.wilt.data

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.ItemKeyedDataSource
import com.oliveroneill.wilt.Event
import com.oliveroneill.wilt.viewmodel.ArtistRank
import com.oliveroneill.wilt.viewmodel.PlayHistoryFragmentState
import java.time.LocalDate
import java.time.ZoneId

/**
 * Factory for creating page data sources for artist ranking
 */
class ArtistRankDataSourceFactory(
    private val loadingState: MutableLiveData<Event<PlayHistoryFragmentState>>,
    private val firebase: FirebaseAPI
): DataSource.Factory<LocalDate, ArtistRank>() {
    override fun create(): DataSource<LocalDate, ArtistRank> = ArtistRankDataSource(loadingState, firebase)
}

/**
 * Handles requesting pages of artist ranking information over periods of time
 *
 * TODO: I should store the results in Room and use that DataSource instead
 */
class ArtistRankDataSource(
    private val loadingState: MutableLiveData<Event<PlayHistoryFragmentState>>,
    private val firebase: FirebaseAPI
): ItemKeyedDataSource<LocalDate, ArtistRank>() {
    override fun loadInitial(params: LoadInitialParams<LocalDate>, callback: LoadInitialCallback<ArtistRank>) {
        // Convert request to timestamps. Use now if no date was specified
        val endDate = params.requestedInitialKey ?: LocalDate.now()
        val end = endDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        // Each page is a month, so we subtract months to decide what to request
        val startDate = endDate.minusMonths(params.requestedLoadSize.toLong())
        val start = startDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        // Update state
        loadingState.postValue(Event(PlayHistoryFragmentState.LoadingMore))
        firebase.topArtists(start.toInt(), end.toInt()) {
            it.onSuccess {
                callback.onResult(it)
                loadingState.postValue(Event(PlayHistoryFragmentState.NotLoading))
            }.onFailure {
                loadingState.postValue(Event(PlayHistoryFragmentState.Failure(it.localizedMessage)))
            }
        }
    }

    override fun loadAfter(params: LoadParams<LocalDate>, callback: LoadCallback<ArtistRank>) {
        callback.onResult(listOf())
    }

    override fun loadBefore(params: LoadParams<LocalDate>, callback: LoadCallback<ArtistRank>) {
        callback.onResult(listOf())
    }

    override fun getKey(item: ArtistRank): LocalDate = LocalDate.parse(item.date)
}
