package com.oliveroneill.wilt.ui

import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.oliveroneill.wilt.R
import com.oliveroneill.wilt.viewmodel.ArtistRank
import com.oliveroneill.wilt.viewmodel.PlayHistoryFragmentState

/**
 * Adapter for displaying artist play history
 */
class HistoryListAdapter(private val retryCallback: () -> Unit)
    : PagedListAdapter<ArtistRank, RecyclerView.ViewHolder>(ITEM_COMPARATOR) {
    private var state: PlayHistoryFragmentState? = null
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            R.layout.artist_rank -> (holder as ArtistRankViewHolder).bind(getItem(position))
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
            R.layout.network_state_item -> NetworkStateItemViewHolder.create(parent, retryCallback)
            else -> throw IllegalArgumentException("unknown view type $viewType")
        }
    }

    /**
     * We'll display an extra row for a loading spinner if we're loading or displaying an error
     */
    private fun hasExtraRow() = state != null && state != PlayHistoryFragmentState.NotLoading

    override fun getItemViewType(position: Int): Int {
        return if (hasExtraRow() && position == itemCount - 1) {
            // Display the loading spinner or error message
            R.layout.network_state_item
        } else {
            R.layout.artist_rank
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasExtraRow()) 1 else 0
    }

    fun setNetworkState(newState: PlayHistoryFragmentState?) {
        val previousState = this.state
        val hadExtraRow = hasExtraRow()
        this.state = newState
        val hasExtraRow = hasExtraRow()
        // Notify the view if we need to add or remove a row
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(super.getItemCount())
            } else {
                notifyItemInserted(super.getItemCount())
            }
        } else if (hasExtraRow && previousState != newState) {
            notifyItemChanged(itemCount - 1)
        }
    }

    companion object {
        val ITEM_COMPARATOR = object : DiffUtil.ItemCallback<ArtistRank>() {
            override fun areContentsTheSame(oldItem: ArtistRank, newItem: ArtistRank): Boolean =
                oldItem == newItem

            override fun areItemsTheSame(oldItem: ArtistRank, newItem: ArtistRank): Boolean =
                oldItem.date == newItem.date
        }
    }
}
