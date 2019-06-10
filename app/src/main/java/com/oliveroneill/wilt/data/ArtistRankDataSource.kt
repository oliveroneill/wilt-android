package com.oliveroneill.wilt.data

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.ItemKeyedDataSource
import com.oliveroneill.wilt.Event
import com.oliveroneill.wilt.viewmodel.ArtistRank
import com.oliveroneill.wilt.viewmodel.PlayHistoryFragmentState

/**
 * Factory for creating page data sources for artist ranking
 */
class ArtistRankDataSourceFactory(private val loadingState: MutableLiveData<Event<PlayHistoryFragmentState>>):
    DataSource.Factory<Int, ArtistRank>() {
    override fun create(): DataSource<Int, ArtistRank> = ArtistRankDataSource(loadingState)
}

/**
 * Handles requesting pages of artist ranking information over periods of time
 *
 * TODO: I should store the results in Room and use that DataSource instead
 */
class ArtistRankDataSource(private val loadingState: MutableLiveData<Event<PlayHistoryFragmentState>>):
    ItemKeyedDataSource<Int, ArtistRank>() {
    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<ArtistRank>) {
        // TODO
        callback.onResult(listOf(ArtistRank("Feb 2019", "Tierra Whack", 0)))
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<ArtistRank>) {
        callback.onResult(listOf())
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<ArtistRank>) {
        callback.onResult(listOf())
    }

    override fun getKey(item: ArtistRank): Int = item.index
}
