package com.example.honestbeeapp.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Summarize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.honestbeeapp.data.model.AppUser
import com.example.honestbeeapp.data.model.SessionProfile
import com.example.honestbeeapp.data.sample.AndroidSampleData
import com.example.honestbeeapp.ui.components.HonestbeeButton
import com.example.honestbeeapp.ui.components.HonestbeeCard
import com.example.honestbeeapp.ui.components.HonestbeeOutlinedButton
import com.example.honestbeeapp.ui.components.SectionHeader
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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AdminMainScreen(
    profile: SessionProfile,
    onLogout: () -> Unit,
    firestore: FirebaseFirestore = remember { FirebaseFirestore.getInstance() }
) {
    var selectedTab by rememberSaveable { mutableStateOf(AdminTab.Dashboard) }
    var users by remember { mutableStateOf<List<AppUser>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var refreshKey by rememberSaveable { mutableIntStateOf(0) }
    var selectedUser by remember { mutableStateOf<AppUser?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(firestore, refreshKey) {
        isLoading = true
        errorMessage = null
        runCatching { loadSharedUsers(firestore) }
            .onSuccess { users = it }
            .onFailure { errorMessage = it.localizedMessage ?: "Could not load users." }
        isLoading = false
    }

    fun updateApproval(user: AppUser, status: String) {
        scope.launch {
            isLoading = true
            errorMessage = null
            runCatching {
                updateSharedApprovalStatus(
                    firestore = firestore,
                    user = user,
                    status = status
                )
                loadSharedUsers(firestore)
            }.onSuccess {
                users = it
            }.onFailure {
                errorMessage = it.localizedMessage ?: "Could not update approval."
            }
            isLoading = false
        }
    }

    fun updateUserStatus(user: AppUser, status: String) {
        scope.launch {
            isLoading = true
            errorMessage = null
            runCatching {
                updateSharedApprovalStatus(
                    firestore = firestore,
                    user = user,
                    status = status
                )
                loadSharedUsers(firestore)
            }.onSuccess {
                users = it
                selectedUser = null
            }.onFailure {
                errorMessage = it.localizedMessage ?: "Could not update user status."
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            AdminTopHeader(
                selectedTab = selectedTab,
                adminLabel = profile.email.ifBlank { profile.displayName.ifBlank { "Admin" } },
                onRefresh = { refreshKey++ },
                onLogout = onLogout
            )
        },
        bottomBar = {
            AdminBottomBar(
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
                errorMessage?.let { ErrorCard(message = it) }

                if (isLoading && users.isEmpty()) {
                    LoadingCard()
                } else {
                    when (selectedTab) {
                    AdminTab.Dashboard -> AdminDashboardScreen(
                        users = users,
                        onUserClick = { selectedUser = it }
                    )
                    AdminTab.Users -> AdminUsersScreen(
                        users = users,
                        onUserClick = { selectedUser = it }
                    )
                        AdminTab.Approvals -> AdminApprovalsScreen(
                            users = users,
                            isUpdating = isLoading,
                            onApprove = { updateApproval(it, FirebaseConstants.STATUS_APPROVED) },
                            onReject = { updateApproval(it, FirebaseConstants.STATUS_REJECTED) }
                        )
                        AdminTab.Reports -> AdminReportsScreen()
                    }
                }
            }
        }
    }

    selectedUser?.let { user ->
        UserDetailsDialog(
            user = user,
            isUpdating = isLoading,
            onDismiss = { selectedUser = null },
            onUpdateStatus = { status -> updateUserStatus(user, status) }
        )
    }
}

@Composable
private fun AdminTopHeader(
    selectedTab: AdminTab,
    adminLabel: String,
    onRefresh: () -> Unit,
    onLogout: () -> Unit
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
                    text = selectedTab.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = BeeDarkText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Signed in as $adminLabel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BeeDarkText.copy(alpha = 0.76f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "Refresh users",
                        tint = BeeDarkText
                    )
                }
                IconButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Logout,
                        contentDescription = "Logout",
                        tint = BeeDarkText
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminBottomBar(
    selectedTab: AdminTab,
    onTabSelected: (AdminTab) -> Unit
) {
    NavigationBar(
        modifier = Modifier.navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        AdminTab.entries.forEach { tab ->
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
private fun AdminDashboardScreen(
    users: List<AppUser>,
    onUserClick: (AppUser) -> Unit
) {
    val customers = users.count { it.role.equals(FirebaseConstants.ROLE_CUSTOMER, ignoreCase = true) }
    val merchants = users.count { it.role.equals(FirebaseConstants.ROLE_MERCHANT, ignoreCase = true) }
    val riders = users.count { it.role.equals(FirebaseConstants.ROLE_RIDER, ignoreCase = true) }
    val pending = users.count { it.status.equals(FirebaseConstants.STATUS_PENDING, ignoreCase = true) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ScreenTitle("Admin Dashboard")
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryCard("Customers", customers.toString(), "Shared users")
            SummaryCard("Merchants", merchants.toString(), "Shared users")
            SummaryCard("Riders", riders.toString(), "Shared users")
            SummaryCard("Pending", pending.toString(), "Needs review")
        }

        SectionHeader(title = "Recent accounts")
        users.take(5).forEach { user ->
            UserListCard(
                user = user,
                onViewDetails = { onUserClick(user) }
            )
        }
    }
}

@Composable
private fun AdminUsersScreen(
    users: List<AppUser>,
    onUserClick: (AppUser) -> Unit
) {
    var selectedRole by rememberSaveable { mutableStateOf("All Roles") }
    val filters = listOf("All Roles", "Customer", "Merchant", "Rider", "Admin")
    val filteredUsers = users.filter { user ->
        selectedRole == "All Roles" || user.role.equals(selectedRole.lowercase(), ignoreCase = true)
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ScreenTitle("Users")
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { filter ->
                PillChip(
                    text = filter,
                    selected = selectedRole == filter,
                    onClick = { selectedRole = filter }
                )
            }
        }

        if (filteredUsers.isEmpty()) {
            EmptyAdminCard("No users found", "Shared users/${FirebaseConstants.USERS} has no matching accounts.")
        } else {
            filteredUsers.forEach { user ->
                UserListCard(
                    user = user,
                    onViewDetails = { onUserClick(user) }
                )
            }
        }
    }
}

@Composable
private fun AdminApprovalsScreen(
    users: List<AppUser>,
    isUpdating: Boolean,
    onApprove: (AppUser) -> Unit,
    onReject: (AppUser) -> Unit
) {
    var selectedType by rememberSaveable { mutableStateOf(FirebaseConstants.ROLE_MERCHANT) }
    val pendingUsers = users.filter {
        it.status.equals(FirebaseConstants.STATUS_PENDING, ignoreCase = true) &&
            it.role.equals(selectedType, ignoreCase = true)
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ScreenTitle("Approvals")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PillChip(
                text = "Merchants",
                selected = selectedType == FirebaseConstants.ROLE_MERCHANT,
                onClick = { selectedType = FirebaseConstants.ROLE_MERCHANT }
            )
            PillChip(
                text = "Riders",
                selected = selectedType == FirebaseConstants.ROLE_RIDER,
                onClick = { selectedType = FirebaseConstants.ROLE_RIDER }
            )
        }

        if (pendingUsers.isEmpty()) {
            EmptyAdminCard("No pending ${selectedType}s", "Pending merchant and rider accounts will appear here.")
        } else {
            pendingUsers.forEach { user ->
                ApprovalUserCard(
                    user = user,
                    isUpdating = isUpdating,
                    onApprove = { onApprove(user) },
                    onReject = { onReject(user) }
                )
            }
        }
    }
}

@Composable
private fun AdminReportsScreen() {
    val activeAndroidOrders = AndroidSampleData.orders.count {
        it.status != FirebaseConstants.STATUS_COMPLETED &&
            it.status != FirebaseConstants.STATUS_CANCELLED
    }
    val androidSales = AndroidSampleData.orders
        .filter { it.status != FirebaseConstants.STATUS_CANCELLED }
        .sumOf { it.totalAmount }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ScreenTitle("Reports")
        ReportCard("Android stores", AndroidSampleData.stores.size.toString(), FirebaseConstants.ANDROID_STORES)
        ReportCard("Android products", AndroidSampleData.products.size.toString(), FirebaseConstants.ANDROID_PRODUCTS)
        ReportCard("Android orders", AndroidSampleData.orders.size.toString(), FirebaseConstants.ANDROID_ORDERS)
        ReportCard("Active Android orders", activeAndroidOrders.toString(), "Android-only order monitor")
        ReportCard("Sample Android sales", formatSamplePeso(androidSales), "Does not include website orders")
    }
}

@Composable
private fun UserListCard(
    user: AppUser,
    onViewDetails: () -> Unit
) {
    HonestbeeCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Avatar(initials = initials(user))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = fullName(user),
                        style = MaterialTheme.typography.titleMedium,
                        color = BeeDarkText,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = user.email.ifBlank { "No email" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = BeeMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RoleChip(role = user.role)
                        StatusChip(status = user.status.ifBlank { FirebaseConstants.STATUS_PENDING })
                    }
                }
            }
            HonestbeeOutlinedButton(
                text = "View details",
                onClick = onViewDetails
            )
        }
    }
}

