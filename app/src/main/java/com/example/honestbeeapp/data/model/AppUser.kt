package com.example.honestbeeapp.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class AppUser(
    val uid: String = "",
    val email: String = "",
    val username: String = "",
    val role: String = "",
    val status: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val address: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
