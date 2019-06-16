package com.oliveroneill.wilt.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.toLiveData
import com.oliveroneill.wilt.Event
import com.oliveroneill.wilt.data.ArtistRankDataSourceFactory
import com.oliveroneill.wilt.data.FirebaseAPI
import com.oliveroneill.wilt.testing.OpenForTesting
import java.time.LocalDate

/**
 * The data to be displayed per row in the view
 */
@OpenForTesting
data class ArtistRank(val date: String, val top_artist: String, val count: Int)

sealed class PlayHistoryFragmentState {
    /**
     * If new rows are being loaded
     */
    object LoadingMore : PlayHistoryFragmentState()

    /**
     * If nothing is currently loading
     */
    object NotLoading : PlayHistoryFragmentState()

    /**
     * If we failed to load the next page
     */
    data class Failure(val error: String, val retry: () -> Unit): PlayHistoryFragmentState()
}

/**
 * ViewModel for PlayHistoryFragment
 *
 * Note that this ViewModel has a separated state between the network state and the artist rank data. This is not
 * ideal because it means any combination of row data and loading spinner is valid, however I'm not sure I can join
 * these two together without making the RecyclerView interactions more complicated.
 */
@OpenForTesting
class PlayHistoryFragmentViewModel(firebase: FirebaseAPI = FirebaseAPI()): ViewModel() {
    private val _loadingState = MutableLiveData<Event<PlayHistoryFragmentState>>()
    /**
     * Used to display a loading spinner or error message while a new page is loaded
     */
    val loadingState : LiveData<Event<PlayHistoryFragmentState>>
        get() = _loadingState

    /**
     * Used by the RecyclerView to request new pages
     */
    val itemDataSource = ArtistRankDataSourceFactory(_loadingState, firebase).toLiveData(
        initialLoadKey = LocalDate.now(), pageSize = 10
    )
}
