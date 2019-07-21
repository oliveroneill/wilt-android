package com.oliveroneill.wilt.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.oliveroneill.wilt.EventObserver
import com.oliveroneill.wilt.R
import com.oliveroneill.wilt.databinding.ProfileFragmentBinding
import com.oliveroneill.wilt.viewmodel.ProfileFragmentViewModel
import com.oliveroneill.wilt.viewmodel.ProfileState
import kotlinx.android.synthetic.main.history_fragment.view.*

class ProfileFragment: Fragment() {
    // Specify a view model factory since this is useful for testing purposes
    @VisibleForTesting
    var viewModelFactory: ViewModelProvider.AndroidViewModelFactory? = activity?.let {
        ViewModelProvider.AndroidViewModelFactory(it.application)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<ProfileFragmentBinding>(
            inflater,
            R.layout.profile_fragment,
            container,
            false
        )
        val model = ViewModelProviders.of(this, viewModelFactory).get(ProfileFragmentViewModel::class.java)
        model.state.observe(this, EventObserver {
            when (it) {
                is ProfileState.LoggedIn -> {
                    val context = context ?: return@EventObserver
                    val viewData = it.networkState.toViewData(context)
                    binding.loading = viewData.loading
                    binding.profileName = viewData.profileName
                    binding.lastListened = viewData.lastListenedText
                    binding.favouriteArtist = viewData.artistName
                    binding.plays = viewData.playText
                }
                is ProfileState.LoggedOut -> {
                    NavHostFragment.findNavController(this).navigate(
                        ProfileFragmentDirections.logout()
                    )
                }
            }
        })
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.bottom_navigation.setupWithNavController(
            NavHostFragment.findNavController(this)
        )
    }
}
