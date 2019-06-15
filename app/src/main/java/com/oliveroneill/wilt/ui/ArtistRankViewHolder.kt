package com.oliveroneill.wilt.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.oliveroneill.wilt.R
import com.oliveroneill.wilt.viewmodel.ArtistRank
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * A view for displaying the most listened to artist for a specific period
 */
class ArtistRankViewHolder(private val view: View): RecyclerView.ViewHolder(view) {
    private val topArtistTextView: TextView = view.findViewById(R.id.topArtist)
    private val subTextView: TextView = view.findViewById(R.id.subText)

    fun bind(item: ArtistRank?) {
        item?.let {
            topArtistTextView.text = it.top_artist
            // Parse date to be presented to UI
            val date = LocalDate.parse(it.date, DateTimeFormatter.ISO_DATE)
            subTextView.text = view.context.getString(R.string.subtext_format, it.count, date)
        }
    }

    companion object {
        fun create(parent: ViewGroup): ArtistRankViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.artist_rank, parent, false)
            return ArtistRankViewHolder(view)
        }
    }
}
