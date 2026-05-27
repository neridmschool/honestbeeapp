package com.example.honestbeeapp.data.repository

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
}
