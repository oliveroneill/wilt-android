package com.oliveroneill.wilt.walkthrough

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.oliveroneill.wilt.R
import kotlinx.android.synthetic.main.walkthrough_fragment.view.*

/**
 * Fragment that displays pager of walkthrough screens
 */
class WalkthroughFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.walkthrough_fragment, container, false) as ViewGroup
        rootView.viewPager.offscreenPageLimit = 2
        // Set adapter
        activity?.supportFragmentManager?.let {
            rootView.viewPager.adapter = WalkthroughPagerAdapter(it)
        }
        rootView.signInButton.setOnClickListener {
            // TODO: use viewmodel
        }
        return rootView
    }
}
