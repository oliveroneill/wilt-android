package com.oliveroneill.wilt.ui.walkthrough

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.oliveroneill.wilt.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.walkthrough_screen.view.*

/**
 * Individual screen used in walkthrough
 *
 * I've decided to keep this logic here instead of within a ViewModel as it's
 * mostly related to setting up views and storing the strings and images in another class
 * has no benefit that I can see.
 */
class WalkthroughScreenFragment: Fragment() {
    // Optional views and data to be set
    private var image: ImageView? = null
    private var imageResId: Int? = null
    private var titleView: TextView? = null
    private var title: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve image and title from arguments set in newInstance() below
        imageResId = arguments?.getInt(IMG_ID)
        title = arguments?.getString(TITLE_ID)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.walkthrough_screen, container, false) as ViewGroup
        // Store the views that we'll display the relevant data on
        image = rootView.image_view
        titleView = rootView.text_view
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Set the views using the relevant data. We'll return early if things are missing
        // TODO: maybe crashing makes more sense if things are unset
        titleView?.text = (title ?: return)
        Picasso.get()
            .load(imageResId ?: return)
            .fit()
            .centerCrop()
            .into(image ?: return)
    }

    companion object {
        private const val IMG_ID = "imgId"
        private const val TITLE_ID = "titleId"

        fun newInstance(imageResId: Int, title: String): WalkthroughScreenFragment {
            return WalkthroughScreenFragment().also {
                // Set arguments to be read by onCreate() above
                val args = Bundle()
                args.putInt(IMG_ID, imageResId)
                args.putString(TITLE_ID, title)
                it.arguments = args
            }
        }
    }
}
