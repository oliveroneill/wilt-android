package com.oliveroneill.wilt.ui

import android.os.Bundle
import androidx.navigation.NavDirections
import com.oliveroneill.wilt.R

/**
 * Navigation events
 */
class ProfileFragmentDirections {
    private class LogOut : NavDirections {
        override fun getActionId() = R.id.action_logout
        override fun getArguments() = Bundle()
        override fun equals(other: Any?) = (other is LogOut)
        override fun hashCode() = javaClass.hashCode()
    }

    companion object {
        fun logout(): NavDirections = LogOut()
    }
}
