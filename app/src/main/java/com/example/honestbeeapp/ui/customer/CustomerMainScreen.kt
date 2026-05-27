package com.example.honestbeeapp.ui.customer

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.honestbeeapp.data.model.AndroidCartItem
import com.example.honestbeeapp.data.model.AndroidOrder
import com.example.honestbeeapp.data.model.AndroidStore
import com.example.honestbeeapp.data.model.CustomerProfile
import com.example.honestbeeapp.data.model.SessionProfile
import com.example.honestbeeapp.data.sample.AndroidSampleData
import com.example.honestbeeapp.ui.components.HonestbeeButton
import com.example.honestbeeapp.ui.components.HonestbeeCard
import com.example.honestbeeapp.ui.components.HonestbeeOutlinedButton
import com.example.honestbeeapp.ui.components.HonestbeeTextField
import com.example.honestbeeapp.ui.components.ProductCard
import com.example.honestbeeapp.ui.components.SectionHeader
import com.example.honestbeeapp.ui.components.StatusChip
import com.example.honestbeeapp.ui.components.StoreCard
import com.example.honestbeeapp.ui.components.formatSamplePeso
import com.example.honestbeeapp.ui.theme.BeeCream
import com.example.honestbeeapp.ui.theme.BeeDarkText
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
fun CustomerMainScreen(
    profile: SessionProfile,
    onLogout: () -> Unit,
    firestore: FirebaseFirestore = remember { FirebaseFirestore.getInstance() }
) {
    var selectedTab by rememberSaveable { mutableStateOf(CustomerTab.Home) }
    var customerProfile by remember { mutableStateOf<CustomerProfile?>(null) }
    var isEditingProfile by rememberSaveable { mutableStateOf(false) }
    val cartItems = remember {
        mutableStateListOf<AndroidCartItem>().apply {
            addAll(AndroidSampleData.cartItems)
        }
    }
    val orders = remember {
        mutableStateListOf<AndroidOrder>().apply {
            addAll(AndroidSampleData.orders)
        }
    }
    var checkoutMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(profile.uid, firestore) {
        customerProfile = runCatching {
            firestore.collection(FirebaseConstants.CUSTOMERS)
                .document(profile.uid)
                .get()
                .await()
                .toObject(CustomerProfile::class.java)
        }.getOrNull()
    }

    fun saveCustomerProfile(
        firstName: String,
        lastName: String,
        phone: String,
        address: String
    ) {
        scope.launch {
            val cleanFirstName = firstName.trim()
            val cleanLastName = lastName.trim()
            val cleanPhone = phone.trim()
            val cleanAddress = address.trim()
            val displayName = "$cleanFirstName $cleanLastName".trim()

            val userUpdates = mapOf(
                "firstName" to cleanFirstName,
                "lastName" to cleanLastName,
                "username" to displayName,
                "phone" to cleanPhone,
                "address" to cleanAddress,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val profileUpdates = mapOf(
                "uid" to profile.uid,
                "email" to profile.email,
                "firstName" to cleanFirstName,
                "lastName" to cleanLastName,
                "phone" to cleanPhone,
                "address" to cleanAddress,
                "role" to FirebaseConstants.ROLE_CUSTOMER,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            runCatching {
                firestore.collection(FirebaseConstants.USERS)
                    .document(profile.uid)
                    .set(userUpdates, SetOptions.merge())
                    .await()
                firestore.collection(FirebaseConstants.CUSTOMERS)
                    .document(profile.uid)
                    .set(profileUpdates, SetOptions.merge())
                    .await()
            }.onSuccess {
                customerProfile = (customerProfile ?: CustomerProfile(
                    uid = profile.uid,
                    email = profile.email,
                    role = FirebaseConstants.ROLE_CUSTOMER,
                    status = profile.status
                )).copy(
                    firstName = cleanFirstName,
                    lastName = cleanLastName,
                    phone = cleanPhone,
                    address = cleanAddress
                )
                isEditingProfile = false
            }
        }
    }

    fun updateQuantity(item: AndroidCartItem, newQuantity: Int) {
        val index = cartItems.indexOfFirst { it.cartItemId == item.cartItemId }
        if (index == -1) return

        if (newQuantity <= 0) {
            cartItems.removeAt(index)
        } else {
            cartItems[index] = item.copy(
                quantity = newQuantity,
                subtotal = item.price * newQuantity
            )
        }
    }

    fun checkout() {
        if (cartItems.isEmpty()) {
            checkoutMessage = "Your cart is empty."
            return
        }

        val deliveryFee = 49.0
        val subtotal = cartItems.sumOf { it.subtotal }
        val orderNumber = "HB-ANDROID-${orders.size + 1007}"

        orders.add(
            0,
            AndroidOrder(
                orderId = orderNumber,
                customerId = profile.uid,
                merchantId = "android_sample_merchant",
                riderId = "",
                storeName = "Android Cart",
                customerName = fullName(profile),
                totalAmount = subtotal + deliveryFee,
                deliveryFee = deliveryFee,
                status = FirebaseConstants.STATUS_TO_PAY,
                paymentMethod = "Cash on Delivery",
                deliveryAddress = "Saved customer address",
                createdAt = Timestamp.now(),
                items = cartItems.toList()
            )
        )
        cartItems.clear()
        checkoutMessage = "$orderNumber created in Android-only orders."
        selectedTab = CustomerTab.Orders
    }

    Scaffold(
        topBar = {
            CustomerTopHeader(
                profile = profile,
                selectedTab = selectedTab
            )
        },
        bottomBar = {
            CustomerBottomBar(
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
                    CustomerTab.Home -> CustomerHomeContent()
                    CustomerTab.Stores -> CustomerStoresContent()
                    CustomerTab.Cart -> CustomerCartContent(
                        cartItems = cartItems,
                        checkoutMessage = checkoutMessage,
                        onQuantityChange = ::updateQuantity,
                        onCheckout = ::checkout
                    )
                    CustomerTab.Orders -> CustomerOrdersContent(orders = orders)
                CustomerTab.Profile -> CustomerProfileContent(
                    profile = profile,
                    customerProfile = customerProfile,
                    onEditProfile = { isEditingProfile = true },
                    onLogout = onLogout
                )
            }
        }
    }

    if (isEditingProfile) {
        CustomerProfileEditDialog(
            profile = profile,
            customerProfile = customerProfile,
            onDismiss = { isEditingProfile = false },
            onSave = ::saveCustomerProfile
        )
    }
}
}

@Composable
private fun CustomerTopHeader(
    profile: SessionProfile,
    selectedTab: CustomerTab
) {
    Surface(
        color = BeePrimaryYellow,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (selectedTab == CustomerTab.Home) {
                            "Hello, ${firstName(profile)}!"
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
                        text = if (selectedTab == CustomerTab.Home) {
                            "What would you like today?"
                        } else {
                            "Android-only grocery delivery"
                        },
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

            if (selectedTab == CustomerTab.Home) {
                SearchPill(placeholder = "Search groceries, stores, and more")
            }
        }
    }
}

@Composable
private fun CustomerBottomBar(
    selectedTab: CustomerTab,
    onTabSelected: (CustomerTab) -> Unit
) {
    NavigationBar(
        modifier = Modifier.navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        CustomerTab.entries.forEach { tab ->
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
private fun CustomerHomeContent() {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        PromoCard()

        SectionHeader(title = "Featured stores", actionText = "View all", onActionClick = {})
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AndroidSampleData.stores.take(4).forEach { store ->
                StoreCard(
                    storeName = store.storeName,
                    rating = store.rating,
                    deliveryTime = store.deliveryTime,
                    minimumOrder = store.minimumOrder,
                    modifier = Modifier.width(245.dp)
                )
            }
        }

        SectionHeader(title = "Popular categories")
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AndroidSampleData.categories.take(8).forEach { category ->
                PillChip(
                    text = category,
                    selected = false,
                    onClick = {},
                )
            }
        }

        SectionHeader(title = "Popular products")
        AndroidSampleData.products.take(8).forEach { product ->
            ProductCard(
                productName = product.productName,
                price = product.price,
                isAvailable = product.isAvailable,
                onAddClick = {}
            )
        }

        Spacer(Modifier.height(6.dp))
    }
}

@Composable
private fun CustomerStoresContent() {
    var search by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("All") }
    val filters = listOf("All", "Groceries", "Fruits", "Snacks", "Drinks")
    val stores = AndroidSampleData.stores
        .filter { store -> matchesStoreFilter(store, selectedCategory) }
        .filter { store ->
            search.isBlank() ||
                store.storeName.contains(search, ignoreCase = true) ||
                store.category.contains(search, ignoreCase = true)
        }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ScreenTitle("Stores")

        HonestbeeTextField(
            value = search,
            onValueChange = { search = it },
            placeholder = "Search stores",
            leadingIcon = Icons.Outlined.Search
        )

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { filter ->
                PillChip(
                    text = filter,
                    selected = selectedCategory == filter,
                    onClick = { selectedCategory = filter }
                )
            }
        }

        stores.forEach { store ->
            StoreCard(
                storeName = store.storeName,
                rating = store.rating,
                deliveryTime = store.deliveryTime,
                minimumOrder = store.minimumOrder
            )
        }
    }
}

