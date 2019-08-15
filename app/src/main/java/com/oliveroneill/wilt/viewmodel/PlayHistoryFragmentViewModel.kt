package com.oliveroneill.wilt.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.oliveroneill.wilt.Event
import com.oliveroneill.wilt.Message
import com.oliveroneill.wilt.data.ArtistRankBoundaryCallback
import com.oliveroneill.wilt.data.FirebaseAPI
import com.oliveroneill.wilt.data.dao.ArtistRank
import com.oliveroneill.wilt.data.dao.PlayHistoryDatabase
import com.oliveroneill.wilt.testing.OpenForTesting

/**
 * The data necessary to display the network state for this view model.
 * By default nothing is displayed
 */
data class PlayHistoryNetworkStateViewData(
    val loadingMessageVisible: Boolean = false,
    val progressBarVisible: Boolean = false,
    val retryButtonVisible: Boolean = false,
    val noDataMessageVisible: Boolean = false,
    // If the error message is null then it should not be displayed
    val errorMessage: String? = null,
    val retry: (() -> Unit)? = null
)

/**
 * The state of the view model. There's a hierarchy here, since there's a lot of states
 * but only if the user is logged in
 */
sealed class PlayHistoryState {
    data class LoggedIn(val state: PlayHistoryNetworkState) : PlayHistoryState()
    object LoggedOut: PlayHistoryState()
}

sealed class PlayHistoryNetworkState {
    /**
     * If new rows are being loaded at the top of the list
     */
    object NoRows : PlayHistoryNetworkState()

    /**
     * If new rows are being loaded at the top of the list
     */
    object LoadingFromTop : PlayHistoryNetworkState()

    /**
     * If new rows are being loaded at the bottom of the list
     */
    object LoadingFromBottom : PlayHistoryNetworkState()

    /**
     * If nothing is currently loading
     */
    object NotLoading : PlayHistoryNetworkState()

    /**
     * If we failed to load the next page
     */
    data class FailureAtTop(val error: String, val retry: () -> Unit): PlayHistoryNetworkState()

    /**
     * If we failed to load the next page
     */
    data class FailureAtBottom(val error: String, val retry: () -> Unit): PlayHistoryNetworkState()

    /**
     * Convert state into a set of necessary data for displaying the view
     */
    fun toViewData(): PlayHistoryNetworkStateViewData {
        return when (this) {
            is PlayHistoryNetworkState.FailureAtBottom -> {
                PlayHistoryNetworkStateViewData(
                    retryButtonVisible = true, errorMessage = error, retry = retry
                )
            }
            is PlayHistoryNetworkState.FailureAtTop -> {
                PlayHistoryNetworkStateViewData(
                    retryButtonVisible = true, errorMessage = error, retry = retry
                )
            }
            is PlayHistoryNetworkState.LoadingFromBottom, is PlayHistoryNetworkState.LoadingFromTop -> {
                PlayHistoryNetworkStateViewData(
                    progressBarVisible = true, loadingMessageVisible = true
                )
            }
            is PlayHistoryNetworkState.NotLoading -> {
                PlayHistoryNetworkStateViewData(
                    progressBarVisible = true, loadingMessageVisible = true
                )
            }
            is NoRows -> {
                PlayHistoryNetworkStateViewData(noDataMessageVisible = true)
            }
        }
    }
}

/**
 * ViewModel for PlayHistoryFragment
 *
 * Note that this ViewModel has a separated state between the network state and the artist rank data. This is not
 * ideal because it means any combination of row data and loading spinner is valid, however I'm not sure I can join
 * these two together without making the RecyclerView interactions more complicated.
 */
@OpenForTesting
class PlayHistoryFragmentViewModel @JvmOverloads constructor(
    application: Application, private val firebase: FirebaseAPI = FirebaseAPI()
): AndroidViewModel(application) {
    private val _loadingState = MutableLiveData<Message<PlayHistoryState>>()
    /**
     * Used to display a loading spinner or error message while a new page is loaded
     */
    val loadingState : LiveData<Message<PlayHistoryState>>
        get() = _loadingState

    /**
     * Used by the RecyclerView to request new pages
     */
    val itemDataSource: LiveData<PagedList<ArtistRank>>
    init {
        val pageSize = 10
        // Create database
        PlayHistoryDatabase.getDatabase(application).historyDao().also {
            itemDataSource = it.loadPlayHistory().toLiveData(
                pageSize = pageSize,
                // This will be used to make network requests
                boundaryCallback = ArtistRankBoundaryCallback(it, firebase, _loadingState, pageSize.toLong())
            )
        }
    }

    fun logout() {
        firebase.logout()
        _loadingState.postValue(Event(PlayHistoryState.LoggedOut))
    }
}
