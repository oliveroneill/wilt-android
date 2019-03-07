package com.oliveroneill.wilt

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.oliveroneill.wilt.walkthrough.WalkthroughFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set initial fragment to walkthrough fragment
        supportFragmentManager.beginTransaction().let {
            it.add(R.id.fragmentPlaceHolder, WalkthroughFragment())
            it.commit()
        }
    }
}
