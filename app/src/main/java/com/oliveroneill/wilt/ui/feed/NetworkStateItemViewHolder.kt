package com.oliveroneill.wilt.ui.feed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.oliveroneill.wilt.R
import com.oliveroneill.wilt.viewmodel.PlayHistoryNetworkState

/**
 * View for displaying a loading spinner or error message based on the state
 */
class NetworkStateItemViewHolder(val view: View): RecyclerView.ViewHolder(view) {
    private val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
    private val retry = view.findViewById<Button>(R.id.retry_button)
    private val errorMessage = view.findViewById<TextView>(R.id.error_txt)
    private val loadingMessage = view.findViewById<TextView>(R.id.loading_txt)
    private val noDataMessage = view.findViewById<TextView>(R.id.no_data_txt)

    fun bind(state: PlayHistoryNetworkState?) {
        // Convert state into view data and then modify the view to match
        state?.toViewData()?.let { data ->
            progressBar.visibility = if (data.progressBarVisible) View.VISIBLE else View.GONE
            loadingMessage.visibility = if (data.loadingMessageVisible) View.VISIBLE else View.GONE
            retry.visibility = if (data.retryButtonVisible) View.VISIBLE else View.GONE
            noDataMessage.visibility = if (data.noDataMessageVisible) View.VISIBLE else View.GONE
            data.errorMessage?.let { message ->
                errorMessage.visibility = View.VISIBLE
                errorMessage.text = message
            } ?: run {
                // If the error message is null then it should not be displayed
                errorMessage.visibility = View.GONE
            }
            retry.setOnClickListener {
                data.retry?.invoke()
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
