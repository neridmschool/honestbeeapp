package com.example.honestbeeapp.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class AndroidOrder(
    val orderId: String = "",
    val customerId: String = "",
    val merchantId: String = "",
    val riderId: String = "",
    val storeName: String = "",
    val customerName: String = "",
    val totalAmount: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val status: String = "",
    val paymentMethod: String = "",
    val deliveryAddress: String = "",
    val createdAt: Timestamp? = null,
    val items: List<AndroidCartItem> = emptyList()
)
