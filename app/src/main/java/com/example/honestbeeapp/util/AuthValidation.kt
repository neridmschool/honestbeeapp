package com.example.honestbeeapp.util

import com.example.honestbeeapp.data.model.AuthMode

fun validateAuthInput(
    mode: AuthMode,
    email: String,
    password: String,
    displayName: String
): String? {
    if (!email.contains("@") || !email.contains(".")) return "Enter a valid email address."
    if (password.length < 6) return "Password must be at least 6 characters."
    if (mode == AuthMode.Register && displayName.isBlank()) return "Enter your name."
    return null
}
