package com.example.honestbeeapp.util

fun String.shortUid(): String = if (length <= 8) this else take(8)

fun Throwable.readableMessage(fallback: String): String {
    return localizedMessage ?: message ?: fallback
}
