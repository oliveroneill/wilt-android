package com.oliveroneill.wilt.ui.feed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.oliveroneill.wilt.R
import com.oliveroneill.wilt.data.dao.ArtistRank
import com.squareup.picasso.Picasso

/**
 * A view for displaying the most listened to artist for a specific period
 */
class ArtistRankViewHolder(private val view: View): RecyclerView.ViewHolder(view) {
    private val topArtistTextView: TextView = view.findViewById(R.id.top_artist)
    private val playsTextView: TextView = view.findViewById(R.id.plays_text)
    private val dateTextView: TextView = view.findViewById(R.id.date_text)
    private val imageView: ImageView = view.findViewById(R.id.artist_image)

    fun bind(item: ArtistRank?) {
        item?.let {
            topArtistTextView.text = it.topArtist
            playsTextView.text = view.context.getString(R.string.playstext_format, it.count)
            dateTextView.text = view.context.getString(R.string.datetext_format, it.date)
            Picasso.get()
                .load(it.imageUrl)
                .fit()
                .centerCrop()
                .transform(PicassoCircleTransform())
                .into(imageView)
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
