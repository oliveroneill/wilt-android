package com.oliveroneill.wilt.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.oliveroneill.wilt.EventObserver
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
        val rootView = inflater.inflate(R.layout.walkthrough_fragment, container, false) as ViewGroup
        val binding = DataBindingUtil.inflate<WalkthroughFragmentBinding>(
            inflater,
            R.layout.walkthrough_fragment,
            container,
            false
        )
        rootView.viewPager.offscreenPageLimit = 2
        // Set adapter
        childFragmentManager.let {
            rootView.viewPager.adapter = WalkthroughPagerAdapter(it)
        }
        val model = ViewModelProviders.of(this, viewModelFactory).get(WalkthroughFragmentViewModel::class.java)
        model.state.observe(this, EventObserver {
            when (it) {
                is WalkthroughFragmentState.LoggingIn -> {
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
        rootView.signInButton.setOnClickListener {
            model.spotifySignup()
        }
        return rootView
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
}
