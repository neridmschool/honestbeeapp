package com.example.honestbeeapp.data.repository

import com.example.honestbeeapp.data.model.AndroidCartItem
import com.example.honestbeeapp.util.FirebaseConstants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AndroidCartRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val cart = firestore.collection(FirebaseConstants.ANDROID_CART)

    suspend fun addCartItem(item: AndroidCartItem): String {
        val document = if (item.cartItemId.isBlank()) cart.document() else cart.document(item.cartItemId)
        document.set(item.copy(cartItemId = document.id)).await()
        return document.id
    }

    suspend fun getCartItem(cartItemId: String): AndroidCartItem? {
        return cart.document(cartItemId)
            .get()
            .await()
            .toObject(AndroidCartItem::class.java)
    }

    suspend fun getAllCartItems(): List<AndroidCartItem> {
        return cart.get()
            .await()
            .toObjects(AndroidCartItem::class.java)
    }

    suspend fun updateCartItem(
        cartItemId: String,
        updates: Map<String, Any>
    ) {
        cart.document(cartItemId).update(updates).await()
    }

    suspend fun removeCartItem(cartItemId: String) {
        cart.document(cartItemId).delete().await()
    }
}
