package com.oliveroneill.wilt.ui

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

    private class LoggedIn : NavDirections {
        override fun getArguments() = Bundle()
        override fun getActionId() = R.id.action_login
    }

    companion object {
        fun showLoginError(): NavDirections = LoginError()
        fun login(): NavDirections = LoggedIn()
    }
}