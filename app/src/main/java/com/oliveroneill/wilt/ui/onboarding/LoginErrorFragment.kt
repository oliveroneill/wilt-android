package com.oliveroneill.wilt.ui.onboarding

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.oliveroneill.wilt.R
import kotlinx.android.synthetic.main.login_error.view.*

/**
 * Page shown for login error
 */
class LoginErrorFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        val rootView = inflater.inflate(R.layout.login_error, container, false) as ViewGroup
        rootView.try_again_button.setOnClickListener {
            NavHostFragment.findNavController(this).popBackStack()
        }
        return rootView
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.logged_out_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_info -> {
            NavHostFragment.findNavController(this).navigate(
                OnboardingFragmentDirections.showInfo()
            )
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }
}
