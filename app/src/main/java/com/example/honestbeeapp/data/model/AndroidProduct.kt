package com.example.honestbeeapp.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class AndroidProduct(
    val productId: String = "",
    val storeId: String = "",
    val productName: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val stock: Int = 0,
    val unit: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val isAvailable: Boolean = false
)
