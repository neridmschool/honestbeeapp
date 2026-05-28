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
    val openingTime: String = "",
    val closingTime: String = "",
    val businessPermitSubmitted: Boolean = false,
    val businessPermitFileName: String = "",
    val businessPermitLocalOnly: Boolean = false,
    val businessPermitUrl: String = "",
    val street: String = "",
    val barangay: String = "",
    val city: String = "",
    val province: String = "",
    val address: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val role: String = "merchant",
    val status: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