@Composable
private fun CustomerCartContent(
    cartItems: List<AndroidCartItem>,
    checkoutMessage: String?,
    onQuantityChange: (AndroidCartItem, Int) -> Unit,
    onCheckout: () -> Unit
) {
    var promoCode by rememberSaveable { mutableStateOf("") }
    val subtotal = cartItems.sumOf { it.subtotal }
    val deliveryFee = if (cartItems.isEmpty()) 0.0 else 49.0
    val total = subtotal + deliveryFee

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ScreenTitle("My Cart")

        if (cartItems.isEmpty()) {
            HonestbeeCard {
                Text(
                    text = "Your cart is empty.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = BeeMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            cartItems.forEach { item ->
                CustomerCartItemCard(
                    item = item,
                    onMinus = { onQuantityChange(item, item.quantity - 1) },
                    onPlus = { onQuantityChange(item, item.quantity + 1) }
                )
            }
        }

        HonestbeeCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Promo code",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = BeeDarkText
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HonestbeeTextField(
                        value = promoCode,
                        onValueChange = { promoCode = it },
                        placeholder = "Enter code",
                        modifier = Modifier.weight(1f)
                    )
                    HonestbeeOutlinedButton(
                        text = "Apply",
                        onClick = {},
                        fullWidth = false
                    )
                }
            }
        }

        HonestbeeCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PriceRow("Subtotal", subtotal)
                PriceRow("Delivery fee", deliveryFee)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = BeeNavigationSelected,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BeeDarkText
                        )
                        Text(
                            text = formatSamplePeso(total),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BeeDarkText
                        )
                    }
                }

                checkoutMessage?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = BeeHoneyYellow,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                HonestbeeButton(
                    text = "Proceed to Checkout",
                    enabled = cartItems.isNotEmpty(),
                    onClick = onCheckout
                )
            }
        }
    }
}

