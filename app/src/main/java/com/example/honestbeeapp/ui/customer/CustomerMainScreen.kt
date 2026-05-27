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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.example.honestbeeapp.data.repository.UserRepository
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
fun CustomerMainScreen(
    profile: SessionProfile,
    onLogout: () -> Unit,
    firestore: FirebaseFirestore = remember { FirebaseFirestore.getInstance() }
) {
    var selectedTab by rememberSaveable { mutableStateOf(CustomerTab.Home) }
    var customerProfile by remember { mutableStateOf<CustomerProfile?>(null) }
    var activeProfileDialog by rememberSaveable { mutableStateOf<CustomerProfileDialog?>(null) }
    var profileMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var defaultPaymentMethod by rememberSaveable(profile.uid) { mutableStateOf("Cash on Delivery") }
    var orderUpdatesEnabled by rememberSaveable(profile.uid) { mutableStateOf(true) }
    var promotionsEnabled by rememberSaveable(profile.uid) { mutableStateOf(false) }
    var deliveryAlertsEnabled by rememberSaveable(profile.uid) { mutableStateOf(true) }
    val userRepository = remember(firestore) { UserRepository(firestore) }
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

    LaunchedEffect(profile.uid, firestore) {
        customerProfile = runCatching {
            firestore.collection(FirebaseConstants.CUSTOMERS)
                .document(profile.uid)
                .get()
                .await()
                .toObject(CustomerProfile::class.java)
        }.getOrNull()

        runCatching {
            firestore.collection(FirebaseConstants.ANDROID_CUSTOMER_SETTINGS)
                .document(profile.uid)
                .get()
                .await()
        }.onSuccess { document ->
            defaultPaymentMethod = document.getString("defaultPaymentMethod") ?: defaultPaymentMethod
            orderUpdatesEnabled = document.getBoolean("orderUpdates") ?: orderUpdatesEnabled
            promotionsEnabled = document.getBoolean("promotions") ?: promotionsEnabled
            deliveryAlertsEnabled = document.getBoolean("deliveryAlerts") ?: deliveryAlertsEnabled
        }
    }

    suspend fun saveCustomerPersonalInfo(
        firstName: String,
        lastName: String,
        phone: String
    ): Result<String> {
        val cleanFirstName = firstName.trim()
        val cleanLastName = lastName.trim()
        val cleanPhone = phone.trim()
        val currentAddress = customerProfile?.address.orEmpty()

        if (cleanFirstName.isBlank() || cleanLastName.isBlank() || cleanPhone.isBlank()) {
            return Result.failure(IllegalArgumentException("First name, last name, and phone are required."))
        }

        return runCatching {
            userRepository.updateCustomerProfile(
                uid = profile.uid,
                email = profile.email,
                firstName = cleanFirstName,
                lastName = cleanLastName,
                phone = cleanPhone,
                address = currentAddress
            )
        }.map {
            customerProfile = (customerProfile ?: CustomerProfile(
                uid = profile.uid,
                email = profile.email,
                role = FirebaseConstants.ROLE_CUSTOMER,
                status = profile.status
            )).copy(
                firstName = cleanFirstName,
                lastName = cleanLastName,
                phone = cleanPhone,
                address = currentAddress
            )
            profileMessage = "Personal information updated."
            "Personal information updated."
        }
    }

    suspend fun saveCustomerAddress(address: String): Result<String> {
        val cleanAddress = address.trim()
        if (cleanAddress.isBlank()) {
            return Result.failure(IllegalArgumentException("Address is required."))
        }

        return runCatching {
            userRepository.updateCustomerAddress(
                uid = profile.uid,
                address = cleanAddress
            )
        }.map {
            customerProfile = (customerProfile ?: CustomerProfile(
                uid = profile.uid,
                email = profile.email,
                role = FirebaseConstants.ROLE_CUSTOMER,
                status = profile.status
            )).copy(address = cleanAddress)
            profileMessage = "Default address updated."
            "Default address updated."
        }
    }

    suspend fun saveCustomerPaymentMethod(paymentMethod: String): Result<String> {
        return runCatching {
            userRepository.updateCustomerSettings(
                uid = profile.uid,
                settings = mapOf("defaultPaymentMethod" to paymentMethod)
            )
        }.map {
            defaultPaymentMethod = paymentMethod
            profileMessage = "Default payment method updated."
            "Default payment method updated."
        }
    }

    suspend fun saveCustomerNotifications(
        orderUpdates: Boolean,
        promotions: Boolean,
        deliveryAlerts: Boolean
    ): Result<String> {
        return runCatching {
            userRepository.updateCustomerSettings(
                uid = profile.uid,
                settings = mapOf(
                    "orderUpdates" to orderUpdates,
                    "promotions" to promotions,
                    "deliveryAlerts" to deliveryAlerts
                )
            )
        }.map {
            orderUpdatesEnabled = orderUpdates
            promotionsEnabled = promotions
            deliveryAlertsEnabled = deliveryAlerts
            profileMessage = "Notification settings saved."
            "Notification settings saved."
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
                    message = profileMessage,
                    defaultPaymentMethod = defaultPaymentMethod,
                    onEditProfile = { activeProfileDialog = CustomerProfileDialog.PersonalInformation },
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
        CustomerProfileDialog.PersonalInformation -> CustomerProfileEditDialog(
            profile = profile,
            customerProfile = customerProfile,
            onDismiss = { activeProfileDialog = null },
            onSave = ::saveCustomerPersonalInfo,
            onSaved = { activeProfileDialog = null }
        )
        CustomerProfileDialog.AddressBook -> CustomerAddressDialog(
            customerProfile = customerProfile,
            onDismiss = { activeProfileDialog = null },
            onSave = ::saveCustomerAddress,
            onSaved = { activeProfileDialog = null }
        )
        CustomerProfileDialog.PaymentMethods -> CustomerPaymentMethodsDialog(
            selectedPaymentMethod = defaultPaymentMethod,
            onDismiss = { activeProfileDialog = null },
            onSave = ::saveCustomerPaymentMethod,
            onSaved = { activeProfileDialog = null }
        )
        CustomerProfileDialog.Notifications -> CustomerNotificationsDialog(
            orderUpdates = orderUpdatesEnabled,
            promotions = promotionsEnabled,
            deliveryAlerts = deliveryAlertsEnabled,
            onDismiss = { activeProfileDialog = null },
            onSave = ::saveCustomerNotifications,
            onSaved = { activeProfileDialog = null }
        )
        CustomerProfileDialog.HelpCenter -> CustomerHelpCenterDialog(
            onDismiss = { activeProfileDialog = null }
        )
        null -> Unit
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
    message: String?,
    defaultPaymentMethod: String,
    onEditProfile: () -> Unit,
    onOpenDialog: (CustomerProfileDialog) -> Unit,
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

        message?.let {
            ProfileMessageCard(message = it)
        }

        HonestbeeCard {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                ProfileMenuRow(
                    icon = Icons.Outlined.AccountCircle,
                    title = "Personal Information",
                    subtitle = "Name, phone, and email",
                    onClick = { onOpenDialog(CustomerProfileDialog.PersonalInformation) }
                )
                ProfileMenuRow(
                    icon = Icons.Outlined.LocationOn,
                    title = "Address Book",
                    subtitle = customerProfile?.address?.takeIf { it.isNotBlank() } ?: "Set your default delivery address",
                    onClick = { onOpenDialog(CustomerProfileDialog.AddressBook) }
                )
                ProfileMenuRow(
                    icon = Icons.Outlined.CreditCard,
                    title = "Payment Methods",
                    subtitle = defaultPaymentMethod,
                    onClick = { onOpenDialog(CustomerProfileDialog.PaymentMethods) }
                )
                ProfileMenuRow(
                    icon = Icons.Outlined.Notifications,
                    title = "Notifications",
                    subtitle = "Order, promo, and delivery alerts",
                    onClick = { onOpenDialog(CustomerProfileDialog.Notifications) }
                )
                ProfileMenuRow(
                    icon = Icons.Outlined.HelpOutline,
                    title = "Help Center",
                    subtitle = "FAQ and support contact",
                    onClick = { onOpenDialog(CustomerProfileDialog.HelpCenter) }
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
private fun CustomerProfileEditDialog(
    profile: SessionProfile,
    customerProfile: CustomerProfile?,
    onDismiss: () -> Unit,
    onSave: suspend (String, String, String) -> Result<String>,
    onSaved: () -> Unit
) {
    val fallbackName = fullName(profile)
    val scope = rememberCoroutineScope()
    var firstName by rememberSaveable(customerProfile?.uid) {
        mutableStateOf(customerProfile?.firstName?.ifBlank { fallbackName.substringBefore(" ") } ?: fallbackName.substringBefore(" "))
    }
    var lastName by rememberSaveable(customerProfile?.uid) {
        mutableStateOf(customerProfile?.lastName?.ifBlank { fallbackName.substringAfter(" ", "") } ?: fallbackName.substringAfter(" ", ""))
    }
    var phone by rememberSaveable(customerProfile?.uid) {
        mutableStateOf(customerProfile?.phone.orEmpty())
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
private fun CustomerAddressDialog(
    customerProfile: CustomerProfile?,
    onDismiss: () -> Unit,
    onSave: suspend (String) -> Result<String>,
    onSaved: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var address by rememberSaveable(customerProfile?.uid) {
        mutableStateOf(customerProfile?.address.orEmpty())
    }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isSaving by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = {
            Text(
                text = "Address Book",
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
                Text(
                    text = "Default address",
                    style = MaterialTheme.typography.labelLarge,
                    color = BeeMuted
                )
                HonestbeeTextField(
                    value = address,
                    onValueChange = { address = it },
                    placeholder = "Address",
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
                        val result = onSave(address)
                        isSaving = false
                        result.onSuccess { onSaved() }
                            .onFailure { errorMessage = it.message ?: "Could not update address." }
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
private fun CustomerPaymentMethodsDialog(
    selectedPaymentMethod: String,
    onDismiss: () -> Unit,
    onSave: suspend (String) -> Result<String>,
    onSaved: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var selected by rememberSaveable(selectedPaymentMethod) { mutableStateOf(selectedPaymentMethod) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isSaving by rememberSaveable { mutableStateOf(false) }
    val options = listOf("Cash on Delivery", "GCash", "Card placeholder")

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = {
            Text(
                text = "Payment Methods",
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
                            "Cash on Delivery" -> "Pay when your delivery arrives"
                            "GCash" -> "Android-only wallet preference"
                            else -> "Card support placeholder"
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
                            .onFailure { errorMessage = it.message ?: "Could not update payment method." }
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
private fun CustomerNotificationsDialog(
    orderUpdates: Boolean,
    promotions: Boolean,
    deliveryAlerts: Boolean,
    onDismiss: () -> Unit,
    onSave: suspend (Boolean, Boolean, Boolean) -> Result<String>,
    onSaved: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var orderUpdatesChecked by rememberSaveable(orderUpdates) { mutableStateOf(orderUpdates) }
    var promotionsChecked by rememberSaveable(promotions) { mutableStateOf(promotions) }
    var deliveryAlertsChecked by rememberSaveable(deliveryAlerts) { mutableStateOf(deliveryAlerts) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isSaving by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = {
            Text(
                text = "Notifications",
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
                    title = "Order updates",
                    subtitle = "Status changes and checkout reminders",
                    checked = orderUpdatesChecked,
                    onCheckedChange = { orderUpdatesChecked = it }
                )
                SettingsSwitchRow(
                    title = "Promotions",
                    subtitle = "Deals and app-only offers",
                    checked = promotionsChecked,
                    onCheckedChange = { promotionsChecked = it }
                )
                SettingsSwitchRow(
                    title = "Delivery alerts",
                    subtitle = "Rider and drop-off notifications",
                    checked = deliveryAlertsChecked,
                    onCheckedChange = { deliveryAlertsChecked = it }
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
                            orderUpdatesChecked,
                            promotionsChecked,
                            deliveryAlertsChecked
                        )
                        isSaving = false
                        result.onSuccess { onSaved() }
                            .onFailure { errorMessage = it.message ?: "Could not save notification settings." }
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
private fun CustomerHelpCenterDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Help Center",
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
                HelpFaqItem("How to place an order", "Open Stores, add items to your cart, then proceed to checkout.")
                HelpFaqItem("How to track delivery", "Track delivery progress from My Orders after checkout.")
                HelpFaqItem("How to cancel an order", "Open the order details and contact support before preparation starts.")
                HelpFaqItem("How to contact support", "Email support@honestbee.app with your account email and order number.")
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

private enum class CustomerProfileDialog {
    PersonalInformation,
    AddressBook,
    PaymentMethods,
    Notifications,
    HelpCenter
}
