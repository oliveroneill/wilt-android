package com.oliveroneill.wilt.ui.onboarding

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
import com.oliveroneill.wilt.databinding.OnboardingFragmentBinding
import com.oliveroneill.wilt.viewmodel.OnboardingFragmentState
import com.oliveroneill.wilt.viewmodel.OnboardingFragmentViewModel
import com.spotify.sdk.android.authentication.AuthenticationClient
import kotlinx.android.synthetic.main.onboarding_fragment.view.*

/**
 * Fragment that displays pager of onboarding screens
 */
class OnboardingFragment: Fragment() {
    companion object {
        // Random number for checking that login response comes from matching request
        private const val SPOTIFY_REQUEST_CODE: Int = 7253
    }

    // Specify a view model factory since this is useful for testing purposes
    @VisibleForTesting
    var viewModelFactory: AndroidViewModelFactory? = activity?.let { AndroidViewModelFactory(it.application) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        val binding = DataBindingUtil.inflate<OnboardingFragmentBinding>(
            inflater,
            R.layout.onboarding_fragment,
            container,
            false
        )
        setupNavigation()
        val rootView = binding.root
        rootView.view_pager.offscreenPageLimit = 2
        val model = ViewModelProviders.of(
            this, viewModelFactory
        ).get(OnboardingFragmentViewModel::class.java)
        // Set adapter
        childFragmentManager.let {
            rootView.view_pager.adapter = OnboardingPagerAdapter(model, it)
        }
        model.state.observe(viewLifecycleOwner, MessageObserver {
            when (it) {
                is OnboardingFragmentState.Onboarding -> {
                    binding.loading = false
                }
                is OnboardingFragmentState.AuthenticatingSpotify -> {
                    binding.loading = true
                    // Start login activity
                    AuthenticationClient.openLoginActivity(
                        activity,
                        SPOTIFY_REQUEST_CODE,
                        it.request.toAuthenticationRequest()
                    )
                }
                is OnboardingFragmentState.LoggedIn -> {
                    findNavController(this).navigate(
                        OnboardingFragmentDirections.showPlayHistory()
                    )
                }
                is OnboardingFragmentState.LoginError -> {
                    findNavController(this).navigate(
                        OnboardingFragmentDirections.showLoginError()
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
            // button so that you cannot return to onboarding
            val appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.onboardingFragment, R.id.navigation_feed, R.id.navigation_profile
                )
            )
            NavigationUI.setupActionBarWithNavController(it, findNavController(this), appBarConfiguration)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != SPOTIFY_REQUEST_CODE) return
        // This will be called due to Spotify authentication response
        val model = ViewModelProviders.of(this, viewModelFactory).get(OnboardingFragmentViewModel::class.java)
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
                OnboardingFragmentDirections.showInfo()
            )
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }
}
