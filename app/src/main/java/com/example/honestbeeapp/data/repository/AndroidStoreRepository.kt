package com.example.honestbeeapp.data.repository

import com.example.honestbeeapp.data.model.AndroidStore
import com.example.honestbeeapp.util.FirebaseConstants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AndroidStoreRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val stores = firestore.collection(FirebaseConstants.ANDROID_STORES)

    suspend fun createStore(store: AndroidStore): String {
        val document = if (store.storeId.isBlank()) stores.document() else stores.document(store.storeId)
        document.set(store.copy(storeId = document.id)).await()
        return document.id
    }

    suspend fun getStore(storeId: String): AndroidStore? {
        return stores.document(storeId)
            .get()
            .await()
            .toObject(AndroidStore::class.java)
    }

    suspend fun getAllStores(): List<AndroidStore> {
        return stores.get()
            .await()
            .toObjects(AndroidStore::class.java)
    }

    suspend fun updateStore(
        storeId: String,
        updates: Map<String, Any>
    ) {
        stores.document(storeId).update(updates).await()
    }

    suspend fun deleteStore(storeId: String) {
        stores.document(storeId).delete().await()
    }
}
