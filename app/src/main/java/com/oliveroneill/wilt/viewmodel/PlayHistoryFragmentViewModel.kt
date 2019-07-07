package com.oliveroneill.wilt.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.oliveroneill.wilt.Event
import com.oliveroneill.wilt.data.ArtistRankBoundaryCallback
import com.oliveroneill.wilt.data.FirebaseAPI
import com.oliveroneill.wilt.data.dao.ArtistRank
import com.oliveroneill.wilt.data.dao.PlayHistoryDatabase
import com.oliveroneill.wilt.testing.OpenForTesting
import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed class PlayHistoryFragmentState {
    /**
     * If new rows are being loaded at the top of the list
     */
    object LoadingFromTop : PlayHistoryFragmentState()

    /**
     * If new rows are being loaded at the bottom of the list
     */
    object LoadingFromBottom : PlayHistoryFragmentState()

    /**
     * If nothing is currently loading
     */
    object NotLoading : PlayHistoryFragmentState()

    /**
     * If we failed to load the next page
     */
    data class FailureAtTop(val error: String, val retry: () -> Unit): PlayHistoryFragmentState()

    /**
     * If we failed to load the next page
     */
    data class FailureAtBottom(val error: String, val retry: () -> Unit): PlayHistoryFragmentState()
}

/**
 * ViewModel for PlayHistoryFragment
 *
 * Note that this ViewModel has a separated state between the network state and the artist rank data. This is not
 * ideal because it means any combination of row data and loading spinner is valid, however I'm not sure I can join
 * these two together without making the RecyclerView interactions more complicated.
 */
@OpenForTesting
class PlayHistoryFragmentViewModel @JvmOverloads constructor(application: Application, firebase: FirebaseAPI = FirebaseAPI()): AndroidViewModel(application) {
    private val _loadingState = MutableLiveData<Event<PlayHistoryFragmentState>>()
    /**
     * Used to display a loading spinner or error message while a new page is loaded
     */
    val loadingState : LiveData<Event<PlayHistoryFragmentState>>
        get() = _loadingState

    /**
     * Used by the RecyclerView to request new pages
     */
    val itemDataSource: LiveData<PagedList<ArtistRank>>
    init {
        val pageSize = 10
        // Get data from now onwards (back in time)
        val startDate = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
        // Create database
        PlayHistoryDatabase.getDatabase(application).historyDao().also {
            itemDataSource = it.loadPlayHistory(startDate).toLiveData(
                pageSize = pageSize,
                // This will be used to make network requests
                boundaryCallback = ArtistRankBoundaryCallback(it, firebase, _loadingState, pageSize.toLong())
            )
        }
    }
}
