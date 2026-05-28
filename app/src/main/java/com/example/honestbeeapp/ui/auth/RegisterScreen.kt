package com.example.honestbeeapp.ui.auth

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.example.honestbeeapp.ui.components.HonestbeeLogo
import com.example.honestbeeapp.ui.components.HonestbeeOutlinedButton
import com.example.honestbeeapp.ui.components.HonestbeePasswordField
import com.example.honestbeeapp.ui.components.HonestbeeTextField
import com.example.honestbeeapp.ui.components.StructuredAddress
import com.example.honestbeeapp.ui.components.StructuredAddressPickerDialog
import com.example.honestbeeapp.ui.theme.BeeCream
import com.example.honestbeeapp.ui.theme.BeeDarkText
import com.example.honestbeeapp.ui.theme.BeeHoneyYellow
import com.example.honestbeeapp.ui.theme.BeeMuted
import com.example.honestbeeapp.ui.theme.BeeNavigationSelected
import com.example.honestbeeapp.util.FirebaseConstants
import com.example.honestbeeapp.util.isDigitsOnly
import com.example.honestbeeapp.util.isGmailAddress
import com.example.honestbeeapp.util.isValidEmailFormat
import com.example.honestbeeapp.util.isValidPassword
import com.example.honestbeeapp.util.passwordsMatch
import com.example.honestbeeapp.util.startsWith09
import com.example.honestbeeapp.util.validateOpeningClosingTime
import com.example.honestbeeapp.util.validateStructuredAddress
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private val allowedRiderVehicleTypes = listOf("Bike", "Motorcycle", "Car")

@Composable
fun CustomerRegisterScreen(
    onRegistered: () -> Unit,
    onBackToWelcome: () -> Unit,
    auth: FirebaseAuth = remember { FirebaseAuth.getInstance() },
    firestore: FirebaseFirestore = remember { FirebaseFirestore.getInstance() }
) {
    RoleRegisterScreen(
        role = FirebaseConstants.ROLE_CUSTOMER,
        title = "Create Customer Account",
        subtitle = "Shop groceries and food from Android stores.",
        onRegistered = onRegistered,
        onBackToWelcome = onBackToWelcome,
        auth = auth,
        firestore = firestore
    )
}

@Composable
fun MerchantRegisterScreen(
    onRegistered: () -> Unit,
    onBackToWelcome: () -> Unit,
    auth: FirebaseAuth = remember { FirebaseAuth.getInstance() },
    firestore: FirebaseFirestore = remember { FirebaseFirestore.getInstance() }
) {
    RoleRegisterScreen(
        role = FirebaseConstants.ROLE_MERCHANT,
        title = "Become a Merchant",
        subtitle = "Create a store profile for admin review.",
        onRegistered = onRegistered,
        onBackToWelcome = onBackToWelcome,
        auth = auth,
        firestore = firestore
    )
}

@Composable
fun RiderRegisterScreen(
    onRegistered: () -> Unit,
    onBackToWelcome: () -> Unit,
    auth: FirebaseAuth = remember { FirebaseAuth.getInstance() },
    firestore: FirebaseFirestore = remember { FirebaseFirestore.getInstance() }
) {
    RoleRegisterScreen(
        role = FirebaseConstants.ROLE_RIDER,
        title = "Apply as Rider",
        subtitle = "Submit your rider profile for admin review.",
        onRegistered = onRegistered,
        onBackToWelcome = onBackToWelcome,
        auth = auth,
        firestore = firestore
    )
}

