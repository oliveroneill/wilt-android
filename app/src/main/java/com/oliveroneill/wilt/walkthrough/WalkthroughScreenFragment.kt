package com.oliveroneill.wilt.walkthrough

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.oliveroneill.wilt.R
import kotlinx.android.synthetic.main.walkthrough_screen.view.*

/**
 * Individual screen used in walkthrough
 *
 * I've decided to keep this logic here instead of within a ViewModel as it's
 * mostly view setup related and seems unnecessary to move this logic to a view.
 */
class WalkthroughScreenFragment: Fragment() {
    // Optional views and data to be set
    private var image: ImageView? = null
    private var imageResId: Int? = null
    private var title: TextView? = null
    private var titleResId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve image and title from arguments set in newInstance() below
        imageResId = arguments?.getInt(IMG_ID)
        titleResId = arguments?.getInt(TITLE_ID)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.walkthrough_screen, container, false) as ViewGroup
        // Store the views that we'll display the relevant data on
        image = rootView.imageView
        title = rootView.textView
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Set the views using the relevant data. We'll return early if things are missing
        // TODO: maybe crashing makes more sense if things are unset
        title?.setText(titleResId ?: return)
        Glide.with(this)
            .load(imageResId ?: return)
            .centerCrop()
            .into(image ?: return)
    }

    companion object {
        private const val IMG_ID = "imgId"
        private const val TITLE_ID = "titleId"

        fun newInstance(imageResId: Int, titleResId: Int): WalkthroughScreenFragment {
            return WalkthroughScreenFragment().also {
                // Set arguments to be read by onCreate() above
                val args = Bundle()
                args.putInt(IMG_ID, imageResId)
                args.putInt(TITLE_ID, titleResId)
                it.arguments = args
            }
        }
    }
}
