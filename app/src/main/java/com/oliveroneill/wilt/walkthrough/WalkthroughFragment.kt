package com.oliveroneill.wilt.walkthrough

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.oliveroneill.wilt.R
import com.spotify.sdk.android.authentication.AuthenticationClient
import kotlinx.android.synthetic.main.walkthrough_fragment.view.*

/**
 * Fragment that displays pager of walkthrough screens
 */
class WalkthroughFragment: Fragment() {
    // Random number for checking that login response comes from matching request
    private val SPOTIFY_REQUEST_CODE: Int = 7253

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.walkthrough_fragment, container, false) as ViewGroup
        rootView.viewPager.offscreenPageLimit = 2
        // Set adapter
        childFragmentManager.let {
            rootView.viewPager.adapter = WalkthroughPagerAdapter(it)
        }
        val model = ViewModelProviders.of(this).get(WalkthroughFragmentViewModel::class.java)
        model.state.observe(this, Observer {
            when (it) {
                is WalkthroughFragmentState.LoggingIn -> {
                    // Start login activity
                    AuthenticationClient.openLoginActivity(
                        activity,
                        SPOTIFY_REQUEST_CODE,
                        it.request.toAuthenticationRequest()
                    );
                }
                is WalkthroughFragmentState.LoggedIn -> {
                    // TODO
                }
                is WalkthroughFragmentState.LoginError -> {
                    findNavController(this).navigate(R.id.action_login_failure)
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
        if (requestCode != SPOTIFY_REQUEST_CODE) return;
        // This will be called due to Spotify authentication response
        val model = ViewModelProviders.of(this).get(WalkthroughFragmentViewModel::class.java)
        val response = AuthenticationClient.getResponse(resultCode, data)
        // Notify ViewModel
        model.onSpotifyLoginResponse(SpotifyAuthenticationResponse.fromAuthenticationResponse(response))
    }
}
