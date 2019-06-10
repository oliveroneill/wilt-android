package com.oliveroneill.wilt.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.oliveroneill.wilt.R
import com.oliveroneill.wilt.viewmodel.ArtistRank

/**
 * A view for displaying the most listened to artist for a specific period
 */
class ArtistRankViewHolder(view: View): RecyclerView.ViewHolder(view) {
    private val topArtistTextView: TextView = view.findViewById(R.id.topArtist)

    fun bind(item: ArtistRank?) {
        topArtistTextView.text = item?.topArtist ?: "Loading..."
    }

    companion object {
        fun create(parent: ViewGroup): ArtistRankViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.artist_rank, parent, false)
            return ArtistRankViewHolder(view)
        }
    }
}
