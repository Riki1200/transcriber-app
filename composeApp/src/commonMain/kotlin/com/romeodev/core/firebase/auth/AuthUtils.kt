package com.romeodev.core.firebase.auth

import com.romeodev.core.AppConstants
import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth


object AuthUtils {
    val isLoggedIn: Boolean by lazy {
        Firebase.auth.currentUser != null
    }

    val firebaseUserId by lazy {
        Firebase.auth.currentUser!!.uid
    }

    val firebaseUserIdOrNull by lazy {
        Firebase.auth.currentUser?.uid
    }

    fun initGoogleAuthProvider(){
        GoogleAuthProvider.create(credentials = GoogleAuthCredentials(serverId = AppConstants.GOOGLE_WEB_CLIENT_ID))
    }
}