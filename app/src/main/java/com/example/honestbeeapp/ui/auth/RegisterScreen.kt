package com.example.honestbeeapp.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalPhone
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.honestbeeapp.data.model.AppUser
import com.example.honestbeeapp.data.model.CustomerProfile
import com.example.honestbeeapp.data.model.MerchantProfile
import com.example.honestbeeapp.data.model.RiderProfile
import com.example.honestbeeapp.ui.components.ErrorMessage
import com.example.honestbeeapp.ui.components.HonestbeeButton
import com.example.honestbeeapp.ui.components.HonestbeeCard
import com.example.honestbeeapp.ui.components.HonestbeePasswordField
import com.example.honestbeeapp.ui.components.HonestbeeTextField
import com.example.honestbeeapp.ui.theme.BeeCream
import com.example.honestbeeapp.ui.theme.BeeDarkText
import com.example.honestbeeapp.ui.theme.BeeHoneyYellow
import com.example.honestbeeapp.ui.theme.BeeMuted
import com.example.honestbeeapp.ui.theme.BeeNavigationSelected
import com.example.honestbeeapp.util.FirebaseConstants
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    onBackToLogin: () -> Unit,
    auth: FirebaseAuth = remember { FirebaseAuth.getInstance() },
    firestore: FirebaseFirestore = remember { FirebaseFirestore.getInstance() }
) {
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var selectedRole by rememberSaveable { mutableStateOf(FirebaseConstants.ROLE_CUSTOMER) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BeeCream
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "honestbee",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = BeeDarkText
            )
            Spacer(Modifier.height(14.dp))

            HonestbeeCard(modifier = Modifier.widthIn(max = 430.dp)) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.headlineSmall,
                        color = BeeDarkText,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Join honestbee today",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BeeMuted,
                        textAlign = TextAlign.Center
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RoleChip(
                            label = "Customer",
                            icon = Icons.Outlined.Person,
                            selected = selectedRole == FirebaseConstants.ROLE_CUSTOMER,
                            onClick = { selectedRole = FirebaseConstants.ROLE_CUSTOMER },
                            modifier = Modifier.weight(1f)
                        )
                        RoleChip(
                            label = "Merchant",
                            icon = Icons.Outlined.Storefront,
                            selected = selectedRole == FirebaseConstants.ROLE_MERCHANT,
                            onClick = { selectedRole = FirebaseConstants.ROLE_MERCHANT },
                            modifier = Modifier.weight(1f)
                        )
                        RoleChip(
                            label = "Rider",
                            icon = Icons.Outlined.LocalShipping,
                            selected = selectedRole == FirebaseConstants.ROLE_RIDER,
                            onClick = { selectedRole = FirebaseConstants.ROLE_RIDER },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HonestbeeTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        placeholder = "First name",
                        leadingIcon = Icons.Outlined.Badge,
                        enabled = !isLoading
                    )
                    HonestbeeTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        placeholder = "Last name",
                        leadingIcon = Icons.Outlined.Badge,
                        enabled = !isLoading
                    )

                    HonestbeeTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "Email address",
                        leadingIcon = Icons.Outlined.Email,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        enabled = !isLoading
                    )
                    HonestbeeTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        placeholder = "Phone number",
                        leadingIcon = Icons.Outlined.LocalPhone,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        enabled = !isLoading
                    )
                    HonestbeeTextField(
                        value = address,
                        onValueChange = { address = it },
                        placeholder = "Address",
                        leadingIcon = Icons.Outlined.Home,
                        enabled = !isLoading,
                        singleLine = false
                    )
                    HonestbeePasswordField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "Password",
                        enabled = !isLoading
                    )
                    HonestbeePasswordField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = "Confirm password",
                        enabled = !isLoading
                    )

                    errorMessage?.let { ErrorMessage(it) }

                    HonestbeeButton(
                        text = "Register",
                        isLoading = isLoading,
                        enabled = !isLoading,
                        onClick = {
                            val validationError = validateRegisterForm(
                                firstName = firstName,
                                lastName = lastName,
                                email = email,
                                phone = phone,
                                address = address,
                                password = password,
                                confirmPassword = confirmPassword
                            )
                            if (validationError != null) {
                                errorMessage = validationError
                                return@HonestbeeButton
                            }

                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                try {
                                    registerAccount(
                                        auth = auth,
                                        firestore = firestore,
                                        firstName = firstName.trim(),
                                        lastName = lastName.trim(),
                                        email = email.trim(),
                                        phone = phone.trim(),
                                        address = address.trim(),
                                        password = password,
                                        role = selectedRole
                                    )
                                    isLoading = false
                                    onRegistered()
                                } catch (exception: Exception) {
                                    isLoading = false
                                    errorMessage = exception.localizedMessage ?: "Registration failed."
                                }
                            }
                        }
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Already have an account?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BeeMuted
                        )
                        TextButton(onClick = onBackToLogin) {
                            Text(
                                text = "Login",
                                color = BeeHoneyYellow,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) BeeNavigationSelected else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (selected) BeeHoneyYellow else MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) BeeHoneyYellow else BeeMuted
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = BeeDarkText,
                maxLines = 1
            )
        }
    }
}