@Composable
private fun UserDetailsDialog(
    user: AppUser,
    isUpdating: Boolean,
    onDismiss: () -> Unit,
    onUpdateStatus: (String) -> Unit
) {
    val statusOptions = listOf(
        FirebaseConstants.STATUS_ACTIVE,
        FirebaseConstants.STATUS_APPROVED,
        FirebaseConstants.STATUS_PENDING,
        FirebaseConstants.STATUS_REJECTED,
        FirebaseConstants.STATUS_DELETED
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = fullName(user),
                color = BeeDarkText,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                UserDetailLine("Email", user.email.ifBlank { "No email" })
                UserDetailLine("Role", user.role.ifBlank { "user" })
                UserDetailLine("Phone", user.phone.ifBlank { "No phone" })
                UserDetailLine("Address", user.address.ifBlank { "No address" })
                StatusChip(status = user.status.ifBlank { FirebaseConstants.STATUS_PENDING })

                Text(
                    text = "Update status",
                    style = MaterialTheme.typography.titleMedium,
                    color = BeeDarkText,
                    fontWeight = FontWeight.SemiBold
                )
                statusOptions.forEach { status ->
                    HonestbeeOutlinedButton(
                        text = status.replaceFirstChar { it.uppercase() },
                        onClick = { onUpdateStatus(status) },
                        enabled = !isUpdating && user.status != status
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = BeeHoneyYellow, fontWeight = FontWeight.SemiBold)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp)
    )
}

