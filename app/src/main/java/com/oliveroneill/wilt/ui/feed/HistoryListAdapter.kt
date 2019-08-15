package com.oliveroneill.wilt.ui.feed

import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.oliveroneill.wilt.R
import com.oliveroneill.wilt.data.dao.ArtistRank
import com.oliveroneill.wilt.viewmodel.PlayHistoryNetworkState

/**
 * Adapter for displaying artist play history
 *
 * TODO: there's a lot of logic in this class that should be moved to a ViewModel. I couldn't figure out a
 * clean way to do this
 */
class HistoryListAdapter : PagedListAdapter<ArtistRank, RecyclerView.ViewHolder>(ITEM_COMPARATOR) {
    private var state: PlayHistoryNetworkState? = null
    /**
     * The position index where the loading spinner is displayed, or null if it should not be displayed
     */
    private val loadingIndex
        get() = when(state) {
            is PlayHistoryNetworkState.NoRows -> 0
            is PlayHistoryNetworkState.LoadingFromTop -> 0
            is PlayHistoryNetworkState.FailureAtTop -> 0
            is PlayHistoryNetworkState.LoadingFromBottom -> itemCount - 1
            is PlayHistoryNetworkState.FailureAtBottom -> itemCount - 1
            else -> null
        }

    /**
     * Get the item position while handling the loading spinner. This is necessary since the spinner may push the views
     * down by one
     */
    private fun correctedPosition(position: Int): Int {
        return if (loadingIndex == 0) {
            // If the loading spinner is at the top then we should shift every element down.
            // Since the zeroth element is taken by the spinner, we want every corresponding element
            // to be the previous index in the list
            position - 1
        } else {
            position
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            R.layout.artist_rank -> (holder as ArtistRankViewHolder).bind(getItem(correctedPosition(position)))
            R.layout.network_state_item -> (holder as NetworkStateItemViewHolder).bind(state)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>) {
        onBindViewHolder(holder, position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.artist_rank -> ArtistRankViewHolder.create(parent)
            R.layout.network_state_item -> NetworkStateItemViewHolder.create(
                parent
            )
            else -> throw IllegalArgumentException("unknown view type $viewType")
        }
    }

    /**
     * We'll display an extra row for a loading spinner if we're loading or displaying an error
     */
    private fun hasExtraRow() = state != null && state != PlayHistoryNetworkState.NotLoading

    override fun getItemViewType(position: Int): Int {
        return if (hasExtraRow() && position == loadingIndex) {
            // Display the loading spinner or error message
            R.layout.network_state_item
        } else {
            R.layout.artist_rank
        }
    }

    override fun getItemCount() = super.getItemCount() + if (hasExtraRow()) 1 else 0

    fun setNetworkState(newState: PlayHistoryNetworkState?) {
        val previousState = this.state
        val hadExtraRow = hasExtraRow()
        val previousLoadingIndex = loadingIndex
        this.state = newState
        val hasExtraRow = hasExtraRow()
        // Notify the view if we need to add or remove a row
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                // Delete loading spinner
                notifyItemRemoved(previousLoadingIndex ?: return)
            } else {
                // Insert loading spinner
                notifyItemInserted(loadingIndex ?: return)
                // This should ensure that we scroll to the top and also push every element down by 1
                if (loadingIndex == 0) notifyDataSetChanged()
            }
        } else if (hasExtraRow && previousState != newState) {
            notifyItemChanged(loadingIndex ?: return)
        }
    }

    companion object {
        val ITEM_COMPARATOR = object : DiffUtil.ItemCallback<ArtistRank>() {
            override fun areContentsTheSame(oldItem: ArtistRank, newItem: ArtistRank) = oldItem == newItem

            override fun areItemsTheSame(oldItem: ArtistRank, newItem: ArtistRank) = oldItem.date == newItem.date
        }
    }
}
