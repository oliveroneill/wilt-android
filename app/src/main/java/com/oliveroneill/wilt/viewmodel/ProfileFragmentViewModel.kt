package com.oliveroneill.wilt.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.oliveroneill.wilt.Event
import com.oliveroneill.wilt.data.FirebaseAuthentication
import com.oliveroneill.wilt.testing.OpenForTesting

@OpenForTesting
class ProfileFragmentViewModel @JvmOverloads constructor(
    application: Application, firebase: FirebaseAuthentication = FirebaseAuthentication(application)
): AndroidViewModel(application) {
    private val _loginState = MutableLiveData<Event<ProfileLogInState>>()
    val loginState : LiveData<Event<ProfileLogInState>>
        get() = _loginState
    init {
        val profileName = firebase.currentUser
        if (profileName == null) {
            _loginState.postValue(Event(ProfileLogInState.LoggedOut))
        } else {
            _loginState.postValue(Event(ProfileLogInState.LoggedIn(profileName)))
        }
    }
}

sealed class ProfileLogInState {
    data class LoggedIn(val profileName: String) : ProfileLogInState()
    object LoggedOut : ProfileLogInState()
}