@Composable
private fun UserDetailLine(
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
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ApprovalUserCard(
    user: AppUser,
    isUpdating: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    HonestbeeCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Avatar(initials = initials(user))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = fullName(user),
                        style = MaterialTheme.typography.titleMedium,
                        color = BeeDarkText,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = user.email.ifBlank { "No email" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = BeeMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                RoleChip(role = user.role)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HonestbeeButton(
                    text = "Approve",
                    onClick = onApprove,
                    enabled = !isUpdating,
                    modifier = Modifier.weight(1f)
                )
                HonestbeeOutlinedButton(
                    text = "Reject",
                    onClick = onReject,
                    enabled = !isUpdating,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = BeeSuccess,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Approve or reject updates shared profile status only.",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = BeeMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = null,
                    tint = BeeError,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    note: String
) {
    HonestbeeCard(modifier = Modifier.size(width = 142.dp, height = 112.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = BeeMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = BeeDarkText,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ReportCard(
    title: String,
    value: String,
    note: String
) {
    HonestbeeCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(BeeNavigationSelected),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Apps,
                    contentDescription = null,
                    tint = BeeHoneyYellow,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = BeeDarkText,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall,
                    color = BeeMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = BeeDarkText,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RoleChip(role: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = BeeNavigationSelected,
        border = BorderStroke(1.dp, BeePrimaryYellow.copy(alpha = 0.45f))
    ) {
        Text(
            text = role.ifBlank { "user" }.replaceFirstChar { it.uppercase() },
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            color = BeeDarkText,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun Avatar(initials: String) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(BeePrimaryYellow),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleMedium,
            color = BeeDarkText,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PillChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) BeePrimaryYellow else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) BeeHoneyYellow else MaterialTheme.colorScheme.outline
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) BeeDarkText else BeeMuted,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LoadingCard() {
    HonestbeeCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = BeeHoneyYellow,
                strokeWidth = 2.dp
            )
            Text(
                text = "Loading shared users...",
                style = MaterialTheme.typography.bodyMedium,
                color = BeeMuted
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = BeeError.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, BeeError.copy(alpha = 0.30f))
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = BeeError
        )
    }
}

@Composable
private fun EmptyAdminCard(
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
                imageVector = Icons.Outlined.Groups,
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

private suspend fun loadSharedUsers(firestore: FirebaseFirestore): List<AppUser> {
    return firestore.collection(FirebaseConstants.USERS)
        .get()
        .await()
        .documents
        .mapNotNull { document ->
            document.toObject(AppUser::class.java)?.let { user ->
                user.copy(uid = user.uid.ifBlank { document.id })
            }
        }
        .sortedWith(compareBy<AppUser> { roleSortOrder(it.role) }.thenBy { fullName(it) })
}

private suspend fun updateSharedApprovalStatus(
    firestore: FirebaseFirestore,
    user: AppUser,
    status: String
) {
    val uid = user.uid
    require(uid.isNotBlank()) { "User id is missing." }

    val updates = mapOf(
        "status" to status,
        "updatedAt" to FieldValue.serverTimestamp()
    )

    firestore.collection(FirebaseConstants.USERS)
        .document(uid)
        .update(updates)
        .await()

    val profileCollection = when (user.role.trim().lowercase()) {
        FirebaseConstants.ROLE_CUSTOMER -> FirebaseConstants.CUSTOMERS
        FirebaseConstants.ROLE_MERCHANT -> FirebaseConstants.MERCHANTS
        FirebaseConstants.ROLE_RIDER -> FirebaseConstants.RIDERS
        else -> null
    }

    if (profileCollection != null) {
        firestore.collection(profileCollection)
            .document(uid)
            .set(updates, SetOptions.merge())
            .await()
    }
}

private fun fullName(user: AppUser): String {
    val firstLast = "${user.firstName} ${user.lastName}".trim()
    return firstLast.ifBlank {
        user.username.ifBlank {
            user.email.substringBefore("@").ifBlank { "Unknown user" }
        }
    }
}

private fun initials(user: AppUser): String {
    val parts = fullName(user).split(" ").filter { it.isNotBlank() }
    return parts.take(2).joinToString("") { it.first().uppercaseChar().toString() }
        .ifBlank { "HB" }
}

private fun roleSortOrder(role: String): Int {
    return when (role.trim().lowercase()) {
        FirebaseConstants.ROLE_ADMIN -> 0
        FirebaseConstants.ROLE_CUSTOMER -> 1
        FirebaseConstants.ROLE_MERCHANT -> 2
        FirebaseConstants.ROLE_RIDER -> 3
        else -> 4
    }
}

private enum class AdminTab(
    val title: String,
    val icon: ImageVector
) {
    Dashboard("Dashboard", Icons.Outlined.Dashboard),
    Users("Users", Icons.Outlined.Groups),
    Approvals("Approvals", Icons.Outlined.HowToReg),
    Reports("Reports", Icons.Outlined.Summarize)
}
