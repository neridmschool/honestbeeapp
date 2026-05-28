package com.example.honestbeeapp.ui.rider

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.honestbeeapp.data.model.RiderProfile
import com.example.honestbeeapp.data.model.SessionProfile
import com.example.honestbeeapp.data.repository.AuthRepository
import com.example.honestbeeapp.data.repository.UserRepository
import com.example.honestbeeapp.data.sample.AndroidSampleData
import com.example.honestbeeapp.data.sample.SampleRiderJob
import com.example.honestbeeapp.ui.components.HonestbeeButton
import com.example.honestbeeapp.ui.components.HonestbeeCard
import com.example.honestbeeapp.ui.components.HonestbeeLogo
import com.example.honestbeeapp.ui.components.HonestbeeOutlinedButton
import com.example.honestbeeapp.ui.components.HonestbeePasswordField
import com.example.honestbeeapp.ui.components.HonestbeeRemoteImage
import com.example.honestbeeapp.ui.components.HonestbeeTextField
import com.example.honestbeeapp.ui.components.StatusChip
import com.example.honestbeeapp.ui.components.formatSamplePeso
import com.example.honestbeeapp.ui.theme.BeeCream
import com.example.honestbeeapp.ui.theme.BeeDarkText
import com.example.honestbeeapp.ui.theme.BeeError
import com.example.honestbeeapp.ui.theme.BeeHoneyYellow
import com.example.honestbeeapp.ui.theme.BeeMuted
import com.example.honestbeeapp.ui.theme.BeeNavigationSelected
import com.example.honestbeeapp.ui.theme.BeePrimaryYellow
import com.example.honestbeeapp.ui.theme.BeeSuccess
import com.example.honestbeeapp.util.FirebaseConstants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private val deliveryStages = listOf(
    "Accepted",
    "Shopping",
    "Out for delivery",
    "Delivered"
)

