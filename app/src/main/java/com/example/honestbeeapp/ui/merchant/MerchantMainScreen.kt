package com.example.honestbeeapp.ui.merchant

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.honestbeeapp.data.model.AndroidOrder
import com.example.honestbeeapp.data.model.AndroidProduct
import com.example.honestbeeapp.data.model.MerchantProfile
import com.example.honestbeeapp.data.model.SessionProfile
import com.example.honestbeeapp.data.repository.AuthRepository
import com.example.honestbeeapp.data.repository.UserRepository
import com.example.honestbeeapp.data.sample.AndroidSampleData
import com.example.honestbeeapp.ui.components.HonestbeeButton
import com.example.honestbeeapp.ui.components.HonestbeeCard
import com.example.honestbeeapp.ui.components.HonestbeeOutlinedButton
import com.example.honestbeeapp.ui.components.HonestbeePasswordField
import com.example.honestbeeapp.ui.components.HonestbeeTextField
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
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MerchantMainScreen(
    profile: SessionProfile,
    onLogout: () -> Unit,
    firestore: FirebaseFirestore = remember { FirebaseFirestore.getInstance() }
) {
    var selectedTab by rememberSaveable { mutableStateOf(MerchantTab.Dashboard) }
    var merchantProfile by remember { mutableStateOf<MerchantProfile?>(null) }
    val products = remember {
        mutableStateListOf<AndroidProduct>().apply {
            addAll(AndroidSampleData.products)
        }
    }
    val orders = remember {
        mutableStateListOf<AndroidOrder>().apply {
            addAll(AndroidSampleData.orders)
        }
    }
    var editingProduct by remember { mutableStateOf<AndroidProduct?>(null) }
    var isAddingProduct by rememberSaveable { mutableStateOf(false) }
    var activeProfileDialog by rememberSaveable { mutableStateOf<MerchantProfileDialog?>(null) }
    var profileMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var bankAccountName by rememberSaveable(profile.uid) { mutableStateOf("") }
    var bankName by rememberSaveable(profile.uid) { mutableStateOf("") }
    var bankAccountNumber by rememberSaveable(profile.uid) { mutableStateOf("") }
    var gcashNumber by rememberSaveable(profile.uid) { mutableStateOf("") }
    var storeOpen by rememberSaveable(profile.uid) { mutableStateOf(true) }
    var acceptingOrders by rememberSaveable(profile.uid) { mutableStateOf(true) }
    var lowStockAlerts by rememberSaveable(profile.uid) { mutableStateOf(true) }
    val userRepository = remember(firestore) { UserRepository(firestore) }
    val authRepository = remember { AuthRepository() }

    LaunchedEffect(profile.uid, firestore) {
        merchantProfile = runCatching {
            firestore.collection(FirebaseConstants.MERCHANTS)
                .document(profile.uid)
                .get()
                .await()
                .toObject(MerchantProfile::class.java)
        }.getOrNull()

        runCatching {
            firestore.collection(FirebaseConstants.ANDROID_MERCHANT_SETTINGS)
                .document(profile.uid)
                .get()
                .await()
        }.onSuccess { document ->
            bankAccountName = document.getString("accountName") ?: bankAccountName
            bankName = document.getString("bankName") ?: bankName
            bankAccountNumber = document.getString("accountNumber") ?: bankAccountNumber
            gcashNumber = document.getString("gcashNumber") ?: gcashNumber
            storeOpen = document.getBoolean("storeOpen") ?: storeOpen
            acceptingOrders = document.getBoolean("acceptingOrders") ?: acceptingOrders
            lowStockAlerts = document.getBoolean("lowStockAlerts") ?: lowStockAlerts
        }
    }

    fun saveProduct(product: AndroidProduct) {
        val index = products.indexOfFirst { it.productId == product.productId }
        if (index >= 0) {
            products[index] = product
        } else {
            products.add(0, product)
        }
        editingProduct = null
        isAddingProduct = false
    }

    fun deleteProduct(product: AndroidProduct) {
        products.removeAll { it.productId == product.productId }
    }

    fun updateOrderStatus(order: AndroidOrder, status: String) {
        val index = orders.indexOfFirst { it.orderId == order.orderId }
        if (index >= 0) {
            orders[index] = order.copy(status = status)
        }
    }

    suspend fun saveMerchantProfile(
        storeName: String,
        ownerName: String,
        phone: String,
        address: String
    ): Result<String> {
        val cleanStoreName = storeName.trim()
        val cleanOwnerName = ownerName.trim()
        val cleanPhone = phone.trim()
        val cleanAddress = address.trim()

        if (
            cleanStoreName.isBlank() ||
            cleanOwnerName.isBlank() ||
            cleanPhone.isBlank() ||
            cleanAddress.isBlank()
        ) {
            return Result.failure(IllegalArgumentException("Store name, owner name, phone, and address are required."))
        }

        return runCatching {
            userRepository.updateMerchantProfile(
                uid = profile.uid,
                email = profile.email,
                storeName = cleanStoreName,
                ownerName = cleanOwnerName,
                phone = cleanPhone,
                address = cleanAddress
            )
        }.map {
            merchantProfile = (merchantProfile ?: MerchantProfile(
                uid = profile.uid,
                email = profile.email,
                role = FirebaseConstants.ROLE_MERCHANT,
                status = profile.status
            )).copy(
                storeName = cleanStoreName,
                ownerName = cleanOwnerName,
                phone = cleanPhone,
                address = cleanAddress
            )
            profileMessage = "Store information updated."
            "Store information updated."
        }
    }

    suspend fun saveMerchantBankInfo(
        accountName: String,
        selectedBankName: String,
        accountNumber: String,
        selectedGcashNumber: String
    ): Result<String> {
        val cleanAccountName = accountName.trim()
        val cleanBankName = selectedBankName.trim()
        val cleanAccountNumber = accountNumber.trim()
        val cleanGcashNumber = selectedGcashNumber.trim()

        if (
            cleanAccountName.isBlank() ||
            cleanBankName.isBlank() ||
            cleanAccountNumber.isBlank() ||
            cleanGcashNumber.isBlank()
        ) {
            return Result.failure(IllegalArgumentException("All bank information fields are required."))
        }

        return runCatching {
            userRepository.updateMerchantBankInfo(
                uid = profile.uid,
                bankInfo = mapOf(
                    "accountName" to cleanAccountName,
                    "bankName" to cleanBankName,
                    "accountNumber" to cleanAccountNumber,
                    "gcashNumber" to cleanGcashNumber
                )
            )
        }.map {
            bankAccountName = cleanAccountName
            bankName = cleanBankName
            bankAccountNumber = cleanAccountNumber
            gcashNumber = cleanGcashNumber
            profileMessage = "Bank information saved."
            "Bank information saved."
        }
    }

    suspend fun saveMerchantSettings(
        isStoreOpen: Boolean,
        isAcceptingOrders: Boolean,
        wantsLowStockAlerts: Boolean
    ): Result<String> {
        return runCatching {
            userRepository.updateMerchantSettings(
                uid = profile.uid,
                settings = mapOf(
                    "storeOpen" to isStoreOpen,
                    "acceptingOrders" to isAcceptingOrders,
                    "lowStockAlerts" to wantsLowStockAlerts
                )
            )
        }.map {
            storeOpen = isStoreOpen
            acceptingOrders = isAcceptingOrders
            lowStockAlerts = wantsLowStockAlerts
            profileMessage = "Merchant settings saved."
            "Merchant settings saved."
        }
    }

    suspend fun changeMerchantPassword(
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
            MerchantTopHeader(
                storeName = storeName(profile, merchantProfile),
                selectedTab = selectedTab
            )
        },
        bottomBar = {
            MerchantBottomBar(
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
                    MerchantTab.Dashboard -> MerchantDashboardContent(
                        storeName = storeName(profile, merchantProfile),
                        products = products,
                        orders = orders
                    )
                    MerchantTab.Products -> MerchantProductsContent(
                        products = products,
                        onAddProduct = { isAddingProduct = true },
                        onEditProduct = { editingProduct = it },
                        onDeleteProduct = ::deleteProduct
                    )
                    MerchantTab.Orders -> MerchantOrdersContent(
                        orders = orders,
                        onUpdateStatus = ::updateOrderStatus
                    )
                MerchantTab.Profile -> MerchantProfileContent(
                    profile = profile,
                    merchantProfile = merchantProfile,
                    message = profileMessage,
                    maskedAccountNumber = maskAccountNumber(bankAccountNumber),
                    storeOpen = storeOpen,
                    acceptingOrders = acceptingOrders,
                    onEditProfile = { activeProfileDialog = MerchantProfileDialog.StoreInformation },
                    onOpenDialog = {
                        profileMessage = null
                        activeProfileDialog = it
                    },
                    onLogout = onLogout
                )
            }
            }
        }
    }

    if (isAddingProduct || editingProduct != null) {
        ProductEditorDialog(
            product = editingProduct,
            onDismiss = {
                isAddingProduct = false
                editingProduct = null
            },
            onSave = ::saveProduct
        )
    }

    when (activeProfileDialog) {
        MerchantProfileDialog.StoreInformation -> MerchantProfileEditDialog(
            profile = profile,
            merchantProfile = merchantProfile,
            onDismiss = { activeProfileDialog = null },
            onSave = ::saveMerchantProfile,
            onSaved = { activeProfileDialog = null }
        )
        MerchantProfileDialog.BankInformation -> MerchantBankInformationDialog(
            accountName = bankAccountName,
            bankName = bankName,
            accountNumber = bankAccountNumber,
            gcashNumber = gcashNumber,
            onDismiss = { activeProfileDialog = null },
            onSave = ::saveMerchantBankInfo,
            onSaved = { activeProfileDialog = null }
        )
        MerchantProfileDialog.ChangePassword -> ChangePasswordDialog(
            onDismiss = { activeProfileDialog = null },
            onSave = ::changeMerchantPassword,
            onSaved = { activeProfileDialog = null }
        )
        MerchantProfileDialog.Settings -> MerchantSettingsDialog(
            storeOpen = storeOpen,
            acceptingOrders = acceptingOrders,
            lowStockAlerts = lowStockAlerts,
            onDismiss = { activeProfileDialog = null },
            onSave = ::saveMerchantSettings,
            onSaved = { activeProfileDialog = null }
        )
        null -> Unit
    }
}

