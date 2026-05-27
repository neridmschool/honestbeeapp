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

    suspend fun updateCustomerProfile(
        uid: String,
        email: String,
        firstName: String,
        lastName: String,
        phone: String,
        address: String
    ) {
        val displayName = "$firstName $lastName".trim()
        val userUpdates = mapOf(
            "uid" to uid,
            "email" to email,
            "firstName" to firstName,
            "lastName" to lastName,
            "username" to displayName,
            "phone" to phone,
            "address" to address,
            "role" to FirebaseConstants.ROLE_CUSTOMER,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        val customerUpdates = mapOf(
            "uid" to uid,
            "email" to email,
            "firstName" to firstName,
            "lastName" to lastName,
            "phone" to phone,
            "address" to address,
            "role" to FirebaseConstants.ROLE_CUSTOMER,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        firestore.runBatch { batch ->
            batch.set(
                firestore.collection(FirebaseConstants.USERS).document(uid),
                userUpdates,
                SetOptions.merge()
            )
            batch.set(
                firestore.collection(FirebaseConstants.CUSTOMERS).document(uid),
                customerUpdates,
                SetOptions.merge()
            )
        }.await()
    }

    suspend fun updateCustomerAddress(
        uid: String,
        address: String
    ) {
        val updates = mapOf(
            "address" to address,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        firestore.runBatch { batch ->
            batch.set(
                firestore.collection(FirebaseConstants.USERS).document(uid),
                updates,
                SetOptions.merge()
            )
            batch.set(
                firestore.collection(FirebaseConstants.CUSTOMERS).document(uid),
                updates,
                SetOptions.merge()
            )
        }.await()
    }

    suspend fun updateCustomerSettings(
        uid: String,
        settings: Map<String, Any>
    ) {
        saveAndroidSettings(FirebaseConstants.ANDROID_CUSTOMER_SETTINGS, uid, settings)
    }

    suspend fun updateMerchantProfile(
        uid: String,
        email: String,
        storeName: String,
        ownerName: String,
        phone: String,
        address: String
    ) {
        val userUpdates = mapOf(
            "uid" to uid,
            "email" to email,
            "username" to ownerName.ifBlank { storeName },
            "phone" to phone,
            "address" to address,
            "role" to FirebaseConstants.ROLE_MERCHANT,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        val merchantUpdates = mapOf(
            "uid" to uid,
            "email" to email,
            "storeName" to storeName,
            "ownerName" to ownerName,
            "phone" to phone,
            "address" to address,
            "role" to FirebaseConstants.ROLE_MERCHANT,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        firestore.runBatch { batch ->
            batch.set(
                firestore.collection(FirebaseConstants.USERS).document(uid),
                userUpdates,
                SetOptions.merge()
            )
            batch.set(
                firestore.collection(FirebaseConstants.MERCHANTS).document(uid),
                merchantUpdates,
                SetOptions.merge()
            )
        }.await()
    }

    suspend fun updateMerchantSettings(
        uid: String,
        settings: Map<String, Any>
    ) {
        saveAndroidSettings(FirebaseConstants.ANDROID_MERCHANT_SETTINGS, uid, settings)
    }

    suspend fun updateMerchantBankInfo(
        uid: String,
        bankInfo: Map<String, Any>
    ) {
        saveAndroidSettings(FirebaseConstants.ANDROID_MERCHANT_SETTINGS, uid, bankInfo)
    }

    suspend fun updateRiderProfile(
        uid: String,
        email: String,
        firstName: String,
        lastName: String,
        phone: String
    ) {
        val displayName = "$firstName $lastName".trim()
        val userUpdates = mapOf(
            "uid" to uid,
            "email" to email,
            "firstName" to firstName,
            "lastName" to lastName,
            "username" to displayName,
            "phone" to phone,
            "role" to FirebaseConstants.ROLE_RIDER,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        val riderUpdates = mapOf(
            "uid" to uid,
            "email" to email,
            "firstName" to firstName,
            "lastName" to lastName,
            "phone" to phone,
            "role" to FirebaseConstants.ROLE_RIDER,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        firestore.runBatch { batch ->
            batch.set(
                firestore.collection(FirebaseConstants.USERS).document(uid),
                userUpdates,
                SetOptions.merge()
            )
            batch.set(
                firestore.collection(FirebaseConstants.RIDERS).document(uid),
                riderUpdates,
                SetOptions.merge()
            )
        }.await()
    }

    suspend fun updateRiderVehicleInfo(
        uid: String,
        vehicleType: String,
        plateNumber: String,
        currentLocation: String
    ) {
        val updates = mapOf(
            "vehicleType" to vehicleType,
            "plateNumber" to plateNumber,
            "currentLocation" to currentLocation,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        firestore.collection(FirebaseConstants.RIDERS)
            .document(uid)
            .set(updates, SetOptions.merge())
            .await()
    }

    suspend fun updateRiderAvailability(
        uid: String,
        availabilityStatus: String
    ) {
        firestore.collection(FirebaseConstants.RIDERS)
            .document(uid)
            .set(
                mapOf(
                    "availabilityStatus" to availabilityStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
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

    private suspend fun saveAndroidSettings(
        collection: String,
        uid: String,
        values: Map<String, Any>
    ) {
        val updates = values.toMutableMap()
        updates["uid"] = uid
        updates["updatedAt"] = FieldValue.serverTimestamp()

        firestore.collection(collection)
            .document(uid)
            .set(updates, SetOptions.merge())
            .await()
    }
}
