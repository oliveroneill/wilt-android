package com.oliveroneill.wilt.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.oliveroneill.wilt.viewmodel.WalkthroughFragmentViewModel

/**
 * Pager adapter for walkthrough screens
 */
class WalkthroughPagerAdapter(private val viewModel: WalkthroughFragmentViewModel,
                              fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        val page = viewModel.pages[position]
        return WalkthroughScreenFragment.newInstance(
            page.imageResID,
            page.title
        )
    }

    override fun getCount(): Int = viewModel.pages.size
}