@Composable
private fun MerchantTopHeader(
    storeName: String,
    selectedTab: MerchantTab
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
                    text = if (selectedTab == MerchantTab.Dashboard) {
                        "Good day, $storeName"
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
                    text = "Manage Android-only products and orders",
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
private fun MerchantBottomBar(
    selectedTab: MerchantTab,
    onTabSelected: (MerchantTab) -> Unit
) {
    NavigationBar(
        modifier = Modifier.navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        MerchantTab.entries.forEach { tab ->
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
private fun MerchantDashboardContent(
    storeName: String,
    products: List<AndroidProduct>,
    orders: List<AndroidOrder>
) {
    val activeOrders = orders.count {
        it.status == FirebaseConstants.STATUS_TO_PREPARE ||
            it.status == FirebaseConstants.STATUS_TO_SHIP ||
            it.status == FirebaseConstants.STATUS_OUT_FOR_DELIVERY
    }
    val sales = orders
        .filter { it.status != FirebaseConstants.STATUS_CANCELLED }
        .sumOf { it.totalAmount }
    val rating = AndroidSampleData.stores.firstOrNull { it.storeName == storeName }?.rating
        ?: AndroidSampleData.stores.first().rating

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionHeader(title = "Today's Overview")
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OverviewCard("Orders", activeOrders.toString(), "Needs action")
            OverviewCard("Products", products.size.toString(), "In catalog")
            OverviewCard("Sales", formatSamplePeso(sales), "Sample total", wide = true)
            OverviewCard("Rating", String.format("%.1f", rating), "Store score")
        }

        SectionHeader(title = "Recent Orders")
        orders.take(5).forEach { order ->
            MerchantOrderCard(
                order = order,
                onUpdateStatus = {}
            )
        }
    }
}

@Composable
private fun MerchantProductsContent(
    products: List<AndroidProduct>,
    onAddProduct: () -> Unit,
    onEditProduct: (AndroidProduct) -> Unit,
    onDeleteProduct: (AndroidProduct) -> Unit
) {
    var search by rememberSaveable { mutableStateOf("") }
    val filteredProducts = products.filter { product ->
        search.isBlank() ||
            product.productName.contains(search, ignoreCase = true) ||
            product.category.contains(search, ignoreCase = true)
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ScreenTitle("Products")
        HonestbeeTextField(
            value = search,
            onValueChange = { search = it },
            placeholder = "Search products",
            leadingIcon = Icons.Outlined.Search
        )
        HonestbeeButton(
            text = "Add Product",
            onClick = onAddProduct
        )

        filteredProducts.forEach { product ->
            MerchantProductCard(
                product = product,
                onEdit = { onEditProduct(product) },
                onDelete = { onDeleteProduct(product) }
            )
        }
    }
}

@Composable
private fun MerchantOrdersContent(
    orders: List<AndroidOrder>,
    onUpdateStatus: (AndroidOrder, String) -> Unit
) {
    var selectedStatus by rememberSaveable { mutableStateOf("All") }
    val filters = listOf("All", "To Prepare", "To Ship", "Completed")
    val filteredOrders = orders.filter { order ->
        when (selectedStatus) {
            "To Prepare" -> order.status == FirebaseConstants.STATUS_TO_PREPARE
            "To Ship" -> order.status == FirebaseConstants.STATUS_TO_SHIP ||
                order.status == FirebaseConstants.STATUS_OUT_FOR_DELIVERY
            "Completed" -> order.status == FirebaseConstants.STATUS_COMPLETED
            else -> true
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ScreenTitle("Orders")
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { filter ->
                PillChip(
                    text = filter,
                    selected = selectedStatus == filter,
                    onClick = { selectedStatus = filter }
                )
            }
        }

        filteredOrders.forEach { order ->
            MerchantOrderCard(
                order = order,
                onUpdateStatus = { newStatus -> onUpdateStatus(order, newStatus) }
            )
        }
    }
}

@Composable
private fun MerchantProfileContent(
    profile: SessionProfile,
    merchantProfile: MerchantProfile?,
    message: String?,
    maskedAccountNumber: String,
    storeOpen: Boolean,
    acceptingOrders: Boolean,
    onEditProfile: () -> Unit,
    onOpenDialog: (MerchantProfileDialog) -> Unit,
    onLogout: () -> Unit
) {
    val name = storeName(profile, merchantProfile)

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
                    text = "Role: Merchant",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BeeMuted
                )
                merchantProfile?.ownerName?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = BeeDarkText,
                        textAlign = TextAlign.Center
                    )
                }
                Text(
                    text = profile.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = BeeMuted,
                    textAlign = TextAlign.Center
                )
                StatusChip(status = profile.status)
                HonestbeeOutlinedButton(
                    text = "Edit Store Profile",
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
                    icon = Icons.Outlined.Storefront,
                    title = "Store Information",
                    subtitle = merchantProfile?.address?.takeIf { it.isNotBlank() } ?: "Store name, owner, phone, and address",
                    onClick = { onOpenDialog(MerchantProfileDialog.StoreInformation) }
                )
                ProfileMenuRow(
                    icon = Icons.Outlined.AccountBalance,
                    title = "Bank Information",
                    subtitle = if (maskedAccountNumber.isBlank()) "Add payout details" else "Saved account $maskedAccountNumber",
                    onClick = { onOpenDialog(MerchantProfileDialog.BankInformation) }
                )
                ProfileMenuRow(
                    icon = Icons.Outlined.Lock,
                    title = "Change Password",
                    subtitle = "Update your sign-in password",
                    onClick = { onOpenDialog(MerchantProfileDialog.ChangePassword) }
                )
                ProfileMenuRow(
                    icon = Icons.Outlined.Settings,
                    title = "Settings",
                    subtitle = "${if (storeOpen) "Open" else "Closed"} - ${if (acceptingOrders) "accepting orders" else "not accepting orders"}",
                    onClick = { onOpenDialog(MerchantProfileDialog.Settings) }
                )
                ProfileMenuRow(
                    icon = Icons.Outlined.Logout,
                    title = "Logout",
                    subtitle = "Sign out of this account",
                    onClick = onLogout
                )
            }
        }
    }
}

