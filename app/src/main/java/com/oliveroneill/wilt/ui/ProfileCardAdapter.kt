package com.oliveroneill.wilt.ui

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.oliveroneill.wilt.viewmodel.ProfileStateViewData

/**
 * Adapter for displaying profile card info
 */
class ProfileCardAdapter : RecyclerView.Adapter<ProfileCardViewHolder>() {
    /**
     * Store view data as it gets updated so that we know what to do when [onBindViewHolder] is called.
     * This will be populated when [updateItem] is called
     */
    private var itemStates: MutableMap<Int, ProfileStateViewData> = HashMap()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileCardViewHolder {
        return ProfileCardViewHolder.create(parent)
    }

    override fun getItemCount(): Int = 1

    override fun onBindViewHolder(holder: ProfileCardViewHolder, position: Int) {
        holder.bind(itemStates[position] ?: return)
    }

    /**
     * Call this when there is a new state for a specific item
     */
    fun updateItem(index: Int, viewData: ProfileStateViewData) {
        itemStates[index] = viewData
        notifyItemChanged(index)
    }
}
