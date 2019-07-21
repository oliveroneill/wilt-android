package com.oliveroneill.wilt

import android.app.Application
import com.google.firebase.FirebaseApp

class WiltApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialise our libraries
        FirebaseApp.initializeApp(this)
    }
}