@Composable
private fun RoleRegisterScreen(
    role: String,
    title: String,
    subtitle: String,
    onRegistered: () -> Unit,
    onBackToWelcome: () -> Unit,
    auth: FirebaseAuth = remember { FirebaseAuth.getInstance() },
    firestore: FirebaseFirestore = remember { FirebaseFirestore.getInstance() }
) {
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var storeName by rememberSaveable { mutableStateOf("") }
    var ownerName by rememberSaveable { mutableStateOf("") }
    var openingTime by rememberSaveable { mutableStateOf("") }
    var closingTime by rememberSaveable { mutableStateOf("") }
    var businessPermitLocalUri by rememberSaveable { mutableStateOf("") }
    var businessPermitFileName by rememberSaveable { mutableStateOf("") }
    var vehicleType by rememberSaveable { mutableStateOf("") }
    var driverLicenseLocalUri by rememberSaveable { mutableStateOf("") }
    var driverLicenseFileName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var street by rememberSaveable { mutableStateOf("") }
    var barangay by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("") }
    var province by rememberSaveable { mutableStateOf("") }
    var latitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var longitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var isAddressPickerOpen by rememberSaveable { mutableStateOf(false) }
    var hasSubmitted by rememberSaveable { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isMerchant = role == FirebaseConstants.ROLE_MERCHANT
    val isRider = role == FirebaseConstants.ROLE_RIDER
    val businessPermitPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            businessPermitLocalUri = uri.toString()
            businessPermitFileName = resolveDisplayName(context, uri, "business_permit_image")
        }
    }
    val driverLicensePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            driverLicenseLocalUri = uri.toString()
            driverLicenseFileName = resolveDisplayName(context, uri, "driver_license_image")
        }
    }
    val submittedErrors = validateRegisterForm(
        firstName = firstName,
        lastName = lastName,
        storeName = storeName,
        ownerName = ownerName,
        openingTime = openingTime,
        closingTime = closingTime,
        businessPermitLocalUri = businessPermitLocalUri,
        vehicleType = vehicleType,
        driverLicenseLocalUri = driverLicenseLocalUri,
        email = email,
        phone = phone,
        street = street,
        barangay = barangay,
        city = city,
        province = province,
        password = password,
        confirmPassword = confirmPassword,
        role = role
    )
    val storeNameError = if (hasSubmitted) submittedErrors.storeName else null
    val ownerNameError = if (hasSubmitted) submittedErrors.ownerName else null
    val firstNameError = if (hasSubmitted) submittedErrors.firstName else null
    val lastNameError = if (hasSubmitted) submittedErrors.lastName else null
    val emailError = if (hasSubmitted || email.isNotBlank()) submittedErrors.email else null
    val phoneError = if (hasSubmitted || phone.isNotBlank()) submittedErrors.phone else null
    val openingTimeError = if (hasSubmitted) submittedErrors.openingTime else null
    val closingTimeError = if (hasSubmitted) submittedErrors.closingTime else null
    val businessPermitImageError = if (hasSubmitted) submittedErrors.businessPermitImage else null
    val vehicleTypeError = if (hasSubmitted) submittedErrors.vehicleType else null
    val driverLicenseImageError = if (hasSubmitted) submittedErrors.driverLicenseImage else null
    val streetError = if (hasSubmitted) submittedErrors.street else null
    val barangayError = if (hasSubmitted) submittedErrors.barangay else null
    val cityError = if (hasSubmitted) submittedErrors.city else null
    val provinceError = if (hasSubmitted) submittedErrors.province else null
    val passwordError = if (hasSubmitted || password.isNotBlank()) submittedErrors.password else null
    val confirmPasswordError = if (hasSubmitted || confirmPassword.isNotBlank()) {
        submittedErrors.confirmPassword
    } else {
        null
    }

    BackHandler(onBack = onBackToWelcome)

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
            HonestbeeLogo(modifier = Modifier.size(96.dp))
            Spacer(Modifier.height(14.dp))

            HonestbeeCard(modifier = Modifier.widthIn(max = 430.dp)) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = BeeDarkText,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = BeeMuted,
                        textAlign = TextAlign.Center
                    )

                    if (isMerchant) {
                        HonestbeeTextField(
                            value = storeName,
                            onValueChange = { storeName = it },
                            placeholder = "Store name",
                            leadingIcon = Icons.Outlined.Storefront,
                            enabled = !isLoading,
                            errorText = storeNameError
                        )
                        HonestbeeTextField(
                            value = ownerName,
                            onValueChange = { ownerName = it },
                            placeholder = "Owner name",
                            leadingIcon = Icons.Outlined.Badge,
                            enabled = !isLoading,
                            errorText = ownerNameError
                        )
                    }

                    if (!isMerchant) {
                        HonestbeeTextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            placeholder = "First name",
                            leadingIcon = Icons.Outlined.Badge,
                            enabled = !isLoading,
                            errorText = firstNameError
                        )
                        HonestbeeTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            placeholder = "Last name",
                            leadingIcon = Icons.Outlined.Badge,
                            enabled = !isLoading,
                            errorText = lastNameError
                        )
                    }

                    HonestbeeTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "Email address",
                        leadingIcon = Icons.Outlined.Email,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        enabled = !isLoading,
                        errorText = emailError
                    )
                    HonestbeeTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        placeholder = "Phone number",
                        leadingIcon = Icons.Outlined.LocalPhone,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        enabled = !isLoading,
                        errorText = phoneError
                    )

                    if (isMerchant) {
                        HonestbeeTextField(
                            value = openingTime,
                            onValueChange = { openingTime = it },
                            placeholder = "Opening time",
                            enabled = !isLoading,
                            errorText = openingTimeError
                        )
                        HonestbeeTextField(
                            value = closingTime,
                            onValueChange = { closingTime = it },
                            placeholder = "Closing time",
                            enabled = !isLoading,
                            errorText = closingTimeError
                        )
                        RequiredImageField(
                            title = "Business permit image",
                            selectedImageUri = businessPermitLocalUri,
                            selectedFileName = businessPermitFileName,
                            buttonText = "Select Business Permit Image",
                            errorText = businessPermitImageError,
                            enabled = !isLoading,
                            onPickImage = { businessPermitPicker.launch("image/*") }
                        )
                    }

                    if (isRider) {
                        VehicleTypeSelector(
                            selectedVehicleType = vehicleType,
                            onVehicleTypeSelected = { vehicleType = it },
                            enabled = !isLoading,
                            errorText = vehicleTypeError
                        )
                        RequiredImageField(
                            title = "Driver's license image",
                            selectedImageUri = driverLicenseLocalUri,
                            selectedFileName = driverLicenseFileName,
                            buttonText = "Select Driver's License Image",
                            errorText = driverLicenseImageError,
                            enabled = !isLoading,
                            onPickImage = { driverLicensePicker.launch("image/*") }
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Delivery / Location Address",
                            style = MaterialTheme.typography.titleMedium,
                            color = BeeDarkText,
                            fontWeight = FontWeight.SemiBold
                        )
                        HonestbeeTextField(
                            value = street,
                            onValueChange = { street = it },
                            placeholder = "Street",
                            leadingIcon = Icons.Outlined.Home,
                            enabled = !isLoading,
                            errorText = streetError
                        )
                        HonestbeeTextField(
                            value = barangay,
                            onValueChange = { barangay = it },
                            placeholder = "Barangay",
                            enabled = !isLoading,
                            errorText = barangayError
                        )
                        HonestbeeTextField(
                            value = city,
                            onValueChange = { city = it },
                            placeholder = "City",
                            enabled = !isLoading,
                            errorText = cityError
                        )
                        HonestbeeTextField(
                            value = province,
                            onValueChange = { province = it },
                            placeholder = "Province",
                            enabled = !isLoading,
                            errorText = provinceError
                        )
                        HonestbeeButton(
                            text = "Pick on Map",
                            onClick = { isAddressPickerOpen = true },
                            enabled = !isLoading
                        )

                        if (
                            street.isNotBlank() ||
                            barangay.isNotBlank() ||
                            city.isNotBlank() ||
                            province.isNotBlank() ||
                            latitude != null ||
                            longitude != null
                        ) {
                            SelectedAddressSummary(
                                street = street,
                                barangay = barangay,
                                city = city,
                                province = province,
                                latitude = latitude,
                                longitude = longitude
                            )
                        }
                    }
                    HonestbeePasswordField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "Password",
                        enabled = !isLoading,
                        errorText = passwordError
                    )
                    HonestbeePasswordField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = "Confirm password",
                        enabled = !isLoading,
                        errorText = confirmPasswordError
                    )

                    errorMessage?.let { ErrorMessage(it) }

                    HonestbeeButton(
                        text = "Register",
                        isLoading = isLoading,
                        enabled = !isLoading,
                        onClick = {
                            if (isLoading) return@HonestbeeButton

                            hasSubmitted = true
                            val validationErrors = validateRegisterForm(
                                firstName = firstName,
                                lastName = lastName,
                                storeName = storeName,
                                ownerName = ownerName,
                                openingTime = openingTime,
                                closingTime = closingTime,
                                businessPermitLocalUri = businessPermitLocalUri,
                                vehicleType = vehicleType,
                                driverLicenseLocalUri = driverLicenseLocalUri,
                                email = email,
                                phone = phone,
                                street = street,
                                barangay = barangay,
                                city = city,
                                province = province,
                                password = password,
                                confirmPassword = confirmPassword,
                                role = role
                            )
                            if (validationErrors.hasErrors) {
                                errorMessage = null
                                return@HonestbeeButton
                            }

                            isLoading = true
                            scope.launch {
                                errorMessage = null
                                try {
                                    registerAccount(
                                        auth = auth,
                                        firestore = firestore,
                                        firstName = firstName.trim(),
                                        lastName = lastName.trim(),
                                        storeName = storeName.trim(),
                                        ownerName = ownerName.trim(),
                                        openingTime = openingTime.trim(),
                                        closingTime = closingTime.trim(),
                                        businessPermitFileName = businessPermitFileName,
                                        vehicleType = vehicleType.trim(),
                                        driverLicenseFileName = driverLicenseFileName,
                                        email = email.trim(),
                                        phone = phone.trim(),
                                        street = street.trim(),
                                        barangay = barangay.trim(),
                                        city = city.trim(),
                                        province = province.trim(),
                                        latitude = latitude,
                                        longitude = longitude,
                                        password = password,
                                        role = role
                                    )
                                    isLoading = false
                                    onRegistered()
                                } catch (exception: Exception) {
                                    isLoading = false
                                    errorMessage = friendlyRegistrationError(exception)
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
                        TextButton(onClick = onBackToWelcome) {
                            Text(
                                text = "Back to welcome",
                                color = BeeHoneyYellow,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }

    if (isAddressPickerOpen) {
        StructuredAddressPickerDialog(
            initialAddress = StructuredAddress(
                street = street,
                barangay = barangay,
                city = city,
                province = province,
                latitude = latitude,
                longitude = longitude
            ),
            onDismiss = { isAddressPickerOpen = false },
            onAddressSelected = { selectedAddress ->
                street = selectedAddress.street
                barangay = selectedAddress.barangay
                city = selectedAddress.city
                province = selectedAddress.province
                latitude = selectedAddress.latitude
                longitude = selectedAddress.longitude
                isAddressPickerOpen = false
            }
        )
    }
}

@Composable
private fun SelectedAddressSummary(
    street: String,
    barangay: String,
    city: String,
    province: String,
    latitude: Double?,
    longitude: Double?
) {
    val selectedAddress = formatStructuredAddress(
        street = street,
        barangay = barangay,
        city = city,
        province = province
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = BeeNavigationSelected,
        border = BorderStroke(1.dp, BeeHoneyYellow.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Selected Location:",
                style = MaterialTheme.typography.labelLarge,
                color = BeeDarkText,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = selectedAddress.ifBlank { "Complete the address fields manually." },
                style = MaterialTheme.typography.bodyMedium,
                color = BeeDarkText
            )
            latitude?.let {
                Text(
                    text = "Lat: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = BeeMuted
                )
            }
            longitude?.let {
                Text(
                    text = "Lng: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = BeeMuted
                )
            }
        }
    }
}

@Composable
private fun RequiredImageField(
    title: String,
    selectedImageUri: String,
    selectedFileName: String,
    buttonText: String,
    errorText: String?,
    enabled: Boolean,
    onPickImage: () -> Unit
) {
    val previewBitmap = rememberSelectedImageBitmap(selectedImageUri)
    val borderColor = if (errorText != null) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.outline
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = BeeDarkText,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (selectedImageUri.isBlank()) {
                    "No image selected"
                } else {
                    selectedFileName.ifBlank { selectedImageLabel(selectedImageUri) }
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (selectedImageUri.isBlank()) BeeMuted else BeeDarkText
            )
            previewBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap,
                    contentDescription = "$title preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            HonestbeeOutlinedButton(
                text = if (selectedImageUri.isBlank()) buttonText else "Change image",
                onClick = onPickImage,
                enabled = enabled
            )
            errorText?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun VehicleTypeSelector(
    selectedVehicleType: String,
    onVehicleTypeSelected: (String) -> Unit,
    enabled: Boolean,
    errorText: String?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Vehicle Type",
            style = MaterialTheme.typography.labelLarge,
            color = BeeDarkText,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            allowedRiderVehicleTypes.forEach { option ->
                val selected = selectedVehicleType == option
                Surface(
                    onClick = {
                        if (enabled) {
                            onVehicleTypeSelected(option)
                        }
                    },
                    shape = RoundedCornerShape(50),
                    color = if (selected) BeeNavigationSelected else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        1.dp,
                        if (selected) BeeHoneyYellow else MaterialTheme.colorScheme.outline
                    )
                ) {
                    Text(
                        text = option,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (enabled) BeeDarkText else BeeMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        errorText?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun rememberSelectedImageBitmap(selectedImageUri: String): ImageBitmap? {
    val context = LocalContext.current
    val previewBitmap by produceState<ImageBitmap?>(initialValue = null, selectedImageUri) {
        value = if (selectedImageUri.isBlank()) {
            null
        } else {
            withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(Uri.parse(selectedImageUri))?.use { input ->
                        BitmapFactory.decodeStream(input)?.asImageBitmap()
                    }
                }.getOrNull()
            }
        }
    }
    return previewBitmap
}

private fun selectedImageLabel(uri: String): String {
    return uri.substringAfterLast("/")
        .substringBefore("?")
        .ifBlank { "Selected image" }
}

private fun resolveDisplayName(context: Context, uri: Uri, fallbackName: String): String {
    val displayName = context.contentResolver
        .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                cursor.getString(nameIndex)
            } else {
                null
            }
        }

    return displayName
        ?: uri.lastPathSegment?.substringAfterLast("/")
        ?: fallbackName
}


private data class RegisterFormErrors(
    val storeName: String? = null,
    val ownerName: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val openingTime: String? = null,
    val closingTime: String? = null,
    val businessPermitImage: String? = null,
    val vehicleType: String? = null,
    val driverLicenseImage: String? = null,
    val street: String? = null,
    val barangay: String? = null,
    val city: String? = null,
    val province: String? = null,
    val password: String? = null,
    val confirmPassword: String? = null
) {
    val hasErrors: Boolean
        get() = listOf(
            storeName,
            ownerName,
            firstName,
            lastName,
            email,
            phone,
            openingTime,
            closingTime,
            businessPermitImage,
            vehicleType,
            driverLicenseImage,
            street,
            barangay,
            city,
            province,
            password,
            confirmPassword
        ).any { it != null }
}

private fun validateRegisterForm(
    firstName: String,
    lastName: String,
    storeName: String,
    ownerName: String,
    openingTime: String,
    closingTime: String,
    businessPermitLocalUri: String,
    vehicleType: String,
    driverLicenseLocalUri: String,
    email: String,
    phone: String,
    street: String,
    barangay: String,
    city: String,
    province: String,
    password: String,
    confirmPassword: String,
    role: String
): RegisterFormErrors {
    val isMerchant = role == FirebaseConstants.ROLE_MERCHANT
    val isRider = role == FirebaseConstants.ROLE_RIDER
    val addressErrors = validateStructuredAddress(
        street = street,
        barangay = barangay,
        city = city,
        province = province
    )
    val timeErrors = validateOpeningClosingTime(
        openingTime = openingTime,
        closingTime = closingTime
    )

    return RegisterFormErrors(
        storeName = if (isMerchant && storeName.isBlank()) "Store name is required." else null,
        ownerName = if (isMerchant && ownerName.isBlank()) "Owner name is required." else null,
        firstName = when {
            !isMerchant && firstName.isBlank() -> "First name is required."
            else -> null
        },
        lastName = when {
            !isMerchant && lastName.isBlank() -> "Last name is required."
            else -> null
        },
        email = validateEmailInput(email),
        phone = validatePhoneInput(phone),
        openingTime = if (isMerchant) timeErrors.openingTime else null,
        closingTime = if (isMerchant) timeErrors.closingTime else null,
        businessPermitImage = if (isMerchant && businessPermitLocalUri.isBlank()) {
            "Business permit image is required."
        } else {
            null
        },
        vehicleType = when {
            isRider && vehicleType.isBlank() -> "Vehicle type is required."
            isRider && vehicleType !in allowedRiderVehicleTypes -> "Select Bike, Motorcycle, or Car."
            else -> null
        },
        driverLicenseImage = if (isRider && driverLicenseLocalUri.isBlank()) {
            "Driver's license image is required."
        } else {
            null
        },
        street = addressErrors.street,
        barangay = addressErrors.barangay,
        city = addressErrors.city,
        province = addressErrors.province,
        password = validatePasswordInput(password),
        confirmPassword = validateConfirmPasswordInput(
            password = password,
            confirmPassword = confirmPassword
        )
    )
}

private fun validateEmailInput(email: String): String? {
    val cleanEmail = email.trim()
    if (cleanEmail.isBlank()) return "Email is required."
    if (!isValidEmailFormat(cleanEmail)) return "Email must be valid format."
    if (!isGmailAddress(cleanEmail)) return "Only Gmail addresses are allowed."
    return null
}

private fun validatePhoneInput(phone: String): String? {
    val cleanPhone = phone.trim()
    if (cleanPhone.isBlank()) return "Phone is required."
    if (!isDigitsOnly(cleanPhone)) return "Phone number must contain digits only."
    if (!startsWith09(cleanPhone)) return "Phone number must start with 09."
    if (cleanPhone.length != 11) return "Phone number must be exactly 11 digits."
    return null
}

private fun validatePasswordInput(password: String): String? {
    if (password.isBlank()) return "Password is required."
    if (!isValidPassword(password)) return "Password must be at least 6 characters."
    return null
}

private fun validateConfirmPasswordInput(
    password: String,
    confirmPassword: String
): String? {
    if (confirmPassword.isBlank()) return "Confirm password is required."
    if (!passwordsMatch(password, confirmPassword)) return "Passwords do not match."
    return null
}

private suspend fun registerAccount(
    auth: FirebaseAuth,
    firestore: FirebaseFirestore,
    firstName: String,
    lastName: String,
    storeName: String,
    ownerName: String,
    openingTime: String,
    closingTime: String,
    businessPermitFileName: String,
    vehicleType: String,
    driverLicenseFileName: String,
    email: String,
    phone: String,
    street: String,
    barangay: String,
    city: String,
    province: String,
    latitude: Double?,
    longitude: Double?,
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
    val isMerchant = role == FirebaseConstants.ROLE_MERCHANT
    val isRider = role == FirebaseConstants.ROLE_RIDER
    val profileFirstName = if (isMerchant) ownerName else firstName
    val profileLastName = if (isMerchant) "" else lastName
    val username = if (isMerchant) ownerName else "$firstName $lastName".trim()
    val address = formatStructuredAddress(
        street = street,
        barangay = barangay,
        city = city,
        province = province
    )
    val batch = firestore.batch()

    val appUser = AppUser(
        uid = user.uid,
        email = email,
        username = username,
        role = role,
        status = status,
        storeName = if (isMerchant) storeName else "",
        ownerName = if (isMerchant) ownerName else "",
        firstName = profileFirstName,
        lastName = profileLastName,
        phone = phone,
        street = street,
        barangay = barangay,
        city = city,
        province = province,
        address = address,
        latitude = latitude,
        longitude = longitude,
        vehicleType = if (isRider) vehicleType else "",
        openingTime = if (isMerchant) openingTime else "",
        closingTime = if (isMerchant) closingTime else "",
        businessPermitSubmitted = isMerchant,
        businessPermitFileName = if (isMerchant) businessPermitFileName else "",
        businessPermitLocalOnly = isMerchant,
        businessPermitUrl = "",
        driverLicenseSubmitted = isRider,
        driverLicenseFileName = if (isRider) driverLicenseFileName else "",
        driverLicenseLocalOnly = isRider,
        driverLicenseUrl = "",
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
                street = street,
                barangay = barangay,
                city = city,
                province = province,
                address = address,
                latitude = latitude,
                longitude = longitude,
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
                storeName = storeName,
                ownerName = ownerName,
                phone = phone,
                openingTime = openingTime,
                closingTime = closingTime,
                businessPermitSubmitted = true,
                businessPermitFileName = businessPermitFileName,
                businessPermitLocalOnly = true,
                businessPermitUrl = "",
                street = street,
                barangay = barangay,
                city = city,
                province = province,
                address = address,
                latitude = latitude,
                longitude = longitude,
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
                vehicleType = vehicleType,
                driverLicenseSubmitted = true,
                driverLicenseFileName = driverLicenseFileName,
                driverLicenseLocalOnly = true,
                driverLicenseUrl = "",
                street = street,
                barangay = barangay,
                city = city,
                province = province,
                currentLocation = address,
                latitude = latitude,
                longitude = longitude,
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

private fun friendlyRegistrationError(exception: Exception): String {
    return when (exception) {
        is FirebaseAuthUserCollisionException -> "Email already in use."
        is FirebaseAuthInvalidCredentialsException -> "Invalid email address."
        is FirebaseAuthWeakPasswordException -> "Weak password. Password must be at least 6 characters."
        is FirebaseNetworkException -> "Network error. Please check your connection and try again."
        is FirebaseFirestoreException -> {
            if (exception.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                "Permission denied. Please contact support."
            } else {
                "Unknown error. Please try again."
            }
        }
        else -> {
            val message = exception.localizedMessage.orEmpty()
            when {
                message.contains("network", ignoreCase = true) ->
                    "Network error. Please check your connection and try again."
                message.contains("permission", ignoreCase = true) ->
                    "Permission denied. Please contact support."
                message.contains("email address is already", ignoreCase = true) ->
                    "Email already in use."
                else -> "Unknown error. Please try again."
            }
        }
    }
}

private fun formatStructuredAddress(
    street: String,
    barangay: String,
    city: String,
    province: String
): String {
    return listOf(street, barangay, city, province)
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .joinToString(", ")
}
