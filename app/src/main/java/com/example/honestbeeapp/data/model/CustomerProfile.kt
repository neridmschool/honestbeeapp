package com.example.honestbeeapp.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class CustomerProfile(
    val uid: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val street: String = "",
    val barangay: String = "",
    val city: String = "",
    val province: String = "",
    val address: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val role: String = "customer",
    val status: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
