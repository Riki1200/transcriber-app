package com.romeodev.features.auth.presentation.states

import com.romeodev.features.auth.domain.models.UserData


data class AuthState(
    // common
    val user: UserData = UserData(),
    val email: String = "",
    val emailError: String = "",
    val password: String = "",
    val passwordError: String = "",
    val isLoading: Boolean = false,
    val signedIn: Boolean = false,
    val signOutWithGoogle: Boolean = false,
    // change pw screen
    val currentPassword: String = "",
    val currentPasswordError: String = "",
    val newPassword: String = "",
    val newPasswordError: String = "",
    val deletePassword: String = "",
    val deletePasswordError: String = "",
    // signUp Screen
    val name: String = "",
    val nameError: String = "",
    val confirmPassword: String = "",
    val confirmPasswordError: String = "",
    // forget password
    val isForgetPasswordLoading: Boolean = false,
)
