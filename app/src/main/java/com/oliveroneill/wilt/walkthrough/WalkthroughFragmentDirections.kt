package com.oliveroneill.wilt.walkthrough

import android.os.Bundle
import androidx.navigation.NavDirections
import com.oliveroneill.wilt.R

/**
 * Navigation events
 */
class WalkthroughFragmentDirections {
    private class LoginError : NavDirections {
        override fun getArguments() = Bundle()
        override fun getActionId() = R.id.action_login_failure
    }

    companion object {
        fun showLoginError(): NavDirections = LoginError()
    }
}