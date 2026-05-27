package com.example.honestbeeapp.data.model

import com.example.honestbeeapp.util.FirebaseConstants

enum class UserRole(
    val id: String,
    val label: String,
    val profileCollection: String?
) {
    Customer(FirebaseConstants.ROLE_CUSTOMER, "Customer", FirebaseConstants.CUSTOMERS),
    Merchant(FirebaseConstants.ROLE_MERCHANT, "Merchant", FirebaseConstants.MERCHANTS),
    Rider(FirebaseConstants.ROLE_RIDER, "Rider", FirebaseConstants.RIDERS),
    Admin(FirebaseConstants.ROLE_ADMIN, "Admin", null);

    companion object {
        fun from(raw: String?): UserRole? {
            val cleanRole = raw?.trim()?.lowercase() ?: return null
            return entries.firstOrNull { it.id == cleanRole }
        }
    }
}

val registrationRoles = listOf(
    UserRole.Customer,
    UserRole.Merchant,
    UserRole.Rider
)