@Composable
private fun MerchantProfileEditDialog(
    profile: SessionProfile,
    merchantProfile: MerchantProfile?,
    onDismiss: () -> Unit,
    onSave: suspend (String, String, String, String) -> Result<String>,
    onSaved: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var storeName by rememberSaveable(merchantProfile?.uid) {
        mutableStateOf(merchantProfile?.storeName?.ifBlank { storeName(profile, merchantProfile) } ?: storeName(profile, merchantProfile))
    }
    var ownerName by rememberSaveable(merchantProfile?.uid) {
        mutableStateOf(merchantProfile?.ownerName.orEmpty())
    }
    var phone by rememberSaveable(merchantProfile?.uid) {
        mutableStateOf(merchantProfile?.phone.orEmpty())
    }
    var address by rememberSaveable(merchantProfile?.uid) {
        mutableStateOf(merchantProfile?.address.orEmpty())
    }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isSaving by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = {
            Text(
                text = "Store Information",
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
                HonestbeeTextField(storeName, { storeName = it }, placeholder = "Store name")
                HonestbeeTextField(ownerName, { ownerName = it }, placeholder = "Owner name")
                HonestbeeTextField(phone, { phone = it }, placeholder = "Phone")
                HonestbeeTextField(address, { address = it }, placeholder = "Address", singleLine = false)
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
                        val result = onSave(storeName, ownerName, phone, address)
                        isSaving = false
                        result.onSuccess { onSaved() }
                            .onFailure { errorMessage = it.message ?: "Could not update store information." }
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
private fun MerchantBankInformationDialog(
    accountName: String,
    bankName: String,
    accountNumber: String,
    gcashNumber: String,
    onDismiss: () -> Unit,
    onSave: suspend (String, String, String, String) -> Result<String>,
    onSaved: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var accountNameInput by rememberSaveable(accountName) { mutableStateOf(accountName) }
    var bankNameInput by rememberSaveable(bankName) { mutableStateOf(bankName) }
    var accountNumberInput by rememberSaveable(accountNumber) { mutableStateOf(accountNumber) }
    var gcashNumberInput by rememberSaveable(gcashNumber) { mutableStateOf(gcashNumber) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isSaving by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = {
            Text(
                text = "Bank Information",
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
                if (accountNumberInput.isNotBlank()) {
                    Text(
                        text = "Current account: ${maskAccountNumber(accountNumberInput)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = BeeMuted
                    )
                }
                HonestbeeTextField(accountNameInput, { accountNameInput = it }, placeholder = "Account name")
                HonestbeeTextField(bankNameInput, { bankNameInput = it }, placeholder = "Bank name")
                HonestbeeTextField(
                    value = accountNumberInput,
                    onValueChange = { accountNumberInput = it },
                    placeholder = "Account number",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                HonestbeeTextField(
                    value = gcashNumberInput,
                    onValueChange = { gcashNumberInput = it },
                    placeholder = "GCash number",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
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
                        val result = onSave(
                            accountNameInput,
                            bankNameInput,
                            accountNumberInput,
                            gcashNumberInput
                        )
                        isSaving = false
                        result.onSuccess { onSaved() }
                            .onFailure { errorMessage = it.message ?: "Could not save bank information." }
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
private fun MerchantSettingsDialog(
    storeOpen: Boolean,
    acceptingOrders: Boolean,
    lowStockAlerts: Boolean,
    onDismiss: () -> Unit,
    onSave: suspend (Boolean, Boolean, Boolean) -> Result<String>,
    onSaved: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var storeOpenChecked by rememberSaveable(storeOpen) { mutableStateOf(storeOpen) }
    var acceptingOrdersChecked by rememberSaveable(acceptingOrders) { mutableStateOf(acceptingOrders) }
    var lowStockAlertsChecked by rememberSaveable(lowStockAlerts) { mutableStateOf(lowStockAlerts) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isSaving by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = {
            Text(
                text = "Merchant Settings",
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
                SettingsSwitchRow(
                    title = "Store Open/Closed",
                    subtitle = if (storeOpenChecked) "Store is visible as open" else "Store is marked closed",
                    checked = storeOpenChecked,
                    onCheckedChange = { storeOpenChecked = it }
                )
                SettingsSwitchRow(
                    title = "Accepting Orders",
                    subtitle = "Android-only order intake preference",
                    checked = acceptingOrdersChecked,
                    onCheckedChange = { acceptingOrdersChecked = it }
                )
                SettingsSwitchRow(
                    title = "Low Stock Alerts",
                    subtitle = "Get reminders when sample stock is low",
                    checked = lowStockAlertsChecked,
                    onCheckedChange = { lowStockAlertsChecked = it }
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
                        val result = onSave(
                            storeOpenChecked,
                            acceptingOrdersChecked,
                            lowStockAlertsChecked
                        )
                        isSaving = false
                        result.onSuccess { onSaved() }
                            .onFailure { errorMessage = it.message ?: "Could not save merchant settings." }
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
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
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
private fun OverviewCard(
    title: String,
    value: String,
    note: String,
    wide: Boolean = false
) {
    HonestbeeCard(
        modifier = Modifier.width(if (wide) 164.dp else 132.dp)
    ) {
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
                style = MaterialTheme.typography.titleLarge,
                color = BeeDarkText,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
private fun MerchantProductCard(
    product: AndroidProduct,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    HonestbeeCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(BeeCream),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Inventory2,
                    contentDescription = null,
                    tint = BeeHoneyYellow,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = product.productName,
                    style = MaterialTheme.typography.titleMedium,
                    color = BeeDarkText,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${product.category} - ${product.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = BeeMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatSamplePeso(product.price),
                        style = MaterialTheme.typography.bodyMedium,
                        color = BeeDarkText,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Stock ${product.stock}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BeeMuted
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SmallIconAction(
                    icon = Icons.Outlined.Edit,
                    contentDescription = "Edit product",
                    onClick = onEdit
                )
                SmallIconAction(
                    icon = Icons.Outlined.Delete,
                    contentDescription = "Delete product",
                    tint = BeeError,
                    onClick = onDelete
                )
            }
        }
    }
}

@Composable
private fun MerchantOrderCard(
    order: AndroidOrder,
    onUpdateStatus: (String) -> Unit
) {
    HonestbeeCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.orderId,
                        style = MaterialTheme.typography.titleMedium,
                        color = BeeDarkText,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatOrderTime(order.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = BeeMuted
                    )
                }
                StatusChip(status = order.status)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.customerName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = BeeDarkText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${order.items.size} items",
                        style = MaterialTheme.typography.bodySmall,
                        color = BeeMuted
                    )
                }
                Text(
                    text = formatSamplePeso(order.totalAmount),
                    style = MaterialTheme.typography.titleMedium,
                    color = BeeDarkText,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusActionButton(
                    text = "To Prepare",
                    enabled = order.status != FirebaseConstants.STATUS_TO_PREPARE,
                    onClick = { onUpdateStatus(FirebaseConstants.STATUS_TO_PREPARE) }
                )
                StatusActionButton(
                    text = "To Ship",
                    enabled = order.status != FirebaseConstants.STATUS_TO_SHIP,
                    onClick = { onUpdateStatus(FirebaseConstants.STATUS_TO_SHIP) }
                )
                StatusActionButton(
                    text = "Delivered",
                    enabled = order.status != FirebaseConstants.STATUS_COMPLETED,
                    onClick = { onUpdateStatus(FirebaseConstants.STATUS_COMPLETED) }
                )
            }
        }
    }
}

@Composable
private fun ProductEditorDialog(
    product: AndroidProduct?,
    onDismiss: () -> Unit,
    onSave: (AndroidProduct) -> Unit
) {
    var name by rememberSaveable(product?.productId) {
        mutableStateOf(product?.productName.orEmpty())
    }
    var category by rememberSaveable(product?.productId) {
        mutableStateOf(product?.category ?: "Groceries")
    }
    var price by rememberSaveable(product?.productId) {
        mutableStateOf(product?.price?.toString().orEmpty())
    }
    var stock by rememberSaveable(product?.productId) {
        mutableStateOf(product?.stock?.toString().orEmpty())
    }
    var unit by rememberSaveable(product?.productId) {
        mutableStateOf(product?.unit ?: "item")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (product == null) "Add Product" else "Edit Product",
                color = BeeDarkText,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HonestbeeTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Product name"
                )
                HonestbeeTextField(
                    value = category,
                    onValueChange = { category = it },
                    placeholder = "Category"
                )
                HonestbeeTextField(
                    value = price,
                    onValueChange = { price = it },
                    placeholder = "Price",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                HonestbeeTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    placeholder = "Stock",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                HonestbeeTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    placeholder = "Unit"
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val cleanName = name.trim()
                    if (cleanName.isBlank()) return@TextButton

                    val id = product?.productId ?: "android_product_${System.currentTimeMillis()}"
                    onSave(
                        AndroidProduct(
                            productId = id,
                            storeId = product?.storeId ?: "android_sample_store",
                            productName = cleanName,
                            category = category.trim().ifBlank { "Groceries" },
                            price = price.toDoubleOrNull() ?: 0.0,
                            stock = stock.toIntOrNull() ?: 0,
                            unit = unit.trim().ifBlank { "item" },
                            description = product?.description.orEmpty(),
                            imageUrl = product?.imageUrl.orEmpty(),
                            isAvailable = true
                        )
                    )
                }
            ) {
                Text(
                    text = "Save",
                    color = BeeHoneyYellow,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = BeeMuted)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp)
    )
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
private fun ScreenTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = BeeDarkText
    )
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
private fun StatusActionButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        color = if (enabled) BeeNavigationSelected else BeeCream,
        border = BorderStroke(1.dp, BeePrimaryYellow.copy(alpha = 0.45f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) BeeDarkText else BeeMuted,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SmallIconAction(
    icon: ImageVector,
    contentDescription: String,
    tint: androidx.compose.ui.graphics.Color = BeeDarkText,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(BeeNavigationSelected)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
    }
}

private fun storeName(
    profile: SessionProfile,
    merchantProfile: MerchantProfile?
): String {
    return merchantProfile?.storeName?.takeIf { it.isNotBlank() }
        ?: profile.displayName.takeIf { it.isNotBlank() }
        ?: "Fresh Mart"
}

private fun initials(name: String): String {
    val parts = name.split(" ").filter { it.isNotBlank() }
    return parts.take(2).joinToString("") { it.first().uppercaseChar().toString() }
        .ifBlank { "HB" }
}

private fun formatOrderTime(timestamp: Timestamp?): String {
    if (timestamp == null) return "Today"

    return SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        .format(Date(timestamp.seconds * 1000))
}

private fun maskAccountNumber(accountNumber: String): String {
    val clean = accountNumber.trim()
    if (clean.isBlank()) return ""
    val visible = clean.takeLast(4)
    return "****$visible"
}

private enum class MerchantTab(
    val title: String,
    val icon: ImageVector
) {
    Dashboard("Dashboard", Icons.Outlined.Home),
    Products("Products", Icons.Outlined.Inventory2),
    Orders("Orders", Icons.Outlined.ReceiptLong),
    Profile("Profile", Icons.Outlined.Person)
}

private enum class MerchantProfileDialog {
    StoreInformation,
    BankInformation,
    ChangePassword,
    Settings
}
