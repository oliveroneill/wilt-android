package com.oliveroneill.wilt.ui.onboarding

import android.os.Bundle
import androidx.navigation.NavDirections
import com.oliveroneill.wilt.R

/**
 * Navigation events
 */
class OnboardingFragmentDirections {
    private class LoginError : NavDirections {
        override fun getActionId() = R.id.action_login_failure
        override fun getArguments() = Bundle()
        override fun equals(other: Any?) = (other is LoginError)
        override fun hashCode() = javaClass.hashCode()
    }

    private class LogIn : NavDirections {
        override fun getActionId() = R.id.action_login
        override fun getArguments() = Bundle()
        override fun equals(other: Any?) = (other is LogIn)
        override fun hashCode() = javaClass.hashCode()
    }

    private class InfoNav : NavDirections {
        override fun getActionId() = R.id.action_info
        override fun getArguments() = Bundle()
        override fun equals(other: Any?) = (other is InfoNav)
        override fun hashCode() = javaClass.hashCode()
    }

    companion object {
        fun showLoginError(): NavDirections =
            LoginError()
        fun showPlayHistory(): NavDirections = LogIn()
        fun showInfo(): NavDirections = InfoNav()
    }
}