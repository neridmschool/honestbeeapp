package com.example.honestbeeapp.data.repository

import com.example.honestbeeapp.data.model.AndroidOrder
import com.example.honestbeeapp.util.FirebaseConstants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AndroidOrderRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val orders = firestore.collection(FirebaseConstants.ANDROID_ORDERS)

    suspend fun createOrder(order: AndroidOrder): String {
        val document = if (order.orderId.isBlank()) orders.document() else orders.document(order.orderId)
        document.set(order.copy(orderId = document.id)).await()
        return document.id
    }

    suspend fun getOrder(orderId: String): AndroidOrder? {
        return orders.document(orderId)
            .get()
            .await()
            .toObject(AndroidOrder::class.java)
    }

    suspend fun getOrdersForCustomer(customerId: String): List<AndroidOrder> {
        return orders.whereEqualTo("customerId", customerId)
            .get()
            .await()
            .toObjects(AndroidOrder::class.java)
    }

    suspend fun getOrdersForMerchant(merchantId: String): List<AndroidOrder> {
        return orders.whereEqualTo("merchantId", merchantId)
            .get()
            .await()
            .toObjects(AndroidOrder::class.java)
    }

    suspend fun getOrdersForRider(riderId: String): List<AndroidOrder> {
        return orders.whereEqualTo("riderId", riderId)
            .get()
            .await()
            .toObjects(AndroidOrder::class.java)
    }

    suspend fun updateOrder(
        orderId: String,
        updates: Map<String, Any>
    ) {
        orders.document(orderId).update(updates).await()
    }

    suspend fun updateOrderStatus(
        orderId: String,
        status: String
    ) {
        updateOrder(
            orderId = orderId,
            updates = mapOf("status" to status)
        )
    }

    suspend fun deleteOrder(orderId: String) {
        orders.document(orderId).delete().await()
    }
}
