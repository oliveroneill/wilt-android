package com.oliveroneill.wilt.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.oliveroneill.wilt.R
import com.oliveroneill.wilt.databinding.HistoryScreenBinding

/**
 * Shows a user's Spotify play history
 */
class PlayHistoryFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<HistoryScreenBinding>(inflater, R.layout.history_screen,container, false)
        binding.loading = true
        return binding.root
    }
}
