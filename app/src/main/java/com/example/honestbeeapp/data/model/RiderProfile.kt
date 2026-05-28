package com.example.honestbeeapp.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class RiderProfile(
    val uid: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val vehicleType: String = "",
    val plateNumber: String = "",
    val driverLicenseSubmitted: Boolean = false,
    val driverLicenseFileName: String = "",
    val driverLicenseLocalOnly: Boolean = false,
    val driverLicenseUrl: String = "",
    val street: String = "",
    val barangay: String = "",
    val city: String = "",
    val province: String = "",
    val currentLocation: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val availabilityStatus: String = "Offline",
    val role: String = "rider",
    val status: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
