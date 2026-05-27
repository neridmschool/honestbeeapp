package com.example.honestbeeapp.ui.components

fun formatSamplePeso(amount: Double): String {
    return "PHP " + String.format("%.2f", amount)
}
