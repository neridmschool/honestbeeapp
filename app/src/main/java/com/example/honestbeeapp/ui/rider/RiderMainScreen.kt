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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import com.example.honestbeeapp.data.sample.AndroidSampleData
import com.example.honestbeeapp.data.sample.SampleRiderJob
import com.example.honestbeeapp.ui.components.HonestbeeButton
import com.example.honestbeeapp.ui.components.HonestbeeCard
import com.example.honestbeeapp.ui.components.HonestbeeOutlinedButton
import com.example.honestbeeapp.ui.components.HonestbeeTextField
import com.example.honestbeeapp.ui.components.StatusChip
import com.example.honestbeeapp.ui.components.formatSamplePeso
import com.example.honestbeeapp.ui.theme.BeeCream
import com.example.honestbeeapp.ui.theme.BeeDarkText
import com.example.honestbeeapp.ui.theme.BeeHoneyYellow
import com.example.honestbeeapp.ui.theme.BeeMuted
import com.example.honestbeeapp.ui.theme.BeeNavigationSelected
import com.example.honestbeeapp.ui.theme.BeePrimaryYellow
import com.example.honestbeeapp.ui.theme.BeeSuccess
import com.example.honestbeeapp.util.FirebaseConstants
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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
    var isEditingProfile by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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

    fun saveRiderProfile(
        firstName: String,
        lastName: String,
        phone: String,
        vehicleType: String,
        currentLocation: String
    ) {
        scope.launch {
            val cleanFirstName = firstName.trim()
            val cleanLastName = lastName.trim()
            val cleanPhone = phone.trim()
            val cleanVehicleType = vehicleType.trim()
            val cleanCurrentLocation = currentLocation.trim()
            val displayName = "$cleanFirstName $cleanLastName".trim()
            val userUpdates = mapOf(
                "firstName" to cleanFirstName,
                "lastName" to cleanLastName,
                "username" to displayName,
                "phone" to cleanPhone,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val riderUpdates = mapOf(
                "uid" to profile.uid,
                "email" to profile.email,
                "firstName" to cleanFirstName,
                "lastName" to cleanLastName,
                "phone" to cleanPhone,
                "vehicleType" to cleanVehicleType,
                "currentLocation" to cleanCurrentLocation,
                "role" to FirebaseConstants.ROLE_RIDER,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            runCatching {
                firestore.collection(FirebaseConstants.USERS)
                    .document(profile.uid)
                    .set(userUpdates, SetOptions.merge())
                    .await()
                firestore.collection(FirebaseConstants.RIDERS)
                    .document(profile.uid)
                    .set(riderUpdates, SetOptions.merge())
                    .await()
            }.onSuccess {
                riderProfile = (riderProfile ?: RiderProfile(
                    uid = profile.uid,
                    email = profile.email,
                    role = FirebaseConstants.ROLE_RIDER,
                    status = profile.status
                )).copy(
                    firstName = cleanFirstName,
                    lastName = cleanLastName,
                    phone = cleanPhone,
                    vehicleType = cleanVehicleType,
                    currentLocation = cleanCurrentLocation
                )
                isEditingProfile = false
            }
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
                    onEditProfile = { isEditingProfile = true },
                    onLogout = onLogout
                )
            }
        }
    }

    if (isEditingProfile) {
        RiderProfileEditDialog(
            profile = profile,
            riderProfile = riderProfile,
            onDismiss = { isEditingProfile = false },
            onSave = ::saveRiderProfile
        )
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
    onEditProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val name = riderName(profile, riderProfile)

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
                HonestbeeOutlinedButton(
                    text = "Edit Profile",
                    onClick = onEditProfile,
                    fullWidth = false
                )
            }
        }

        HonestbeeCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileInfoRow(Icons.Outlined.AccountCircle, "Phone", riderProfile?.phone?.ifBlank { "No phone yet" } ?: "No phone yet")
                ProfileInfoRow(Icons.Outlined.TwoWheeler, "Vehicle type", riderProfile?.vehicleType?.ifBlank { "No vehicle yet" } ?: "No vehicle yet")
                ProfileInfoRow(Icons.Outlined.LocationOn, "Current location", riderProfile?.currentLocation?.ifBlank { "No location yet" } ?: "No location yet")
                ProfileInfoRow(Icons.Outlined.Person, "Profile source", "${FirebaseConstants.RIDERS}/${profile.uid.take(6)}")
            }
        }

        HonestbeeButton(
            text = "Logout",
            onClick = onLogout
        )
    }
}

@Composable
private fun RiderProfileEditDialog(
    profile: SessionProfile,
    riderProfile: RiderProfile?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit
) {
    val fallbackName = riderName(profile, riderProfile)
    var firstName by rememberSaveable(riderProfile?.uid) {
        mutableStateOf(riderProfile?.firstName?.ifBlank { fallbackName.substringBefore(" ") } ?: fallbackName.substringBefore(" "))
    }
    var lastName by rememberSaveable(riderProfile?.uid) {
        mutableStateOf(riderProfile?.lastName?.ifBlank { fallbackName.substringAfter(" ", "") } ?: fallbackName.substringAfter(" ", ""))
    }
    var phone by rememberSaveable(riderProfile?.uid) {
        mutableStateOf(riderProfile?.phone.orEmpty())
    }
    var vehicleType by rememberSaveable(riderProfile?.uid) {
        mutableStateOf(riderProfile?.vehicleType.orEmpty())
    }
    var currentLocation by rememberSaveable(riderProfile?.uid) {
        mutableStateOf(riderProfile?.currentLocation.orEmpty())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Rider Profile",
                color = BeeDarkText,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HonestbeeTextField(firstName, { firstName = it }, placeholder = "First name")
                HonestbeeTextField(lastName, { lastName = it }, placeholder = "Last name")
                HonestbeeTextField(phone, { phone = it }, placeholder = "Phone")
                HonestbeeTextField(vehicleType, { vehicleType = it }, placeholder = "Vehicle type")
                HonestbeeTextField(currentLocation, { currentLocation = it }, placeholder = "Current location", singleLine = false)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(firstName, lastName, phone, vehicleType, currentLocation) }
            ) {
                Text("Save", color = BeeHoneyYellow, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = BeeMuted)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp)
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
