package com.oliveroneill.wilt.ui.feed

import android.os.Bundle
import androidx.navigation.NavDirections
import com.oliveroneill.wilt.R

/**
 * Navigation events
 */
class PlayHistoryFragmentDirections {
    private class LogOut : NavDirections {
        override fun getActionId() = R.id.action_login
        override fun getArguments() = Bundle()
        override fun equals(other: Any?) = (other is LogOut)
        override fun hashCode() = javaClass.hashCode()
    }

    companion object {
        fun logout(): NavDirections = LogOut()
    }
}