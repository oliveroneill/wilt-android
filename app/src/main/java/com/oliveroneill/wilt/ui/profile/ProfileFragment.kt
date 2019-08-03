package com.oliveroneill.wilt.ui.profile

import android.os.Bundle
import android.view.*
import androidx.annotation.VisibleForTesting
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.oliveroneill.wilt.MessageObserver
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
    private lateinit var model: ProfileFragmentViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        val binding = DataBindingUtil.inflate<ProfileFragmentBinding>(
            inflater,
            R.layout.profile_fragment,
            container,
            false
        )
        val adapter = ProfileCardAdapter()
        model = ViewModelProviders.of(this, viewModelFactory).get(ProfileFragmentViewModel::class.java)
        binding.profileInfoList.adapter = adapter
        model.state.observe(this, MessageObserver {
            when (it) {
                is ProfileState.LoggedIn -> {
                    val context = context ?: return@MessageObserver
                    val state = it.state
                    // Update the profile name. This is independent of a card
                    binding.profileName = state.profileName
                    val viewData = it.state.cards.map { card ->
                        card.toViewData(context)
                    }
                    adapter.updateCards(viewData)
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_logout -> {
            model.logout()
            true
        }
        R.id.action_info -> {
            NavHostFragment.findNavController(this).navigate(
                ProfileFragmentDirections.showInfo()
            )
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }
}
