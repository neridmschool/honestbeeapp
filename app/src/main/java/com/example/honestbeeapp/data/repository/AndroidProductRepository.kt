package com.example.honestbeeapp.data.repository

import com.example.honestbeeapp.data.model.AndroidProduct
import com.example.honestbeeapp.util.FirebaseConstants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AndroidProductRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val products = firestore.collection(FirebaseConstants.ANDROID_PRODUCTS)

    suspend fun createProduct(product: AndroidProduct): String {
        val document = if (product.productId.isBlank()) products.document() else products.document(product.productId)
        document.set(product.copy(productId = document.id)).await()
        return document.id
    }

    suspend fun getProduct(productId: String): AndroidProduct? {
        return products.document(productId)
            .get()
            .await()
            .toObject(AndroidProduct::class.java)
    }

    suspend fun getProductsByStore(storeId: String): List<AndroidProduct> {
        return products.whereEqualTo("storeId", storeId)
            .get()
            .await()
            .toObjects(AndroidProduct::class.java)
    }

    suspend fun getAllProducts(): List<AndroidProduct> {
        return products.get()
            .await()
            .toObjects(AndroidProduct::class.java)
    }

    suspend fun updateProduct(
        productId: String,
        updates: Map<String, Any>
    ) {
        products.document(productId).update(updates).await()
    }

    suspend fun deleteProduct(productId: String) {
        products.document(productId).delete().await()
    }
}
