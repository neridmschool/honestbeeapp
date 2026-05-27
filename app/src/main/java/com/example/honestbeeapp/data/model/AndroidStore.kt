package com.example.honestbeeapp.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class AndroidStore(
    val storeId: String = "",
    val storeName: String = "",
    val category: String = "",
    val imageUrl: String = "",
    val address: String = "",
    val rating: Double = 0.0,
    val deliveryTime: String = "",
    val deliveryFee: Double = 0.0,
    val minimumOrder: Double = 0.0,
    val isOpen: Boolean = false
)