private fun validateRegisterForm(
    firstName: String,
    lastName: String,
    email: String,
    phone: String,
    address: String,
    password: String,
    confirmPassword: String
): String? {
    if (firstName.isBlank()) return "Enter your first name."
    if (lastName.isBlank()) return "Enter your last name."
    if (!email.contains("@") || !email.contains(".")) return "Enter a valid email address."
    if (phone.isBlank()) return "Enter your phone number."
    if (address.isBlank()) return "Enter your address."
    if (password.length < 6) return "Password must be at least 6 characters."
    if (password != confirmPassword) return "Passwords do not match."
    return null
}

private suspend fun registerAccount(
    auth: FirebaseAuth,
    firestore: FirebaseFirestore,
    firstName: String,
    lastName: String,
    email: String,
    phone: String,
    address: String,
    password: String,
    role: String
) {
    val authResult = auth.createUserWithEmailAndPassword(email, password).await()
    val user = authResult.user ?: error("Firebase did not return a user.")
    val status = if (role == FirebaseConstants.ROLE_CUSTOMER) {
        FirebaseConstants.STATUS_ACTIVE
    } else {
        FirebaseConstants.STATUS_PENDING
    }
    val now = Timestamp.now()
    val username = "$firstName $lastName".trim()
    val batch = firestore.batch()

    val appUser = AppUser(
        uid = user.uid,
        email = email,
        username = username,
        role = role,
        status = status,
        firstName = firstName,
        lastName = lastName,
        phone = phone,
        address = address,
        createdAt = now,
        updatedAt = now
    )
    batch.set(
        firestore.collection(FirebaseConstants.USERS).document(user.uid),
        appUser
    )

    when (role) {
        FirebaseConstants.ROLE_CUSTOMER -> {
            val profile = CustomerProfile(
                uid = user.uid,
                email = email,
                firstName = firstName,
                lastName = lastName,
                phone = phone,
                address = address,
                role = role,
                status = status,
                createdAt = now,
                updatedAt = now
            )
            batch.set(
                firestore.collection(FirebaseConstants.CUSTOMERS).document(user.uid),
                profile
            )
        }

        FirebaseConstants.ROLE_MERCHANT -> {
            val profile = MerchantProfile(
                uid = user.uid,
                email = email,
                storeName = "",
                ownerName = username,
                phone = phone,
                address = address,
                role = role,
                status = status,
                createdAt = now,
                updatedAt = now
            )
            batch.set(
                firestore.collection(FirebaseConstants.MERCHANTS).document(user.uid),
                profile
            )
        }

        FirebaseConstants.ROLE_RIDER -> {
            val profile = RiderProfile(
                uid = user.uid,
                email = email,
                firstName = firstName,
                lastName = lastName,
                phone = phone,
                vehicleType = "",
                currentLocation = "",
                role = role,
                status = status,
                createdAt = now,
                updatedAt = now
            )
            batch.set(
                firestore.collection(FirebaseConstants.RIDERS).document(user.uid),
                profile
            )
        }
    }

    batch.commit().await()
}
