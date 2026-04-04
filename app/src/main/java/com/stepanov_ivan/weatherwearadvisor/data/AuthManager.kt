package com.stepanov_ivan.weatherwearadvisor.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser? get() = auth.currentUser

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    fun getFirebaseAuth(): FirebaseAuth = auth

    fun signOut() {
        auth.signOut()
    }
}
