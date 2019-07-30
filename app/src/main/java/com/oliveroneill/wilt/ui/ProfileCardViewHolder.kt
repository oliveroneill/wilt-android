package com.oliveroneill.wilt.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.oliveroneill.wilt.R
import com.oliveroneill.wilt.databinding.ProfileCardBinding
import com.oliveroneill.wilt.viewmodel.ProfileCardViewData
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.profile_card.view.*

/**
 * Converts [ProfileCardViewData] to a profile card view
 */
class ProfileCardViewHolder(private val binding: ProfileCardBinding): RecyclerView.ViewHolder(binding.root) {
    /**
     * Update the view to match the view data
     */
    fun bind(viewData: ProfileCardViewData) {
        binding.loading = viewData.loading
        binding.tagTitle = viewData.tagTitle
        binding.subtitle1 = viewData.subtitleFirstLine
        binding.title = viewData.title
        binding.subtitle2 = viewData.subtitleSecondLine
        // TODO: this is definitely something the viewmodel should control but it seems ugly
        // to have an "isVisible" flag for every view...
        binding.displayingError = viewData.errorMessage != null
        binding.errorMessage = viewData.errorMessage
        binding.root.retry_button.setOnClickListener {
            viewData.retry?.invoke()
        }
        viewData.imageUrl?.let {
            Picasso.get()
                .load(it)
                .fit()
                .centerCrop()
                .into(binding.root.imageView)
        }
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
