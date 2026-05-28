package com.example.honestbeeapp.data.sample

import com.example.honestbeeapp.data.model.AndroidCartItem
import com.example.honestbeeapp.data.model.AndroidOrder
import com.example.honestbeeapp.data.model.AndroidProduct
import com.example.honestbeeapp.data.model.AndroidStore
import com.example.honestbeeapp.util.FirebaseConstants
import com.google.firebase.Timestamp

// Local Android-only sample data. Do not connect this to website carts, orders, or products.
object AndroidSampleData {
    val categories = listOf(
        "Fruits",
        "Vegetables",
        "Snacks",
        "Drinks",
        "Dairy",
        "Meat",
        "Rice",
        "Household"
    )

    private const val STORE_FRESH_MART_IMAGE = "https://loremflickr.com/640/420/grocery,store,produce?lock=801"
    private const val STORE_GREEN_BASKET_IMAGE = "https://loremflickr.com/640/420/farmers,market,vegetables?lock=802"
    private const val STORE_DAILY_NEEDS_IMAGE = "https://loremflickr.com/640/420/convenience,store,shelves?lock=803"
    private const val STORE_LOCAL_MARKET_IMAGE = "https://loremflickr.com/640/420/local,market,food?lock=804"
    private const val STORE_BEE_GROCERY_IMAGE = "https://loremflickr.com/640/420/supermarket,aisle,grocery?lock=805"

    private const val BANANAS_IMAGE = "https://loremflickr.com/640/480/bananas,fruit?lock=101"
    private const val ROYAL_ORANGE_IMAGE = "https://loremflickr.com/640/480/orange,soda,drink?lock=102"
    private const val OREO_COOKIES_IMAGE = "https://loremflickr.com/640/480/chocolate,cookies,snack?lock=103"
    private const val FRESH_APPLES_IMAGE = "https://loremflickr.com/640/480/apples,fruit?lock=104"
    private const val PREMIUM_RICE_IMAGE = "https://loremflickr.com/640/480/rice,grain,food?lock=105"
    private const val COCA_COLA_IMAGE = "https://loremflickr.com/640/480/cola,soda,drink?lock=106"
    private const val CHICKEN_BREAST_IMAGE = "https://loremflickr.com/640/480/chicken,meat,food?lock=107"
    private const val MILK_IMAGE = "https://loremflickr.com/640/480/milk,dairy?lock=108"
    private const val EGGS_IMAGE = "https://loremflickr.com/640/480/eggs,food?lock=109"
    private const val BREAD_IMAGE = "https://loremflickr.com/640/480/bread,bakery?lock=110"
    private const val DETERGENT_IMAGE = "https://loremflickr.com/640/480/laundry,detergent?lock=111"
    private const val CANNED_TUNA_IMAGE = "https://loremflickr.com/640/480/canned,food?lock=112"
    private const val INSTANT_NOODLES_IMAGE = "https://loremflickr.com/640/480/noodles,food?lock=113"
    private const val COFFEE_IMAGE = "https://loremflickr.com/640/480/coffee,jar?lock=114"
    private const val COOKING_OIL_IMAGE = "https://loremflickr.com/640/480/cooking,oil,kitchen?lock=115"
    private const val SUGAR_IMAGE = "https://loremflickr.com/640/480/sugar,food?lock=116"
    private const val POTATO_CHIPS_IMAGE = "https://loremflickr.com/640/480/potato,chips,snack?lock=117"
    private const val CARROTS_IMAGE = "https://loremflickr.com/640/480/carrots,vegetables?lock=118"
    private const val LETTUCE_IMAGE = "https://loremflickr.com/640/480/lettuce,vegetables?lock=119"
    private const val YOGURT_IMAGE = "https://loremflickr.com/640/480/yogurt,dairy?lock=120"

