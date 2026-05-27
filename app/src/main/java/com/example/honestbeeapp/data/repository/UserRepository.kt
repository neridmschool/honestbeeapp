package com.example.honestbeeapp.data.repository

import com.example.honestbeeapp.data.model.AppUser
import com.example.honestbeeapp.data.model.CustomerProfile
import com.example.honestbeeapp.data.model.MerchantProfile
import com.example.honestbeeapp.data.model.RiderProfile
import com.example.honestbeeapp.util.FirebaseConstants
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun createUser(user: AppUser) {
        firestore.collection(FirebaseConstants.USERS)
            .document(user.uid)
            .set(user, SetOptions.merge())
            .await()
    }

    suspend fun getUser(uid: String): AppUser? {
        return firestore.collection(FirebaseConstants.USERS)
            .document(uid)
            .get()
            .await()
            .toObject(AppUser::class.java)
    }

    suspend fun updateUser(
        uid: String,
        updates: Map<String, Any>
    ) {
        val safeUpdates = updates.toMutableMap()
        safeUpdates["updatedAt"] = FieldValue.serverTimestamp()

        firestore.collection(FirebaseConstants.USERS)
            .document(uid)
            .update(safeUpdates)
            .await()
    }

    suspend fun updateUser(user: AppUser) {
        firestore.collection(FirebaseConstants.USERS)
            .document(user.uid)
            .set(user, SetOptions.merge())
            .await()
    }

    suspend fun updateUserStatus(
        uid: String,
        status: String
    ) {
        updateUser(
            uid = uid,
            updates = mapOf("status" to status)
        )
    }

    suspend fun createCustomerProfile(profile: CustomerProfile) {
        firestore.collection(FirebaseConstants.CUSTOMERS)
            .document(profile.uid)
            .set(profile, SetOptions.merge())
            .await()
    }

    suspend fun createMerchantProfile(profile: MerchantProfile) {
        firestore.collection(FirebaseConstants.MERCHANTS)
            .document(profile.uid)
            .set(profile, SetOptions.merge())
            .await()
    }

    suspend fun createRiderProfile(profile: RiderProfile) {
        firestore.collection(FirebaseConstants.RIDERS)
            .document(profile.uid)
            .set(profile, SetOptions.merge())
            .await()
    }

    suspend fun getAllUsers(): List<AppUser> {
        return firestore.collection(FirebaseConstants.USERS)
            .get()
            .await()
            .toObjects(AppUser::class.java)
    }

    suspend fun getPendingMerchantAndRiderUsers(): List<AppUser> {
        return firestore.collection(FirebaseConstants.USERS)
            .whereEqualTo("status", FirebaseConstants.STATUS_PENDING)
            .whereIn(
                "role",
                listOf(
                    FirebaseConstants.ROLE_MERCHANT,
                    FirebaseConstants.ROLE_RIDER
                )
            )
            .get()
            .await()
            .toObjects(AppUser::class.java)
    }
}
