package com.example.honestbeeapp.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class AndroidCartItem(
    val cartItemId: String = "",
    val productId: String = "",
    val storeId: String = "",
    val productName: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val imageUrl: String = "",
    val subtotal: Double = 0.0
)