    val stores = listOf(
        AndroidStore(
            storeId = "store_fresh_mart",
            storeName = "Fresh Mart",
            category = "Grocery",
            imageUrl = STORE_FRESH_MART_IMAGE,
            address = "12 Honey Avenue, Quezon City",
            rating = 4.8,
            deliveryTime = "20-30 min",
            deliveryFee = 49.0,
            minimumOrder = 250.0,
            isOpen = true
        ),
        AndroidStore(
            storeId = "store_green_basket",
            storeName = "Green Basket",
            category = "Fresh Produce",
            imageUrl = STORE_GREEN_BASKET_IMAGE,
            address = "88 Market Road, Makati",
            rating = 4.7,
            deliveryTime = "25-35 min",
            deliveryFee = 55.0,
            minimumOrder = 300.0,
            isOpen = true
        ),
        AndroidStore(
            storeId = "store_daily_needs",
            storeName = "Daily Needs",
            category = "Convenience",
            imageUrl = STORE_DAILY_NEEDS_IMAGE,
            address = "45 Sunrise Street, Pasig",
            rating = 4.6,
            deliveryTime = "15-25 min",
            deliveryFee = 39.0,
            minimumOrder = 200.0,
            isOpen = true
        ),
        AndroidStore(
            storeId = "store_local_market",
            storeName = "Local Market",
            category = "Meat and Pantry",
            imageUrl = STORE_LOCAL_MARKET_IMAGE,
            address = "21 Barangay Plaza, Manila",
            rating = 4.5,
            deliveryTime = "30-45 min",
            deliveryFee = 59.0,
            minimumOrder = 350.0,
            isOpen = true
        ),
        AndroidStore(
            storeId = "store_bee_grocery",
            storeName = "Bee Grocery",
            category = "Supermarket",
            imageUrl = STORE_BEE_GROCERY_IMAGE,
            address = "9 Golden Lane, Taguig",
            rating = 4.9,
            deliveryTime = "20-35 min",
            deliveryFee = 45.0,
            minimumOrder = 250.0,
            isOpen = true
        )
    )