@Composable
fun RiderMainScreen(
    profile: SessionProfile,
    onLogout: () -> Unit,
    firestore: FirebaseFirestore = remember { FirebaseFirestore.getInstance() }
) {
    var selectedTab by rememberSaveable { mutableStateOf(RiderTab.Available) }
    var riderProfile by remember { mutableStateOf<RiderProfile?>(null) }
    val availableJobs = remember {
        mutableStateListOf<SampleRiderJob>().apply {
            addAll(AndroidSampleData.riderJobs.filter { it.status == "Available" })
        }
    }
    val historyJobs = remember {
        mutableStateListOf<SampleRiderJob>().apply {
            addAll(
                AndroidSampleData.riderJobs.filter {
                    it.status == FirebaseConstants.STATUS_COMPLETED ||
                        it.status == FirebaseConstants.STATUS_CANCELLED
                }.map {
                    if (it.status == FirebaseConstants.STATUS_COMPLETED) {
                        it.copy(status = "Delivered")
                    } else {
                        it
                    }
                }
            )
        }
    }
    var currentJob by remember {
        mutableStateOf(
            AndroidSampleData.riderJobs
                .firstOrNull { it.status == "Current" }
                ?.copy(status = deliveryStages.first())
        )
    }
    var currentStageIndex by rememberSaveable { mutableIntStateOf(0) }
    var activeProfileDialog by rememberSaveable { mutableStateOf<RiderProfileDialog?>(null) }
    var profileMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val userRepository = remember(firestore) { UserRepository(firestore) }
    val authRepository = remember { AuthRepository() }

    LaunchedEffect(profile.uid, firestore) {
        riderProfile = runCatching {
            firestore.collection(FirebaseConstants.RIDERS)
                .document(profile.uid)
                .get()
                .await()
                .toObject(RiderProfile::class.java)
        }.getOrNull()
    }

    fun acceptJob(job: SampleRiderJob) {
        availableJobs.removeAll { it.jobId == job.jobId }
        currentStageIndex = 0
        currentJob = job.copy(status = deliveryStages.first())
        selectedTab = RiderTab.Current
    }

    fun moveToNextStatus() {
        val job = currentJob ?: return
        if (currentStageIndex < deliveryStages.lastIndex) {
            currentStageIndex += 1
            val newStatus = deliveryStages[currentStageIndex]
            currentJob = job.copy(status = newStatus)

            if (newStatus == "Delivered") {
                historyJobs.add(0, job.copy(status = "Delivered"))
            }
        }
    }

    suspend fun saveRiderPersonalInfo(
        firstName: String,
        lastName: String,
        phone: String
    ): Result<String> {
        val cleanFirstName = firstName.trim()
        val cleanLastName = lastName.trim()
        val cleanPhone = phone.trim()

        if (cleanFirstName.isBlank() || cleanLastName.isBlank() || cleanPhone.isBlank()) {
            return Result.failure(IllegalArgumentException("First name, last name, and phone are required."))
        }

        return runCatching {
            userRepository.updateRiderProfile(
                uid = profile.uid,
                email = profile.email,
                firstName = cleanFirstName,
                lastName = cleanLastName,
                phone = cleanPhone
            )
        }.map {
            riderProfile = (riderProfile ?: RiderProfile(
                uid = profile.uid,
                email = profile.email,
                role = FirebaseConstants.ROLE_RIDER,
                status = profile.status
            )).copy(
                firstName = cleanFirstName,
                lastName = cleanLastName,
                phone = cleanPhone
            )
            profileMessage = "Personal information updated."
            "Personal information updated."
        }
    }

    suspend fun saveRiderVehicleInfo(
        vehicleType: String,
        plateNumber: String,
        currentLocation: String
    ): Result<String> {
        val cleanVehicleType = vehicleType.trim()
        val cleanPlateNumber = plateNumber.trim()
        val cleanCurrentLocation = currentLocation.trim()

        if (cleanVehicleType.isBlank() || cleanPlateNumber.isBlank() || cleanCurrentLocation.isBlank()) {
            return Result.failure(IllegalArgumentException("Vehicle type, plate number, and current location are required."))
        }

        return runCatching {
            userRepository.updateRiderVehicleInfo(
                uid = profile.uid,
                vehicleType = cleanVehicleType,
                plateNumber = cleanPlateNumber,
                currentLocation = cleanCurrentLocation
            )
        }.map {
            riderProfile = (riderProfile ?: RiderProfile(
                uid = profile.uid,
                email = profile.email,
                role = FirebaseConstants.ROLE_RIDER,
                status = profile.status
            )).copy(
                vehicleType = cleanVehicleType,
                plateNumber = cleanPlateNumber,
                currentLocation = cleanCurrentLocation
            )
            profileMessage = "Vehicle information updated."
            "Vehicle information updated."
        }
    }

    suspend fun saveRiderAvailability(availabilityStatus: String): Result<String> {
        return runCatching {
            userRepository.updateRiderAvailability(
                uid = profile.uid,
                availabilityStatus = availabilityStatus
            )
        }.map {
            riderProfile = (riderProfile ?: RiderProfile(
                uid = profile.uid,
                email = profile.email,
                role = FirebaseConstants.ROLE_RIDER,
                status = profile.status
            )).copy(availabilityStatus = availabilityStatus)
            profileMessage = "Availability set to $availabilityStatus."
            "Availability set to $availabilityStatus."
        }
    }

    suspend fun changeRiderPassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Result<String> {
        if (currentPassword.isBlank()) {
            return Result.failure(IllegalArgumentException("Current password is required."))
        }
        if (newPassword.length < 6) {
            return Result.failure(IllegalArgumentException("New password must be at least 6 characters."))
        }
        if (newPassword != confirmPassword) {
            return Result.failure(IllegalArgumentException("Confirm password must match the new password."))
        }

        return runCatching {
            authRepository.updatePassword(currentPassword, newPassword)
        }.map {
            profileMessage = "Password updated."
            "Password updated."
        }
    }

    Scaffold(
        topBar = {
            RiderTopHeader(
                riderName = riderName(profile, riderProfile),
                selectedTab = selectedTab
            )
        },
        bottomBar = {
            RiderBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        },
        containerColor = BeeCream
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                when (selectedTab) {
                    RiderTab.Available -> AvailableJobsScreen(
                        jobs = availableJobs,
                        onAccept = ::acceptJob
                    )
                    RiderTab.Current -> CurrentDeliveryScreen(
                        job = currentJob,
                        stageIndex = currentStageIndex,
                        onMoveNext = ::moveToNextStatus
                    )
                    RiderTab.History -> HistoryScreen(jobs = historyJobs)
                RiderTab.Profile -> RiderProfileScreen(
                    profile = profile,
                    riderProfile = riderProfile,
                    message = profileMessage,
                    onEditProfile = { activeProfileDialog = RiderProfileDialog.PersonalInformation },
                    onOpenDialog = {
                        profileMessage = null
                        activeProfileDialog = it
                    },
                    onLogout = onLogout
                )
            }
        }
    }

    when (activeProfileDialog) {
        RiderProfileDialog.PersonalInformation -> RiderProfileEditDialog(
            profile = profile,
            riderProfile = riderProfile,
            onDismiss = { activeProfileDialog = null },
            onSave = ::saveRiderPersonalInfo,
            onSaved = { activeProfileDialog = null }
        )
        RiderProfileDialog.VehicleInformation -> RiderVehicleInformationDialog(
            riderProfile = riderProfile,
            onDismiss = { activeProfileDialog = null },
            onSave = ::saveRiderVehicleInfo,
            onSaved = { activeProfileDialog = null }
        )
        RiderProfileDialog.AvailabilitySettings -> RiderAvailabilityDialog(
            availabilityStatus = riderProfile?.availabilityStatus?.ifBlank { "Offline" } ?: "Offline",
            onDismiss = { activeProfileDialog = null },
            onSave = ::saveRiderAvailability,
            onSaved = { activeProfileDialog = null }
        )
        RiderProfileDialog.ChangePassword -> ChangePasswordDialog(
            onDismiss = { activeProfileDialog = null },
            onSave = ::changeRiderPassword,
            onSaved = { activeProfileDialog = null }
        )
        RiderProfileDialog.HelpSupport -> RiderSupportDialog(
            onDismiss = { activeProfileDialog = null }
        )
        null -> Unit
    }
}
}