@Composable
private fun CustomerOrdersContent(orders: List<AndroidOrder>) {
    var selectedStatus by rememberSaveable { mutableStateOf("All") }
    val filters = listOf("All", "To Pay", "To Receive", "Completed")
    val filteredOrders = orders.filter { order ->
        when (selectedStatus) {
            "To Pay" -> order.status == FirebaseConstants.STATUS_TO_PAY
            "To Receive" -> order.status == FirebaseConstants.STATUS_TO_SHIP ||
                order.status == FirebaseConstants.STATUS_OUT_FOR_DELIVERY
            "Completed" -> order.status == FirebaseConstants.STATUS_COMPLETED
            else -> true
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ScreenTitle("My Orders")

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
            CustomerOrderCard(order = order)
        }
    }
}

@Composable
private fun CustomerProfileContent(
    profile: SessionProfile,
    customerProfile: CustomerProfile?,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val displayName = customerFullName(profile, customerProfile)

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
                        text = initials(profile),
                        style = MaterialTheme.typography.headlineSmall,
                        color = BeeDarkText,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = displayName,
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
                StatusChip(status = profile.status)
                HonestbeeOutlinedButton(
                    text = "Edit Profile",
                    onClick = onEditProfile,
                    fullWidth = false
                )
            }
        }

        HonestbeeCard {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                ProfileMenuRow(Icons.Outlined.AccountCircle, "Personal Information")
                ProfileMenuRow(Icons.Outlined.LocationOn, "Address Book")
                ProfileMenuRow(Icons.Outlined.CreditCard, "Payment Methods")
                ProfileMenuRow(Icons.Outlined.Notifications, "Notifications")
                ProfileMenuRow(Icons.Outlined.HelpOutline, "Help Center")
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
private fun CustomerProfileEditDialog(
    profile: SessionProfile,
    customerProfile: CustomerProfile?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    val fallbackName = fullName(profile)
    var firstName by rememberSaveable(customerProfile?.uid) {
        mutableStateOf(customerProfile?.firstName?.ifBlank { fallbackName.substringBefore(" ") } ?: fallbackName.substringBefore(" "))
    }
    var lastName by rememberSaveable(customerProfile?.uid) {
        mutableStateOf(customerProfile?.lastName?.ifBlank { fallbackName.substringAfter(" ", "") } ?: fallbackName.substringAfter(" ", ""))
    }
    var phone by rememberSaveable(customerProfile?.uid) {
        mutableStateOf(customerProfile?.phone.orEmpty())
    }
    var address by rememberSaveable(customerProfile?.uid) {
        mutableStateOf(customerProfile?.address.orEmpty())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Profile",
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
                HonestbeeTextField(address, { address = it }, placeholder = "Address", singleLine = false)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(firstName, lastName, phone, address) }
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
private fun PromoCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = BeePrimaryYellow,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BeeHoneyYellow),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Fresh groceries delivered fast",
                    style = MaterialTheme.typography.titleLarge,
                    color = BeeDarkText,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Shop Android-only stores and get daily essentials in minutes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BeeDarkText.copy(alpha = 0.76f)
                )
            }
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ShoppingCart,
                    contentDescription = null,
                    tint = BeeHoneyYellow,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
