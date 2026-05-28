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
    val storeName: String = "",
    val ownerName: String = "",
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
    val vehicleType: String = "",
    val openingTime: String = "",
    val closingTime: String = "",
    val businessPermitSubmitted: Boolean = false,
    val businessPermitFileName: String = "",
    val businessPermitLocalOnly: Boolean = false,
    val businessPermitUrl: String = "",
    val driverLicenseSubmitted: Boolean = false,
    val driverLicenseFileName: String = "",
    val driverLicenseLocalOnly: Boolean = false,
    val driverLicenseUrl: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
