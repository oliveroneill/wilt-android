package com.oliveroneill.wilt.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.oliveroneill.wilt.R
import com.oliveroneill.wilt.databinding.ProfileCardBinding
import com.oliveroneill.wilt.viewmodel.ProfileCardViewData

/**
 * Converts [ProfileCardViewData] to a profile card view
 */
class ProfileCardViewHolder(private val binding: ProfileCardBinding): RecyclerView.ViewHolder(binding.root) {
    /**
     * Update the view to match the view data
     */
    fun bind(viewData: ProfileCardViewData) {
        binding.loading = viewData.loading
        binding.lastListened = viewData.lastListenedText
        binding.favouriteArtist = viewData.artistName
        binding.plays = viewData.playText
    }

    companion object {
        fun create(parent: ViewGroup): ProfileCardViewHolder {
            val binding = DataBindingUtil.inflate<ProfileCardBinding>(
                LayoutInflater.from(parent.context),
                R.layout.profile_card,
                parent,
                false
            )
            return ProfileCardViewHolder(binding)
        }
    }
}