    val products = listOf(
        AndroidProduct(
            productId = "prod_bananas",
            storeId = "store_fresh_mart",
            productName = "Bananas",
            category = "Fruits",
            price = 89.0,
            stock = 60,
            unit = "1 kg",
            description = "Sweet ripe bananas for snacks and smoothies.",
            imageUrl = BANANAS_IMAGE,
            isAvailable = true
        ),
        AndroidProduct(
            productId = "prod_royal_orange",
            storeId = "store_daily_needs",
            productName = "Royal Orange",
            category = "Drinks",
            price = 78.0,
            stock = 34,
            unit = "1.5L",
            description = "Bright orange soda for family meals.",
            imageUrl = ROYAL_ORANGE_IMAGE,
            isAvailable = true
        ),
        AndroidProduct(
            productId = "prod_oreo_cookies",
            storeId = "store_daily_needs",
            productName = "Oreo Cookies",
            category = "Snacks",
            price = 65.0,
            stock = 42,
            unit = "pack",
            description = "Chocolate sandwich cookies.",
            imageUrl = OREO_COOKIES_IMAGE,
            isAvailable = true
        ),
        AndroidProduct(
            productId = "prod_fresh_apples",
            storeId = "store_green_basket",
            productName = "Fresh Apples",
            category = "Fruits",
            price = 155.0,
            stock = 48,
            unit = "1 kg",
            description = "Crisp apples for lunch boxes and desserts.",
            imageUrl = FRESH_APPLES_IMAGE,
            isAvailable = true
        ),
        AndroidProduct(
            productId = "prod_premium_rice",
            storeId = "store_bee_grocery",
            productName = "Premium Rice 5kg",
            category = "Rice",
            price = 325.0,
            stock = 24,
            unit = "5 kg",
            description = "Soft and fluffy premium white rice.",
            imageUrl = PREMIUM_RICE_IMAGE,
            isAvailable = true
        ),
        AndroidProduct(
            productId = "prod_coca_cola",
            storeId = "store_daily_needs",
            productName = "Coca Cola 1.5L",
            category = "Drinks",
            price = 82.0,
            stock = 36,
            unit = "1.5L",
            description = "Classic cola drink.",
            imageUrl = COCA_COLA_IMAGE,
            isAvailable = true
        ),
        AndroidProduct(
            productId = "prod_chicken_breast",
            storeId = "store_local_market",
            productName = "Chicken Breast 1kg",
            category = "Meat",
            price = 255.0,
            stock = 18,
            unit = "1 kg",
            description = "Fresh boneless chicken breast.",
            imageUrl = CHICKEN_BREAST_IMAGE,
            isAvailable = true
        ),
        AndroidProduct(
            productId = "prod_milk",
            storeId = "store_bee_grocery",
            productName = "Milk",
            category = "Dairy",
            price = 95.0,
            stock = 40,
            unit = "1L",
            description = "Creamy fresh milk.",
            imageUrl = MILK_IMAGE,
            isAvailable = true
        ),
        AndroidProduct(
            productId = "prod_eggs",
            storeId = "store_fresh_mart",
            productName = "Eggs",
            category = "Dairy",
            price = 115.0,
            stock = 30,
            unit = "dozen",
            description = "Farm fresh medium eggs.",
            imageUrl = EGGS_IMAGE,
            isAvailable = true
        ),
        AndroidProduct(
            productId = "prod_bread",
            storeId = "store_fresh_mart",
            productName = "Bread",
            category = "Snacks",
            price = 72.0,
            stock = 28,
            unit = "loaf",
            description = "Soft white sandwich bread.",
            imageUrl = BREAD_IMAGE,
            isAvailable = true
        ),
        AndroidProduct(
            productId = "prod_detergent",
            storeId = "store_bee_grocery",
            productName = "Detergent",
            category = "Household",
            price = 188.0,
            stock = 22,
            unit = "1 kg",
            description = "Laundry detergent powder.",
            imageUrl = DETERGENT_IMAGE,
            isAvailable = true
        ),
        AndroidProduct(
            productId = "prod_canned_tuna",
            storeId = "store_local_market",
            productName = "Canned Tuna",
            category = "Meat",
            price = 54.0,
            stock = 52,
            unit = "can",
            description = "Ready-to-eat tuna flakes.",
            imageUrl = CANNED_TUNA_IMAGE,
            isAvailable = true
        ),
        AndroidProduct(
            productId = "prod_instant_noodles",
            storeId = "store_daily_needs",
            productName = "Instant Noodles",
            category = "Snacks",
            price = 18.0,
            stock = 90,
            unit = "pack",
            description = "Quick noodle pack for busy days.",
            imageUrl = INSTANT_NOODLES_IMAGE,
            isAvailable = true
        ),
        AndroidProduct(
            productId = "prod_coffee",
            storeId = "store_bee_grocery",
            productName = "Coffee",
            category = "Drinks",
            price = 139.0,
            stock = 26,
            unit = "jar",
            description = "Rich instant coffee.",
            imageUrl = COFFEE_IMAGE,
            isAvailable = true
        ),
        AndroidProduct(
            productId = "prod_cooking_oil",
            storeId = "store_local_market",
            productName = "Cooking Oil",
            category = "Household",
            price = 172.0,
            stock = 20,
            unit = "1L",
            description = "All-purpose cooking oil.",
            imageUrl = COOKING_OIL_IMAGE,
            isAvailable = true
        ),
        AndroidProduct(
            productId = "prod_sugar",
            storeId = "store_local_market",
            productName = "Sugar",
            category = "Household",
            price = 82.0,
            stock = 44,
            unit = "1 kg",
            description = "Refined white sugar.",
            imageUrl = SUGAR_IMAGE,
            isAvailable = true
        ),
        AndroidProduct(
            productId = "prod_potato_chips",
            storeId = "store_daily_needs",
            productName = "Potato Chips",
            category = "Snacks",
            price = 69.0,
            stock = 39,
            unit = "bag",
            description = "Crunchy salted potato chips.",
            imageUrl = POTATO_CHIPS_IMAGE,
            isAvailable = true
        ),
        AndroidProduct(
            productId = "prod_carrots",
            storeId = "store_green_basket",
            productName = "Carrots",
            category = "Vegetables",
            price = 74.0,
            stock = 33,
            unit = "500 g",
            description = "Fresh carrots for soups and salads.",
            imageUrl = CARROTS_IMAGE,
            isAvailable = true
        ),
        AndroidProduct(
            productId = "prod_lettuce",
            storeId = "store_green_basket",
            productName = "Lettuce",
            category = "Vegetables",
            price = 96.0,
            stock = 21,
            unit = "head",
            description = "Crisp green lettuce.",
            imageUrl = LETTUCE_IMAGE,
            isAvailable = true
        ),
        AndroidProduct(
            productId = "prod_yogurt",
            storeId = "store_bee_grocery",
            productName = "Yogurt",
            category = "Dairy",
            price = 48.0,
            stock = 37,
            unit = "cup",
            description = "Creamy yogurt cup.",
            imageUrl = YOGURT_IMAGE,
            isAvailable = true
        )
    )

