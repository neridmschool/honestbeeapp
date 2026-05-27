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
import com.example.honestbeeapp.data.sample.AndroidSampleData
import com.example.honestbeeapp.ui.components.HonestbeeButton
import com.example.honestbeeapp.ui.components.HonestbeeCard
import com.example.honestbeeapp.ui.components.HonestbeeOutlinedButton
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
import com.example.honestbeeapp.util.FirebaseConstants
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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
    var isEditingProfile by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(profile.uid, firestore) {
        merchantProfile = runCatching {
            firestore.collection(FirebaseConstants.MERCHANTS)
                .document(profile.uid)
                .get()
                .await()
                .toObject(MerchantProfile::class.java)
        }.getOrNull()
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

    fun saveMerchantProfile(
        storeName: String,
        ownerName: String,
        phone: String,
        address: String
    ) {
        scope.launch {
            val cleanStoreName = storeName.trim()
            val cleanOwnerName = ownerName.trim()
            val cleanPhone = phone.trim()
            val cleanAddress = address.trim()
            val userUpdates = mapOf(
                "username" to cleanOwnerName.ifBlank { cleanStoreName },
                "phone" to cleanPhone,
                "address" to cleanAddress,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val merchantUpdates = mapOf(
                "uid" to profile.uid,
                "email" to profile.email,
                "storeName" to cleanStoreName,
                "ownerName" to cleanOwnerName,
                "phone" to cleanPhone,
                "address" to cleanAddress,
                "role" to FirebaseConstants.ROLE_MERCHANT,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            runCatching {
                firestore.collection(FirebaseConstants.USERS)
                    .document(profile.uid)
                    .set(userUpdates, SetOptions.merge())
                    .await()
                firestore.collection(FirebaseConstants.MERCHANTS)
                    .document(profile.uid)
                    .set(merchantUpdates, SetOptions.merge())
                    .await()
            }.onSuccess {
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
                isEditingProfile = false
            }
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
                    onEditProfile = { isEditingProfile = true },
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

    if (isEditingProfile) {
        MerchantProfileEditDialog(
            profile = profile,
            merchantProfile = merchantProfile,
            onDismiss = { isEditingProfile = false },
            onSave = ::saveMerchantProfile
        )
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
    onEditProfile: () -> Unit,
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

        HonestbeeCard {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                ProfileMenuRow(Icons.Outlined.Storefront, "Store Information")
                ProfileMenuRow(Icons.Outlined.AccountBalance, "Bank Information")
                ProfileMenuRow(Icons.Outlined.Lock, "Change Password")
                ProfileMenuRow(Icons.Outlined.Settings, "Settings")
                ProfileMenuRow(
                    icon = Icons.Outlined.Logout,
                    title = "Logout",
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
    onSave: (String, String, String, String) -> Unit
) {
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Store Profile",
                color = BeeDarkText,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HonestbeeTextField(storeName, { storeName = it }, placeholder = "Store name")
                HonestbeeTextField(ownerName, { ownerName = it }, placeholder = "Owner name")
                HonestbeeTextField(phone, { phone = it }, placeholder = "Phone")
                HonestbeeTextField(address, { address = it }, placeholder = "Address", singleLine = false)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(storeName, ownerName, phone, address) }
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
                .heightIn(min = 48.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BeeHoneyYellow,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = BeeDarkText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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

private enum class MerchantTab(
    val title: String,
    val icon: ImageVector
) {
    Dashboard("Dashboard", Icons.Outlined.Home),
    Products("Products", Icons.Outlined.Inventory2),
    Orders("Orders", Icons.Outlined.ReceiptLong),
    Profile("Profile", Icons.Outlined.Person)
}
