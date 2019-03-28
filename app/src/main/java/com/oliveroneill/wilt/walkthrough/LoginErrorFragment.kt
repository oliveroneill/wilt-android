package com.oliveroneill.wilt.walkthrough

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.oliveroneill.wilt.R

/**
 * Page shown for login error
 */
class LoginErrorFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.login_error, container, false) as ViewGroup
    }
}