@Composable
private fun RiderTopHeader(
    riderName: String,
    selectedTab: RiderTab
) {
    Surface(
        color = BeePrimaryYellow,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HonestbeeLogo(modifier = Modifier.size(42.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (selectedTab == RiderTab.Available) {
                        "Good day, $riderName"
                    } else {
                        selectedTab.title
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = BeeDarkText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Android-only deliveries",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BeeDarkText.copy(alpha = 0.76f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = {},
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Icon(
                    imageVector = Icons.Outlined.NotificationsNone,
                    contentDescription = "Notifications",
                    tint = BeeDarkText
                )
            }
        }
    }
}

@Composable
private fun RiderBottomBar(
    selectedTab: RiderTab,
    onTabSelected: (RiderTab) -> Unit
) {
    NavigationBar(
        modifier = Modifier.navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        RiderTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BeeDarkText,
                    selectedTextColor = BeeDarkText,
                    indicatorColor = BeePrimaryYellow,
                    unselectedIconColor = BeeMuted,
                    unselectedTextColor = BeeMuted
                )
            )
        }
    }
}

@Composable
private fun AvailableJobsScreen(
    jobs: List<SampleRiderJob>,
    onAccept: (SampleRiderJob) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ScreenTitle("Available Jobs")

        if (jobs.isEmpty()) {
            EmptyRiderCard("No jobs available", "Accepted jobs move to Current Delivery.")
        } else {
            jobs.forEach { job ->
                AvailableJobCard(
                    job = job,
                    onAccept = { onAccept(job) }
                )
            }
        }
    }
}