private fun CustomerCartItemCard(
    item: AndroidCartItem,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    HonestbeeCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(BeeCream),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ShoppingCart,
                    contentDescription = null,
                    tint = BeeHoneyYellow
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.productName,
                    style = MaterialTheme.typography.titleMedium,
                    color = BeeDarkText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatSamplePeso(item.price),
                    style = MaterialTheme.typography.bodyMedium,
                    color = BeeMuted
                )
                Text(
                    text = formatSamplePeso(item.subtotal),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = BeeDarkText
                )
            }

            QuantityStepper(
                quantity = item.quantity,
                onMinus = onMinus,
                onPlus = onPlus
            )
        }
    }
}

@Composable
private fun QuantityStepper(
    quantity: Int,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        QuantityButton(
            icon = Icons.Outlined.Remove,
            contentDescription = "Decrease quantity",
            onClick = onMinus
        )
        Text(
            text = quantity.toString(),
            modifier = Modifier.widthIn(min = 22.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = BeeDarkText,
            fontWeight = FontWeight.Bold
        )
        QuantityButton(
            icon = Icons.Outlined.Add,
            contentDescription = "Increase quantity",
            onClick = onPlus
        )
    }
}

@Composable
private fun QuantityButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(BeeNavigationSelected)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = BeeDarkText,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun CustomerOrderCard(order: AndroidOrder) {
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
                        text = formatOrderDate(order.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = BeeMuted
                    )
                }
                StatusChip(status = order.status)
            }

            Text(
                text = order.storeName,
                style = MaterialTheme.typography.bodyLarge,
                color = BeeDarkText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatSamplePeso(order.totalAmount),
                    style = MaterialTheme.typography.titleMedium,
                    color = BeeDarkText,
                    fontWeight = FontWeight.Bold
                )
                HonestbeeOutlinedButton(
                    text = "View details",
                    onClick = {},
                    fullWidth = false
                )
            }
        }
    }
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
private fun SearchPill(placeholder: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, BeeHoneyYellow.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = BeeMuted,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = BeeMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
private fun PriceRow(
    label: String,
    amount: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = BeeMuted
        )
        Text(
            text = formatSamplePeso(amount),
            style = MaterialTheme.typography.bodyMedium,
            color = BeeDarkText,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun firstName(profile: SessionProfile): String {
    return fullName(profile).substringBefore(" ").ifBlank { "there" }
}

private fun fullName(profile: SessionProfile): String {
    return profile.displayName.ifBlank {
        profile.email.substringBefore("@").ifBlank { "Customer" }
    }
}

private fun customerFullName(
    profile: SessionProfile,
    customerProfile: CustomerProfile?
): String {
    val profileName = "${customerProfile?.firstName.orEmpty()} ${customerProfile?.lastName.orEmpty()}".trim()
    return profileName.ifBlank { fullName(profile) }
}

private fun initials(profile: SessionProfile): String {
    val parts = fullName(profile).split(" ").filter { it.isNotBlank() }
    return parts.take(2).joinToString("") { it.first().uppercaseChar().toString() }
        .ifBlank { "HB" }
}

private fun matchesStoreFilter(
    store: AndroidStore,
    filter: String
): Boolean {
    return when (filter) {
        "Groceries" -> store.category.contains("grocery", ignoreCase = true) ||
            store.category.contains("supermarket", ignoreCase = true) ||
            store.category.contains("convenience", ignoreCase = true)
        "Fruits" -> store.storeName == "Green Basket"
        "Snacks" -> store.storeName == "Daily Needs" || store.storeName == "Bee Grocery"
        "Drinks" -> store.storeName == "Daily Needs" || store.storeName == "Bee Grocery"
        else -> true
    }
}

private fun formatOrderDate(timestamp: Timestamp?): String {
    if (timestamp == null) return "Today"

    return SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        .format(Date(timestamp.seconds * 1000))
}

private enum class CustomerTab(
    val title: String,
    val icon: ImageVector
) {
    Home("Home", Icons.Outlined.Home),
    Stores("Stores", Icons.Outlined.Storefront),
    Cart("Cart", Icons.Outlined.ShoppingCart),
    Orders("Orders", Icons.Outlined.ReceiptLong),
    Profile("Profile", Icons.Outlined.Person)
}
