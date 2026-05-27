package com.example.honestbeeapp.util

object FirebaseConstants {
    // Shared account/profile collections
    const val USERS = "users"
    const val CUSTOMERS = "customers"
    const val MERCHANTS = "merchants"
    const val RIDERS = "riders"

    // Android-only app feature collections
    const val ANDROID_STORES = "android_stores"
    const val ANDROID_PRODUCTS = "android_products"
    const val ANDROID_CART = "android_cart"
    const val ANDROID_ORDERS = "android_orders"

    // Roles
    const val ROLE_CUSTOMER = "customer"
    const val ROLE_MERCHANT = "merchant"
    const val ROLE_RIDER = "rider"
    const val ROLE_ADMIN = "admin"

    // Account statuses
    const val STATUS_ACTIVE = "active"
    const val STATUS_PENDING = "pending"
    const val STATUS_APPROVED = "approved"
    const val STATUS_REJECTED = "rejected"
    const val STATUS_DELETED = "deleted"

    // Android order statuses
    const val STATUS_TO_PAY = "To Pay"
    const val STATUS_TO_PREPARE = "To Prepare"
    const val STATUS_TO_SHIP = "To Ship"
    const val STATUS_OUT_FOR_DELIVERY = "Out for Delivery"
    const val STATUS_COMPLETED = "Completed"
    const val STATUS_CANCELLED = "Cancelled"
}