@Composable
private fun CurrentDeliveryScreen(
    job: SampleRiderJob?,
    stageIndex: Int,
    onMoveNext: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ScreenTitle("Current Delivery")

        if (job == null) {
            EmptyRiderCard("No current delivery", "Accept a job from Available Jobs to begin.")
            return@Column
        }

        HonestbeeCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HonestbeeRemoteImage(
                        imageUrl = riderJobStoreImageUrl(job),
                        contentDescription = "${job.storeName} image",
                        modifier = Modifier.size(54.dp),
                        icon = Icons.Outlined.LocalShipping
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = job.orderId,
                            style = MaterialTheme.typography.titleMedium,
                            color = BeeDarkText,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = job.storeName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = BeeMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    StatusChip(status = deliveryStages[stageIndex])
                }

                InfoRow("Customer", job.customerName)
                InfoRow("Address", job.deliveryAddress)
                InfoRow("Total amount", formatSamplePeso(orderTotal(job.orderId)))
                InfoRow("Earning", formatSamplePeso(job.payout))
            }
        }

        HonestbeeCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Delivery status",
                    style = MaterialTheme.typography.titleMedium,
                    color = BeeDarkText,
                    fontWeight = FontWeight.SemiBold
                )
                deliveryStages.forEachIndexed { index, stage ->
                    TimelineRow(
                        title = stage,
                        isComplete = index <= stageIndex,
                        isCurrent = index == stageIndex
                    )
                }
                HonestbeeButton(
                    text = if (stageIndex == deliveryStages.lastIndex) {
                        "Delivery Completed"
                    } else {
                        "Move to ${deliveryStages[stageIndex + 1]}"
                    },
                    enabled = stageIndex < deliveryStages.lastIndex,
                    onClick = onMoveNext
                )
            }
        }
    }
}

@Composable
private fun HistoryScreen(jobs: List<SampleRiderJob>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ScreenTitle("History")

        if (jobs.isEmpty()) {
            EmptyRiderCard("No delivery history", "Completed deliveries will appear here.")
        } else {
            jobs.forEach { job ->
                HistoryJobCard(job = job)
            }
        }
    }
}

@Composable
private fun RiderProfileScreen(
    profile: SessionProfile,
    riderProfile: RiderProfile?,
    message: String?,
    onEditProfile: () -> Unit,
    onOpenDialog: (RiderProfileDialog) -> Unit,
    onLogout: () -> Unit
) {
    val name = riderName(profile, riderProfile)
    val availability = riderProfile?.availabilityStatus?.ifBlank { "Offline" } ?: "Offline"

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ScreenTitle("Profile")

        HonestbeeCard {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(82.dp)
                        .clip(CircleShape)
                        .background(BeePrimaryYellow),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials(name),
                        style = MaterialTheme.typography.headlineSmall,
                        color = BeeDarkText,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    color = BeeDarkText,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = profile.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = BeeMuted,
                    textAlign = TextAlign.Center
                )
                StatusChip(status = riderProfile?.status?.ifBlank { profile.status } ?: profile.status)
                StatusChip(status = availability)
                HonestbeeOutlinedButton(
                    text = "Edit Profile",
                    onClick = onEditProfile,
                    fullWidth = false
                )
            }
        }

        message?.let {
            ProfileMessageCard(message = it)
        }

        HonestbeeCard {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                ProfileMenuRow(
                    icon = Icons.Outlined.AccountCircle,
                    title = "Personal Information",
                    subtitle = "Name, phone, and email",
                    onClick = { onOpenDialog(RiderProfileDialog.PersonalInformation) }
                )
                ProfileMenuRow(
                    icon = Icons.Outlined.TwoWheeler,
                    title = "Vehicle Information",
                    subtitle = riderProfile?.vehicleType?.takeIf { it.isNotBlank() } ?: "Vehicle, plate number, and location",
                    onClick = { onOpenDialog(RiderProfileDialog.VehicleInformation) }
                )
                ProfileMenuRow(
                    icon = Icons.Outlined.Settings,
                    title = "Availability Settings",
                    subtitle = availability,
                    onClick = { onOpenDialog(RiderProfileDialog.AvailabilitySettings) }
                )
                ProfileMenuRow(
                    icon = Icons.Outlined.Lock,
                    title = "Change Password",
                    subtitle = "Update your sign-in password",
                    onClick = { onOpenDialog(RiderProfileDialog.ChangePassword) }
                )
                ProfileMenuRow(
                    icon = Icons.Outlined.HelpOutline,
                    title = "Help / Support",
                    subtitle = "Delivery rules and rider FAQ",
                    onClick = { onOpenDialog(RiderProfileDialog.HelpSupport) }
                )
                ProfileMenuRow(
                    icon = Icons.Outlined.Logout,
                    title = "Logout",
                    subtitle = "Sign out of this account",
                    onClick = onLogout
                )
            }
        }

        HonestbeeCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileInfoRow(Icons.Outlined.AccountCircle, "Phone", riderProfile?.phone?.ifBlank { "No phone yet" } ?: "No phone yet")
                ProfileInfoRow(Icons.Outlined.TwoWheeler, "Vehicle type", riderProfile?.vehicleType?.ifBlank { "No vehicle yet" } ?: "No vehicle yet")
                ProfileInfoRow(Icons.Outlined.TwoWheeler, "Plate number", riderProfile?.plateNumber?.ifBlank { "No plate yet" } ?: "No plate yet")
                ProfileInfoRow(Icons.Outlined.LocationOn, "Current location", riderProfile?.currentLocation?.ifBlank { "No location yet" } ?: "No location yet")
                ProfileInfoRow(Icons.Outlined.Person, "Profile source", "${FirebaseConstants.RIDERS}/${profile.uid.take(6)}")
            }
        }
    }
}

