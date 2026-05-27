package com.example.honestbeeapp.data.model

sealed class SessionState {
    object Loading : SessionState()
    object LoadingProfile : SessionState()
    object SignedOut : SessionState()
    data class SignedIn(val profile: SessionProfile) : SessionState()
    data class Blocked(val profile: SessionProfile) : SessionState()
    data class Error(val message: String) : SessionState()
}
