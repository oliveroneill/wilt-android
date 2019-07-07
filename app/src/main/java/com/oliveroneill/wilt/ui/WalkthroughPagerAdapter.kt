package com.oliveroneill.wilt.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.oliveroneill.wilt.R

/**
 * Pager adapter for walkthrough screens
 */
class WalkthroughPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    companion object {
        // Images for each screen
        private val IMAGE_RES_IDS = intArrayOf(
            R.drawable.walkthrough1,
            R.drawable.walkthrough2
        )
        // Subtitle for each screen
        private val TITLES_RES_IDS = intArrayOf(
            R.string.walkthrough1_text,
            R.string.walkthrough2_text
        )
    }

    override fun getItem(position: Int): Fragment {
        return WalkthroughScreenFragment.newInstance(
            IMAGE_RES_IDS[position],
            TITLES_RES_IDS[position]
        )
    }

    override fun getCount(): Int = IMAGE_RES_IDS.size
}