@Composable
private fun RiderProfileEditDialog(
    profile: SessionProfile,
    riderProfile: RiderProfile?,
    onDismiss: () -> Unit,
    onSave: suspend (String, String, String) -> Result<String>,
    onSaved: () -> Unit
) {
    val fallbackName = riderName(profile, riderProfile)
    val scope = rememberCoroutineScope()
    var firstName by rememberSaveable(riderProfile?.uid) {
        mutableStateOf(riderProfile?.firstName?.ifBlank { fallbackName.substringBefore(" ") } ?: fallbackName.substringBefore(" "))
    }
    var lastName by rememberSaveable(riderProfile?.uid) {
        mutableStateOf(riderProfile?.lastName?.ifBlank { fallbackName.substringAfter(" ", "") } ?: fallbackName.substringAfter(" ", ""))
    }
    var phone by rememberSaveable(riderProfile?.uid) {
        mutableStateOf(riderProfile?.phone.orEmpty())
    }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isSaving by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = {
            Text(
                text = "Personal Information",
                color = BeeDarkText,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HonestbeeTextField(firstName, { firstName = it }, placeholder = "First name")
                HonestbeeTextField(lastName, { lastName = it }, placeholder = "Last name")
                HonestbeeTextField(phone, { phone = it }, placeholder = "Phone")
                HonestbeeTextField(
                    value = profile.email,
                    onValueChange = {},
                    placeholder = "Email",
                    enabled = false
                )
                errorMessage?.let {
                    DialogStatusText(message = it, isError = true)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving,
                onClick = {
                    if (isSaving) return@TextButton
                    errorMessage = null
                    isSaving = true
                    scope.launch {
                        val result = onSave(firstName, lastName, phone)
                        isSaving = false
                        result.onSuccess { onSaved() }
                            .onFailure { errorMessage = it.message ?: "Could not update personal information." }
                    }
                }
            ) {
                Text(
                    text = if (isSaving) "Saving..." else "Save",
                    color = BeeHoneyYellow,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isSaving,
                onClick = onDismiss
            ) {
                Text("Cancel", color = BeeMuted)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp)
    )
}

@Composable
private fun RiderVehicleInformationDialog(
    riderProfile: RiderProfile?,
    onDismiss: () -> Unit,
    onSave: suspend (String, String, String) -> Result<String>,
    onSaved: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var vehicleType by rememberSaveable(riderProfile?.uid) {
        mutableStateOf(riderProfile?.vehicleType.orEmpty())
    }
    var plateNumber by rememberSaveable(riderProfile?.uid) {
        mutableStateOf(riderProfile?.plateNumber.orEmpty())
    }
    var currentLocation by rememberSaveable(riderProfile?.uid) {
        mutableStateOf(riderProfile?.currentLocation.orEmpty())
    }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isSaving by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = {
            Text(
                text = "Vehicle Information",
                color = BeeDarkText,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HonestbeeTextField(vehicleType, { vehicleType = it }, placeholder = "Vehicle type")
                HonestbeeTextField(plateNumber, { plateNumber = it }, placeholder = "Plate number")
                HonestbeeTextField(
                    value = currentLocation,
                    onValueChange = { currentLocation = it },
                    placeholder = "Current location",
                    singleLine = false
                )
                errorMessage?.let {
                    DialogStatusText(message = it, isError = true)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving,
                onClick = {
                    if (isSaving) return@TextButton
                    errorMessage = null
                    isSaving = true
                    scope.launch {
                        val result = onSave(vehicleType, plateNumber, currentLocation)
                        isSaving = false
                        result.onSuccess { onSaved() }
                            .onFailure { errorMessage = it.message ?: "Could not update vehicle information." }
                    }
                }
            ) {
                Text(
                    text = if (isSaving) "Saving..." else "Save",
                    color = BeeHoneyYellow,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isSaving,
                onClick = onDismiss
            ) {
                Text("Cancel", color = BeeMuted)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp)
    )
}

@Composable
private fun RiderAvailabilityDialog(
    availabilityStatus: String,
    onDismiss: () -> Unit,
    onSave: suspend (String) -> Result<String>,
    onSaved: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var selected by rememberSaveable(availabilityStatus) { mutableStateOf(availabilityStatus) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isSaving by rememberSaveable { mutableStateOf(false) }
    val options = listOf("Available", "Busy", "Offline")

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = {
            Text(
                text = "Availability Settings",
                color = BeeDarkText,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { option ->
                    RadioOptionRow(
                        title = option,
                        subtitle = when (option) {
                            "Available" -> "Ready to receive delivery jobs"
                            "Busy" -> "Temporarily unavailable for new jobs"
                            else -> "Signed in but off duty"
                        },
                        selected = selected == option,
                        onClick = { selected = option }
                    )
                }
                errorMessage?.let {
                    DialogStatusText(message = it, isError = true)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving,
                onClick = {
                    if (isSaving) return@TextButton
                    errorMessage = null
                    isSaving = true
                    scope.launch {
                        val result = onSave(selected)
                        isSaving = false
                        result.onSuccess { onSaved() }
                            .onFailure { errorMessage = it.message ?: "Could not update availability." }
                    }
                }
            ) {
                Text(
                    text = if (isSaving) "Saving..." else "Save",
                    color = BeeHoneyYellow,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isSaving,
                onClick = onDismiss
            ) {
                Text("Cancel", color = BeeMuted)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp)
    )
}

@Composable
private fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onSave: suspend (String, String, String) -> Result<String>,
    onSaved: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isSaving by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = {
            Text(
                text = "Change Password",
                color = BeeDarkText,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HonestbeePasswordField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    placeholder = "Current password"
                )
                HonestbeePasswordField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    placeholder = "New password"
                )
                HonestbeePasswordField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = "Confirm new password"
                )
                errorMessage?.let {
                    DialogStatusText(message = it, isError = true)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving,
                onClick = {
                    if (isSaving) return@TextButton
                    errorMessage = null
                    isSaving = true
                    scope.launch {
                        val result = onSave(currentPassword, newPassword, confirmPassword)
                        isSaving = false
                        result.onSuccess { onSaved() }
                            .onFailure { errorMessage = it.message ?: "Could not change password." }
                    }
                }
            ) {
                Text(
                    text = if (isSaving) "Saving..." else "Save",
                    color = BeeHoneyYellow,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isSaving,
                onClick = onDismiss
            ) {
                Text("Cancel", color = BeeMuted)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp)
    )
}

@Composable
private fun RiderSupportDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Help / Support",
                color = BeeDarkText,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HelpFaqItem("Delivery rules", "Check item counts, keep orders sealed, and update each delivery stage in order.")
                HelpFaqItem("Contact support", "Email support@honestbee.app with your rider account email and order number.")
                HelpFaqItem("When should I mark Busy?", "Use Busy when you cannot accept another delivery but are still signed in.")
                HelpFaqItem("What if the customer is unavailable?", "Wait briefly, contact support, and keep the order status unchanged until advised.")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = BeeHoneyYellow, fontWeight = FontWeight.SemiBold)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp)
    )
}

@Composable
private fun RadioOptionRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (selected) BeeNavigationSelected else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (selected) BeeHoneyYellow else MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = onClick)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = BeeDarkText,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = BeeMuted
                )
            }
        }
    }
}

