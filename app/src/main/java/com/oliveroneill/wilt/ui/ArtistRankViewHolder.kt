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
    private val playsTextView: TextView = view.findViewById(R.id.playsText)
    private val dateTextView: TextView = view.findViewById(R.id.dateText)

    fun bind(item: ArtistRank?) {
        item?.let {
            topArtistTextView.text = it.top_artist
            // Parse date to be presented to UI
            val date = LocalDate.parse(it.date, DateTimeFormatter.ISO_DATE)
            playsTextView.text = view.context.getString(R.string.playstext_format, it.count)
            dateTextView.text = view.context.getString(R.string.datetext_format, date)
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
