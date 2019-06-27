package com.oliveroneill.wilt.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.oliveroneill.wilt.R
import com.oliveroneill.wilt.viewmodel.PlayHistoryFragmentState

/**
 * View for displaying a loading spinner or error message based on the state
 */
class NetworkStateItemViewHolder(val view: View): RecyclerView.ViewHolder(view) {
    private val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
    private val retry = view.findViewById<Button>(R.id.retry_button)
    private val errorMessage = view.findViewById<TextView>(R.id.error_txt)
    private val loadingMessage = view.findViewById<TextView>(R.id.loading_txt)

    fun bind(state: PlayHistoryFragmentState?) {
        when (state) {
            is PlayHistoryFragmentState.Failure -> {
                progressBar.visibility = View.GONE
                loadingMessage.visibility = View.GONE
                retry.visibility = View.VISIBLE
                errorMessage.visibility = View.VISIBLE
                errorMessage.text = state.error
                retry.setOnClickListener {
                    state.retry()
                }
            }
            is PlayHistoryFragmentState.LoadingMore -> {
                progressBar.visibility = View.VISIBLE
                loadingMessage.visibility = View.VISIBLE
                retry.visibility = View.GONE
                errorMessage.visibility = View.GONE
            }
            is PlayHistoryFragmentState.NotLoading -> {
                progressBar.visibility = View.GONE
                loadingMessage.visibility = View.GONE
                retry.visibility = View.GONE
                errorMessage.visibility = View.GONE
            }
        }
    }

    companion object {
        fun create(parent: ViewGroup): NetworkStateItemViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.network_state_item, parent, false)
            return NetworkStateItemViewHolder(view)
        }
    }
}
