package com.example.honestbeeapp.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class MerchantProfile(
    val uid: String = "",
    val email: String = "",
    val storeName: String = "",
    val ownerName: String = "",
    val phone: String = "",
    val address: String = "",
    val role: String = "merchant",
    val status: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
