package com.example.honestbeeapp.util

private val emailFormatRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
private val gmailRegex = Regex("^[A-Za-z0-9._%+-]+@gmail\\.com$", RegexOption.IGNORE_CASE)

data class StructuredAddressErrors(
    val street: String? = null,
    val barangay: String? = null,
    val city: String? = null,
    val province: String? = null
) {
    val hasErrors: Boolean
        get() = listOf(street, barangay, city, province).any { it != null }
}

data class OpeningClosingTimeErrors(
    val openingTime: String? = null,
    val closingTime: String? = null
) {
    val hasErrors: Boolean
        get() = openingTime != null || closingTime != null
}

fun isValidEmailFormat(email: String): Boolean {
    return emailFormatRegex.matches(email.trim())
}

fun isGmailAddress(email: String): Boolean {
    return gmailRegex.matches(email.trim())
}

fun isDigitsOnly(phone: String): Boolean {
    return phone.trim().all { it.isDigit() }
}

fun startsWith09(phone: String): Boolean {
    return phone.trim().startsWith("09")
}

fun isValidPhone(phone: String): Boolean {
    val cleanPhone = phone.trim()
    return isDigitsOnly(cleanPhone) &&
        startsWith09(cleanPhone) &&
        cleanPhone.length == 11
}

fun isValidPassword(password: String): Boolean {
    return password.length >= 6
}

fun passwordsMatch(password: String, confirmPassword: String): Boolean {
    return password == confirmPassword
}

fun validateStructuredAddress(
    street: String,
    barangay: String,
    city: String,
    province: String
): StructuredAddressErrors {
    return StructuredAddressErrors(
        street = if (street.isBlank()) "Street is required." else null,
        barangay = if (barangay.isBlank()) "Barangay is required." else null,
        city = if (city.isBlank()) "City is required." else null,
        province = if (province.isBlank()) "Province is required." else null
    )
}

fun validateOpeningClosingTime(
    openingTime: String,
    closingTime: String
): OpeningClosingTimeErrors {
    val cleanOpeningTime = openingTime.trim()
    val cleanClosingTime = closingTime.trim()

    return OpeningClosingTimeErrors(
        openingTime = if (cleanOpeningTime.isBlank()) "Opening time is required." else null,
        closingTime = when {
            cleanClosingTime.isBlank() -> "Closing time is required."
            cleanOpeningTime.equals(cleanClosingTime, ignoreCase = true) ->
                "Closing time must be different from opening time."
            else -> null
        }
    )
}