    val cartItems = listOf(
        AndroidCartItem(
            cartItemId = "cart_bananas",
            productId = "prod_bananas",
            storeId = "store_fresh_mart",
            productName = "Bananas",
            price = 89.0,
            quantity = 2,
            imageUrl = BANANAS_IMAGE,
            subtotal = 178.0
        ),
        AndroidCartItem(
            cartItemId = "cart_milk",
            productId = "prod_milk",
            storeId = "store_bee_grocery",
            productName = "Milk",
            price = 95.0,
            quantity = 1,
            imageUrl = MILK_IMAGE,
            subtotal = 95.0
        ),
        AndroidCartItem(
            cartItemId = "cart_bread",
            productId = "prod_bread",
            storeId = "store_fresh_mart",
            productName = "Bread",
            price = 72.0,
            quantity = 1,
            imageUrl = BREAD_IMAGE,
            subtotal = 72.0
        )
    )

    val orders = listOf(
        AndroidOrder(
            orderId = "HB-A1001",
            customerId = "sample_customer_1",
            merchantId = "store_fresh_mart",
            riderId = "sample_rider_1",
            storeName = "Fresh Mart",
            customerName = "Mika Santos",
            totalAmount = 394.0,
            deliveryFee = 49.0,
            status = FirebaseConstants.STATUS_COMPLETED,
            paymentMethod = "GCash",
            deliveryAddress = "BGC, Taguig",
            createdAt = Timestamp(1716696000L, 0),
            items = listOf(cartItems[0], cartItems[2])
        ),
        AndroidOrder(
            orderId = "HB-A1002",
            customerId = "sample_customer_2",
            merchantId = "store_bee_grocery",
            riderId = "sample_rider_2",
            storeName = "Bee Grocery",
            customerName = "Luis Reyes",
            totalAmount = 515.0,
            deliveryFee = 45.0,
            status = FirebaseConstants.STATUS_OUT_FOR_DELIVERY,
            paymentMethod = "Cash on Delivery",
            deliveryAddress = "Ortigas Center, Pasig",
            createdAt = Timestamp(1716703200L, 0),
            items = listOf(
                AndroidCartItem("cart_rice", "prod_premium_rice", "store_bee_grocery", "Premium Rice 5kg", 325.0, 1, PREMIUM_RICE_IMAGE, 325.0),
                AndroidCartItem("cart_milk_order", "prod_milk", "store_bee_grocery", "Milk", 95.0, 1, MILK_IMAGE, 95.0)
            )
        ),
        AndroidOrder(
            orderId = "HB-A1003",
            customerId = "sample_customer_3",
            merchantId = "store_local_market",
            riderId = "",
            storeName = "Local Market",
            customerName = "Ana Cruz",
            totalAmount = 486.0,
            deliveryFee = 59.0,
            status = FirebaseConstants.STATUS_TO_PREPARE,
            paymentMethod = "Card",
            deliveryAddress = "Ermita, Manila",
            createdAt = Timestamp(1716710400L, 0),
            items = listOf(
                AndroidCartItem("cart_chicken", "prod_chicken_breast", "store_local_market", "Chicken Breast 1kg", 255.0, 1, CHICKEN_BREAST_IMAGE, 255.0),
                AndroidCartItem("cart_oil", "prod_cooking_oil", "store_local_market", "Cooking Oil", 172.0, 1, COOKING_OIL_IMAGE, 172.0)
            )
        ),
        AndroidOrder(
            orderId = "HB-A1004",
            customerId = "sample_customer_4",
            merchantId = "store_green_basket",
            riderId = "",
            storeName = "Green Basket",
            customerName = "Paolo Lim",
            totalAmount = 380.0,
            deliveryFee = 55.0,
            status = FirebaseConstants.STATUS_TO_SHIP,
            paymentMethod = "GCash",
            deliveryAddress = "Salcedo Village, Makati",
            createdAt = Timestamp(1716717600L, 0),
            items = listOf(
                AndroidCartItem("cart_apples", "prod_fresh_apples", "store_green_basket", "Fresh Apples", 155.0, 1, FRESH_APPLES_IMAGE, 155.0),
                AndroidCartItem("cart_lettuce", "prod_lettuce", "store_green_basket", "Lettuce", 96.0, 1, LETTUCE_IMAGE, 96.0),
                AndroidCartItem("cart_carrots", "prod_carrots", "store_green_basket", "Carrots", 74.0, 1, CARROTS_IMAGE, 74.0)
            )
        ),
        AndroidOrder(
            orderId = "HB-A1005",
            customerId = "sample_customer_5",
            merchantId = "store_daily_needs",
            riderId = "",
            storeName = "Daily Needs",
            customerName = "Bea Garcia",
            totalAmount = 247.0,
            deliveryFee = 39.0,
            status = FirebaseConstants.STATUS_TO_PAY,
            paymentMethod = "Pending",
            deliveryAddress = "Cubao, Quezon City",
            createdAt = Timestamp(1716724800L, 0),
            items = listOf(
                AndroidCartItem("cart_oreo", "prod_oreo_cookies", "store_daily_needs", "Oreo Cookies", 65.0, 2, OREO_COOKIES_IMAGE, 130.0),
                AndroidCartItem("cart_cola", "prod_coca_cola", "store_daily_needs", "Coca Cola 1.5L", 82.0, 1, COCA_COLA_IMAGE, 82.0)
            )
        ),
        AndroidOrder(
            orderId = "HB-A1006",
            customerId = "sample_customer_6",
            merchantId = "store_bee_grocery",
            riderId = "sample_rider_1",
            storeName = "Bee Grocery",
            customerName = "Noel Tan",
            totalAmount = 236.0,
            deliveryFee = 45.0,
            status = FirebaseConstants.STATUS_CANCELLED,
            paymentMethod = "Card",
            deliveryAddress = "Kapitolyo, Pasig",
            createdAt = Timestamp(1716732000L, 0),
            items = listOf(
                AndroidCartItem("cart_detergent", "prod_detergent", "store_bee_grocery", "Detergent", 188.0, 1, DETERGENT_IMAGE, 188.0)
            )
        )
    )

