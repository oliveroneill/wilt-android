package com.oliveroneill.wilt.ui.info

import android.os.Bundle
import android.text.Html
import android.text.Html.FROM_HTML_MODE_COMPACT
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.oliveroneill.wilt.R
import kotlinx.android.synthetic.main.info_fragment.view.*

class InfoFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.info_fragment, container, false) as ViewGroup
        rootView.info_text.text = Html.fromHtml(getString(R.string.info), FROM_HTML_MODE_COMPACT)
        rootView.info_text.movementMethod = LinkMovementMethod.getInstance()
        return rootView
    }
}
