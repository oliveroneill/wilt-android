package com.oliveroneill.wilt.ui

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.oliveroneill.wilt.viewmodel.ProfileCardViewData

/**
 * Adapter for displaying profile card info
 */
class ProfileCardAdapter : RecyclerView.Adapter<ProfileCardViewHolder>() {
    /**
     * Store view data as it gets updated so that we know what to do when [onBindViewHolder] is called.
     * This will be populated when [updateCards] is called
     */
    private var itemStates: List<ProfileCardViewData> = listOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileCardViewHolder {
        return ProfileCardViewHolder.create(parent)
    }

    override fun getItemCount(): Int = itemStates.size

    override fun onBindViewHolder(holder: ProfileCardViewHolder, position: Int) {
        if (position >= itemStates.size) return
        holder.bind(itemStates[position])
    }

    /**
     * Call this when there is a new state for a specific item
     */
    fun updateCards(viewData: List<ProfileCardViewData>) {
        itemStates = viewData
        // TODO: DiffUtil
        notifyDataSetChanged()
    }
}
