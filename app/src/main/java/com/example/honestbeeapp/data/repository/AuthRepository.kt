package com.example.honestbeeapp.data.repository

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    suspend fun login(
        email: String,
        password: String
    ): FirebaseUser? {
        return auth.signInWithEmailAndPassword(email, password)
            .await()
            .user
    }

    suspend fun register(
        email: String,
        password: String
    ): FirebaseUser? {
        return auth.createUserWithEmailAndPassword(email, password)
            .await()
            .user
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    suspend fun updatePassword(
        currentPassword: String,
        newPassword: String
    ) {
        val user = auth.currentUser ?: throw IllegalStateException("Please sign in again before changing your password.")
        val email = user.email?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("This account has no email address for password verification.")

        val credential = EmailAuthProvider.getCredential(email, currentPassword)
        user.reauthenticate(credential).await()
        user.updatePassword(newPassword).await()
    }
}
