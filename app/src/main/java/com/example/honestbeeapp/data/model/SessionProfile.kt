package com.example.honestbeeapp.data.model

import com.example.honestbeeapp.util.FirebaseConstants

data class SessionProfile(
    val uid: String,
    val email: String,
    val displayName: String,
    val role: UserRole,
    val status: String
) {
    fun toSessionState(): SessionState {
        return if (status == FirebaseConstants.STATUS_ACTIVE) {
            SessionState.SignedIn(this)
        } else {
            SessionState.Blocked(this)
        }
    }
}