@Composable
private fun HelpFaqItem(
    title: String,
    answer: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = BeeDarkText,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = answer,
            style = MaterialTheme.typography.bodyMedium,
            color = BeeMuted
        )
    }
}

@Composable
private fun ProfileMessageCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = BeeSuccess.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, BeeSuccess.copy(alpha = 0.30f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = BeeSuccess,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun DialogStatusText(
    message: String,
    isError: Boolean
) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        color = if (isError) BeeError else BeeSuccess,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun AvailableJobCard(
    job: SampleRiderJob,
    onAccept: () -> Unit
) {
    HonestbeeCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HonestbeeRemoteImage(
                    imageUrl = riderJobStoreImageUrl(job),
                    contentDescription = "${job.storeName} image",
                    modifier = Modifier.size(54.dp),
                    icon = Icons.Outlined.LocalShipping
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.orderId,
                        style = MaterialTheme.typography.titleMedium,
                        color = BeeDarkText,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = job.storeName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = BeeDarkText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                StatusChip(status = "Available")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SmallMeta("Distance", "${String.format("%.1f", job.distanceKm)} km")
                SmallMeta("Earning", formatSamplePeso(job.payout))
            }

            HonestbeeButton(
                text = "Accept",
                onClick = onAccept
            )
        }
    }
}

