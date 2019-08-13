package com.oliveroneill.wilt.ui.walkthrough

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.oliveroneill.wilt.MessageObserver
import com.oliveroneill.wilt.R
import com.oliveroneill.wilt.data.SpotifyAuthenticationResponse
import com.oliveroneill.wilt.databinding.WalkthroughFragmentBinding
import com.oliveroneill.wilt.viewmodel.WalkthroughFragmentState
import com.oliveroneill.wilt.viewmodel.WalkthroughFragmentViewModel
import com.spotify.sdk.android.authentication.AuthenticationClient
import kotlinx.android.synthetic.main.walkthrough_fragment.view.*

/**
 * Fragment that displays pager of walkthrough screens
 */
class WalkthroughFragment: Fragment() {
    companion object {
        // Random number for checking that login response comes from matching request
        private const val SPOTIFY_REQUEST_CODE: Int = 7253
    }

    // Specify a view model factory since this is useful for testing purposes
    @VisibleForTesting
    var viewModelFactory: AndroidViewModelFactory? = activity?.let { AndroidViewModelFactory(it.application) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        val binding = DataBindingUtil.inflate<WalkthroughFragmentBinding>(
            inflater,
            R.layout.walkthrough_fragment,
            container,
            false
        )
        setupNavigation()
        val rootView = binding.root
        rootView.view_pager.offscreenPageLimit = 2
        val model = ViewModelProviders.of(
            this, viewModelFactory
        ).get(WalkthroughFragmentViewModel::class.java)
        // Set adapter
        childFragmentManager.let {
            rootView.view_pager.adapter = WalkthroughPagerAdapter(model, it)
        }
        model.state.observe(viewLifecycleOwner, MessageObserver {
            when (it) {
                is WalkthroughFragmentState.Walkthrough -> {
                    binding.loading = false
                }
                is WalkthroughFragmentState.AuthenticatingSpotify -> {
                    binding.loading = true
                    // Start login activity
                    AuthenticationClient.openLoginActivity(
                        activity,
                        SPOTIFY_REQUEST_CODE,
                        it.request.toAuthenticationRequest()
                    )
                }
                is WalkthroughFragmentState.LoggedIn -> {
                    findNavController(this).navigate(
                        WalkthroughFragmentDirections.showPlayHistory()
                    )
                }
                is WalkthroughFragmentState.LoginError -> {
                    findNavController(this).navigate(
                        WalkthroughFragmentDirections.showLoginError()
                    )
                }
            }
        })
        // On click of login button notify the ViewModel
        rootView.sign_in_button.setOnClickListener {
            model.spotifySignup()
        }
        return rootView
    }

    /**
     * I wish the navigation graph could do this for me
     */
    private fun setupNavigation() {
        // Set up the action bar so that the title is set via the Navigation graph
        (activity as? AppCompatActivity)?.let {
            // Set the fragments that cannot have a back button. The play history fragment should not have a back
            // button so that you cannot return to the walkthrough
            val appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.walkthroughFragment, R.id.navigation_feed, R.id.navigation_profile
                )
            )
            NavigationUI.setupActionBarWithNavController(it, findNavController(this), appBarConfiguration)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != SPOTIFY_REQUEST_CODE) return
        // This will be called due to Spotify authentication response
        val model = ViewModelProviders.of(this, viewModelFactory).get(WalkthroughFragmentViewModel::class.java)
        val response = AuthenticationClient.getResponse(resultCode, data)
        // Notify ViewModel
        model.onSpotifyLoginResponse(
            SpotifyAuthenticationResponse.fromAuthenticationResponse(
                response
            )
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.logged_out_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_info -> {
            findNavController(this).navigate(
                WalkthroughFragmentDirections.showInfo()
            )
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }
}