    val riderJobs = listOf(
        SampleRiderJob(
            jobId = "job_available_green_basket",
            orderId = "HB-A1004",
            storeName = "Green Basket",
            customerName = "Paolo Lim",
            pickupAddress = "88 Market Road, Makati",
            deliveryAddress = "Salcedo Village, Makati",
            status = "Available",
            payout = 88.0,
            distanceKm = 3.2,
            deliveryTime = "25 min"
        ),
        SampleRiderJob(
            jobId = "job_available_local_market",
            orderId = "HB-A1003",
            storeName = "Local Market",
            customerName = "Ana Cruz",
            pickupAddress = "21 Barangay Plaza, Manila",
            deliveryAddress = "Ermita, Manila",
            status = "Available",
            payout = 94.0,
            distanceKm = 4.1,
            deliveryTime = "30 min"
        ),
        SampleRiderJob(
            jobId = "job_current_bee_grocery",
            orderId = "HB-A1002",
            storeName = "Bee Grocery",
            customerName = "Luis Reyes",
            pickupAddress = "9 Golden Lane, Taguig",
            deliveryAddress = "Ortigas Center, Pasig",
            status = "Current",
            payout = 102.0,
            distanceKm = 5.6,
            deliveryTime = "35 min"
        ),
        SampleRiderJob(
            jobId = "job_history_fresh_mart",
            orderId = "HB-A1001",
            storeName = "Fresh Mart",
            customerName = "Mika Santos",
            pickupAddress = "12 Honey Avenue, Quezon City",
            deliveryAddress = "BGC, Taguig",
            status = FirebaseConstants.STATUS_COMPLETED,
            payout = 96.0,
            distanceKm = 4.8,
            deliveryTime = "28 min"
        ),
        SampleRiderJob(
            jobId = "job_history_cancelled",
            orderId = "HB-A1006",
            storeName = "Bee Grocery",
            customerName = "Noel Tan",
            pickupAddress = "9 Golden Lane, Taguig",
            deliveryAddress = "Kapitolyo, Pasig",
            status = FirebaseConstants.STATUS_CANCELLED,
            payout = 0.0,
            distanceKm = 2.4,
            deliveryTime = "Cancelled"
        )
    )

    val reports = listOf(
        SampleReport("Android stores", stores.size.toString(), "Sample stores ready"),
        SampleReport("Android products", products.size.toString(), "Across 8 categories"),
        SampleReport("Android orders", orders.size.toString(), "Demo order statuses"),
        SampleReport("Rider jobs", riderJobs.size.toString(), "Available, current, and history")
    )
}

data class SampleRiderJob(
    val jobId: String = "",
    val orderId: String = "",
    val storeName: String = "",
    val customerName: String = "",
    val pickupAddress: String = "",
    val deliveryAddress: String = "",
    val status: String = "",
    val payout: Double = 0.0,
    val distanceKm: Double = 0.0,
    val deliveryTime: String = ""
)

data class SampleReport(
    val title: String = "",
    val value: String = "",
    val note: String = ""
)
