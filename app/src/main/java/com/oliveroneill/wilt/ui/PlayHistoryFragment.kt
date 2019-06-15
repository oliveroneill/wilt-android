package com.oliveroneill.wilt.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.ViewModelProviders
import com.oliveroneill.wilt.EventObserver
import com.oliveroneill.wilt.R
import com.oliveroneill.wilt.databinding.HistoryScreenBinding
import com.oliveroneill.wilt.viewmodel.PlayHistoryFragmentViewModel

/**
 * Shows a user's Spotify play history
 */
class PlayHistoryFragment: Fragment() {
    // Specify a view model factory since this is useful for testing purposes
    @VisibleForTesting
    var viewModelFactory: AndroidViewModelFactory? = activity?.let {
        AndroidViewModelFactory(it.application)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<HistoryScreenBinding>(inflater, R.layout.history_screen,container, false)
        val rootView = binding.root
        val model = ViewModelProviders.of(this, viewModelFactory).get(PlayHistoryFragmentViewModel::class.java)
        val adapter = HistoryListAdapter()
        binding.historyList.adapter = adapter
        model.loadingState.observe(this, EventObserver {
            adapter.setNetworkState(it)
        })
        model.itemDataSource.observe(this, Observer {
            adapter.submitList(it)
        })
        return rootView
    }
}