@Composable
private fun HistoryJobCard(job: SampleRiderJob) {
    HonestbeeCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HonestbeeRemoteImage(
                    imageUrl = riderJobStoreImageUrl(job),
                    contentDescription = "${job.storeName} image",
                    modifier = Modifier.size(54.dp),
                    icon = Icons.Outlined.LocalShipping
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.orderId,
                        style = MaterialTheme.typography.titleMedium,
                        color = BeeDarkText,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = job.storeName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = BeeMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                StatusChip(status = job.status)
            }

            InfoRow("Date", "May 26, 2024")
            InfoRow("Earning", formatSamplePeso(job.payout))
        }
    }
}

private fun riderJobStoreImageUrl(job: SampleRiderJob): String {
    return AndroidSampleData.stores
        .firstOrNull { it.storeName == job.storeName }
        ?.imageUrl
        .orEmpty()
}

@Composable
private fun TimelineRow(
    title: String,
    isComplete: Boolean,
    isCurrent: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (isComplete) BeePrimaryYellow else BeeCream),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = if (isComplete) BeeDarkText else BeeMuted,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isComplete) BeeDarkText else BeeMuted,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
            )
            if (isCurrent) {
                Text(
                    text = "Current step",
                    style = MaterialTheme.typography.bodySmall,
                    color = BeeHoneyYellow,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp)
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BeeHoneyYellow,
                modifier = Modifier.size(22.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = BeeDarkText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = BeeMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = BeeMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(BeeNavigationSelected),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BeeHoneyYellow,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = BeeMuted
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = BeeDarkText,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = BeeMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = BeeDarkText,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SmallMeta(
    label: String,
    value: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = BeeMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = BeeDarkText,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EmptyRiderCard(
    title: String,
    message: String
) {
    HonestbeeCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.LocalShipping,
                contentDescription = null,
                tint = BeeHoneyYellow,
                modifier = Modifier.size(34.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = BeeDarkText,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = BeeMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ScreenTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = BeeDarkText
    )
}

private fun riderName(
    profile: SessionProfile,
    riderProfile: RiderProfile?
): String {
    val profileName = "${riderProfile?.firstName.orEmpty()} ${riderProfile?.lastName.orEmpty()}".trim()
    return profileName.ifBlank {
        profile.displayName.ifBlank {
            profile.email.substringBefore("@").ifBlank { "Rider" }
        }
    }
}

private fun initials(name: String): String {
    val parts = name.split(" ").filter { it.isNotBlank() }
    return parts.take(2).joinToString("") { it.first().uppercaseChar().toString() }
        .ifBlank { "HB" }
}

private fun orderTotal(orderId: String): Double {
    return AndroidSampleData.orders.firstOrNull { it.orderId == orderId }?.totalAmount ?: 0.0
}

private enum class RiderTab(
    val title: String,
    val icon: ImageVector
) {
    Available("Available", Icons.Outlined.Home),
    Current("Current", Icons.Outlined.LocalShipping),
    History("History", Icons.Outlined.History),
    Profile("Profile", Icons.Outlined.Person)
}

private enum class RiderProfileDialog {
    PersonalInformation,
    VehicleInformation,
    AvailabilitySettings,
    ChangePassword,
    HelpSupport
}
