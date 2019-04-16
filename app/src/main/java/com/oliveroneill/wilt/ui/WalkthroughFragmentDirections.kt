package com.oliveroneill.wilt.ui

import android.os.Bundle
import androidx.navigation.NavDirections
import com.oliveroneill.wilt.R

/**
 * Navigation events
 */
class WalkthroughFragmentDirections {
    private class LoginError : NavDirections {
        override fun getActionId() = R.id.action_login_failure
        override fun getArguments() = Bundle()
        override fun equals(other: Any?) = (other is LoginError)
        override fun hashCode() = javaClass.hashCode()
    }

    private class LoggedIn : NavDirections {
        override fun getActionId() = R.id.action_login
        override fun getArguments() = Bundle()
        override fun equals(other: Any?) = (other is LoggedIn)
        override fun hashCode() = javaClass.hashCode()
    }

    companion object {
        fun showLoginError(): NavDirections = LoginError()
        fun login(): NavDirections = LoggedIn()
    }
}