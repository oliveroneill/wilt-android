package com.oliveroneill.wilt.ui.feed

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
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.oliveroneill.wilt.EventObserver
import com.oliveroneill.wilt.R
import com.oliveroneill.wilt.databinding.HistoryFragmentBinding
import com.oliveroneill.wilt.viewmodel.PlayHistoryFragmentViewModel
import com.oliveroneill.wilt.viewmodel.PlayHistoryState
import kotlinx.android.synthetic.main.history_fragment.view.*



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
        val binding = DataBindingUtil.inflate<HistoryFragmentBinding>(
            inflater, R.layout.history_fragment, container, false
        )
        val rootView = binding.root
        val model = ViewModelProviders.of(this, viewModelFactory).get(PlayHistoryFragmentViewModel::class.java)
        val adapter = HistoryListAdapter()
        rootView.swipe_refresh.setOnRefreshListener {
            model.itemDataSource.value?.dataSource?.invalidate()
        }
        binding.historyList.adapter = adapter
        model.loadingState.observe(this, EventObserver {
            when (it) {
                is PlayHistoryState.LoggedIn -> {
                    adapter.setNetworkState(it.state)
                }
                is PlayHistoryState.LoggedOut -> {
                    NavHostFragment.findNavController(this).navigate(
                        PlayHistoryFragmentDirections.logout()
                    )
                }
            }

        })
        model.itemDataSource.observe(this, Observer {
            rootView.swipe_refresh.isRefreshing = false
            adapter.submitList(it)
        })
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.bottom_navigation.setupWithNavController(
            NavHostFragment.findNavController(this)
        )
    }
}
