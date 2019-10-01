package com.oliveroneill.wilt.ui.onboarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.oliveroneill.wilt.viewmodel.OnboardingFragmentViewModel

/**
 * Pager adapter for onboarding screens
 */
class OnboardingPagerAdapter(private val viewModel: OnboardingFragmentViewModel,
                             fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        val page = viewModel.pages[position]
        return OnboardingScreenFragment.newInstance(
            page.imageResID,
            page.title
        )
    }

    override fun getCount(): Int = viewModel.pages.size
}
