package com.oliveroneill.wilt.ui.walkthrough

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.oliveroneill.wilt.R
import kotlinx.android.synthetic.main.login_error.view.*

/**
 * Page shown for login error
 */
class LoginErrorFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.login_error, container, false) as ViewGroup
        rootView.tryAgainButton.setOnClickListener {
            NavHostFragment.findNavController(this).popBackStack()
        }
        return rootView
    }
}
