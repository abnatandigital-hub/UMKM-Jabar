package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlin.random.Random

class UmkmRepository(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val walletDao = db.walletDao()
    private val txDao = db.walletTransactionDao()
    private val cartDao = db.cartDao()
    private val orderDao = db.foodOrderDao()
    private val rideDao = db.rideBookingDao()
    private val merchantDao = db.foodMerchantDao()
    private val menuDao = db.foodMenuDao()

    // --- Dynamic UMKM Food Data (Flows from DB) ---
    fun getMerchants(): Flow<List<FoodMerchant>> = merchantDao.getMerchantsFlow()
    fun getMenus(): Flow<List<FoodMenu>> = menuDao.getMenusFlow()

    // --- Admin CRUD Operations ---
    suspend fun addMerchant(merchant: FoodMerchant): Long {
        return merchantDao.insertMerchant(merchant)
    }

    suspend fun updateMerchant(merchant: FoodMerchant) {
        merchantDao.updateMerchant(merchant)
    }

    suspend fun deleteMerchant(merchant: FoodMerchant) {
        // Also delete all menus belonging to this merchant
        val currentMenus = menuDao.getMenusFlow().firstOrNull() ?: emptyList()
        currentMenus.filter { it.merchantId == merchant.id }.forEach {
            menuDao.deleteMenu(it)
        }
        merchantDao.deleteMerchant(merchant)
    }

    suspend fun addMenu(menu: FoodMenu): Long {
        return menuDao.insertMenu(menu)
    }

    suspend fun updateMenu(menu: FoodMenu) {
        menuDao.updateMenu(menu)
    }

    suspend fun deleteMenu(menu: FoodMenu) {
        menuDao.deleteMenu(menu)
    }

    private val defaultMerchants = listOf(
        FoodMerchant(1, "Seblak Jeletot Cihampelas", 4.8, "1.2 km", 8000.0, "Seblak pedas khas Bandung dengan aneka topping melimpah.", "Pedas, Kuah, Cemilan", "🌶️"),
        FoodMerchant(2, "Batagor & Siomay Haji Isur", 4.9, "2.4 km", 10000.0, "Batagor ikan tenggiri legendaris renyah dengan bumbu kacang kental.", "Gorengan, Ikan, Legendaris", "🥟"),
        FoodMerchant(3, "Surabi Setiabudi Khas Jabar", 4.7, "3.1 km", 12000.0, "Surabi tradisional bakar arang dengan varian asin oncom dan manis susu.", "Kue, Tradisional, Manis/Asin", "🥞"),
        FoodMerchant(4, "Kupat Tahu Gempol", 4.9, "0.8 km", 6000.0, "Sarapan kupat tahu legendaris Bandung sejak 1975, bumbu kacang lembut.", "Sarapan, Tahu, Kenyang", "🥗"),
        FoodMerchant(5, "Es Cendol Elizabeth", 4.9, "1.9 km", 8000.0, "Cendol nangka gula aren murni menyegarkan tenggorokan.", "Minuman, Manis, Segar", "🥤")
    )

    private val defaultMenus = listOf(
        // Seblak
        FoodMenu(101, 1, "Seblak Spesial Ceker", "Seblak kuah membara dengan topping telur, ceker ayam empuk, kerupuk basah, dan makaroni.", 18000.0, "🍲"),
        FoodMenu(102, 1, "Seblak Komplit Sosis Bakso", "Seblak lengkap dengan tambahan potongan sosis sapi dan bakso kenyal.", 20000.0, "🍜"),
        FoodMenu(103, 1, "Seblak Macaroni Pedas Kering", "Seblak versi tumis kering rasa kencur pedas berlevel.", 15000.0, "🍛"),

        // Batagor
        FoodMenu(201, 2, "Batagor Set (Isi 5)", "Tahu bakso goreng crispy lengkap dengan bumbu kacang khas Bandung dan kecap manis.", 25000.0, "🍱"),
        FoodMenu(202, 2, "Siomay Bandung Set (Isi 5)", "Siomay kukus kol, kentang, tahu, telur dengan siraman saus kacang gurih.", 25000.0, "🍽️"),
        FoodMenu(203, 2, "Batagor Kuah Hangat", "Batagor yang disajikan dengan kuah kaldu sapi hangat nan segar.", 22000.0, "🥣"),

        // Surabi
        FoodMenu(301, 3, "Surabi Oncom Pedas", "Surabi asin dengan taburan oncom pedas sangrai tradisional.", 12000.0, "🫓"),
        FoodMenu(302, 3, "Surabi Keju Susu", "Surabi manis dengan taburan keju cheddar parut melimpah dan susu kental manis.", 14000.0, "🧀"),
        FoodMenu(303, 3, "Surabi Coklat Pisang", "Surabi manis ber-topping cokelat lumer dan irisan pisang raja.", 15000.0, "🍌"),

        // Kupat Tahu
        FoodMenu(401, 4, "Kupat Tahu Porsi Biasa", "Ketupat, tahu goreng Gempol, tauge rebus, disiram saus kacang khas dan kerupuk merah.", 18000.0, "🥗"),
        FoodMenu(402, 4, "Kupat Tahu Telur Dadar", "Kupat tahu lengkap dengan tambahan telur dadar goreng gurih.", 22000.0, "🍳"),

        // Es Cendol
        FoodMenu(501, 5, "Es Cendol Gelas Besar", "Cendol hijau alami pandan, santan gurih, gula aren murni, dan es serut.", 10000.0, "🥤"),
        FoodMenu(502, 5, "Es Cendol Durian", "Es cendol spesial dengan topping daging durian asli Medan yang legit.", 18000.0, "🍈"),
        FoodMenu(503, 5, "Es Cendol Alpukat", "Es cendol dengan tambahan kerukan buah alpukat mentega segar.", 15000.0, "🥑")
    )

    // --- DB Getters (Flows) ---
    fun getWallet(): Flow<Wallet?> = walletDao.getWalletFlow()
    fun getTransactions(): Flow<List<WalletTransaction>> = txDao.getTransactionsFlow()
    fun getCartItems(): Flow<List<CartItem>> = cartDao.getCartItemsFlow()
    fun getFoodOrders(): Flow<List<FoodOrder>> = orderDao.getOrdersFlow()
    fun getRideBookings(): Flow<List<RideBooking>> = rideDao.getBookingsFlow()

    // --- Seed Default Wallet, Merchants, and Menus ---
    suspend fun checkAndSeedWallet() {
        val current = walletDao.getWalletDirect()
        if (current == null) {
            walletDao.insertOrUpdateWallet(Wallet())
            // Insert initial top up transaction for realism
            txDao.insertTransaction(
                WalletTransaction(
                    type = "TOP_UP",
                    amount = 500000.0,
                    recipientOrSource = "Saldo Awal Dompet"
                )
            )
        }

        // Seed Merchants
        if (merchantDao.getMerchantsCount() == 0) {
            defaultMerchants.forEach {
                merchantDao.insertMerchant(it)
            }
        }

        // Seed Menus
        if (menuDao.getMenusCount() == 0) {
            defaultMenus.forEach {
                menuDao.insertMenu(it)
            }
        }
    }

    // --- Wallet Actions ---
    suspend fun topUpWallet(amount: Double, source: String): Boolean {
        val wallet = walletDao.getWalletDirect() ?: Wallet()
        val updatedWallet = wallet.copy(balance = wallet.balance + amount)
        walletDao.insertOrUpdateWallet(updatedWallet)
        txDao.insertTransaction(
            WalletTransaction(
                type = "TOP_UP",
                amount = amount,
                recipientOrSource = source
            )
        )
        return true
    }

    suspend fun transferWallet(amount: Double, recipientPhone: String, recipientName: String): Boolean {
        val wallet = walletDao.getWalletDirect() ?: return false
        if (wallet.balance < amount) return false

        val updatedWallet = wallet.copy(balance = wallet.balance - amount)
        walletDao.insertOrUpdateWallet(updatedWallet)

        txDao.insertTransaction(
            WalletTransaction(
                type = "TRANSFER_OUT",
                amount = amount,
                recipientOrSource = "$recipientName ($recipientPhone)"
            )
        )
        return true
    }

    // --- Cart Actions ---
    suspend fun addToCart(menu: FoodMenu, merchant: FoodMerchant, quantity: Int = 1) {
        val existingItems = cartDao.getCartItemsFlow().firstOrNull() ?: emptyList()
        val existing = existingItems.find { it.foodItemId == menu.id }

        // Rule: Can only add items from the same merchant at a time
        val otherMerchantItems = existingItems.filter { it.merchantId != merchant.id }
        if (otherMerchantItems.isNotEmpty()) {
            // clear cart first if switching merchants (like Grab)
            cartDao.clearCart()
        }

        if (existing != null) {
            cartDao.insertOrUpdateCartItem(
                existing.copy(quantity = existing.quantity + quantity)
            )
        } else {
            cartDao.insertOrUpdateCartItem(
                CartItem(
                    foodItemId = menu.id,
                    name = menu.name,
                    price = menu.price,
                    quantity = quantity,
                    merchantId = merchant.id,
                    merchantName = merchant.name
                )
            )
        }
    }

    suspend fun updateCartQuantity(item: CartItem, newQty: Int) {
        if (newQty <= 0) {
            cartDao.deleteCartItem(item)
        } else {
            cartDao.insertOrUpdateCartItem(item.copy(quantity = newQty))
        }
    }

    suspend fun removeCartItem(item: CartItem) {
        cartDao.deleteCartItem(item)
    }

    suspend fun clearCart() {
        cartDao.clearCart()
    }

    // --- Checkout Food Order ---
    suspend fun checkoutFoodOrder(): FoodOrder? {
        val cartItems = cartDao.getCartItemsFlow().firstOrNull() ?: return null
        if (cartItems.isEmpty()) return null

        val merchantName = cartItems.first().merchantName
        val totalAmount = cartItems.sumOf { it.price * it.quantity } + 10000.0 // including 10k flat delivery fee

        val wallet = walletDao.getWalletDirect() ?: return null
        if (wallet.balance < totalAmount) return null // Insufficient Balance

        // Deduct wallet
        val updatedWallet = wallet.copy(balance = wallet.balance - totalAmount)
        walletDao.insertOrUpdateWallet(updatedWallet)

        // Add Transaction
        txDao.insertTransaction(
            WalletTransaction(
                type = "FOOD_PAYMENT",
                amount = totalAmount,
                recipientOrSource = merchantName
            )
        )

        // Create Items Summary
        val summary = cartItems.joinToString(", ") { "${it.name} x${it.quantity}" }

        // Create Order
        val order = FoodOrder(
            merchantName = merchantName,
            itemsSummary = summary,
            totalAmount = totalAmount,
            status = "PREPARING"
        )
        val orderId = orderDao.insertOrder(order)

        // Clear Cart
        cartDao.clearCart()

        return order.copy(id = orderId.toInt())
    }

    // --- Simulated Driver Matching for Ride ---
    private val firstNames = listOf("Asep", "Dadang", "Cecep", "Agus", "Yudi", "Wildan", "Ginanjar", "Dani", "Ujang")
    private val lastNames = listOf("Sunandar", "Hermawan", "Mulyana", "Hidayat", "Sudrajat", "Kusnandar", "Ramdhan")
    private val plates = listOf("D 4321 SAT", "D 8888 JBR", "D 2026 BRG", "D 1104 SGE", "D 9081 GDS")

    suspend fun createRideBooking(
        serviceType: String,
        origin: String,
        destination: String,
        price: Double
    ): RideBooking? {
        val wallet = walletDao.getWalletDirect() ?: return null
        if (wallet.balance < price) return null // Insufficient balance

        // Deduct Wallet
        val updatedWallet = wallet.copy(balance = wallet.balance - price)
        walletDao.insertOrUpdateWallet(updatedWallet)

        // Add transaction
        txDao.insertTransaction(
            WalletTransaction(
                type = "RIDE_PAYMENT",
                amount = price,
                recipientOrSource = "Ride $serviceType ($origin ke $destination)"
            )
        )

        // Match simulated driver
        val driverName = "${firstNames.random()} ${lastNames.random()}"
        val driverPhone = "08${Random.nextInt(111111111, 999999999)}"
        val driverVehicle = plates.random() + if (serviceType == "MOTOR") " (Motor)" else " (Mobil)"

        val booking = RideBooking(
            serviceType = serviceType,
            origin = origin,
            destination = destination,
            price = price,
            driverName = driverName,
            driverPhone = driverPhone,
            driverVehicle = driverVehicle,
            status = "FINDING_DRIVER"
        )

        val id = rideDao.insertBooking(booking)
        return booking.copy(id = id.toInt())
    }

    suspend fun updateRideStatus(booking: RideBooking, newStatus: String) {
        rideDao.updateBooking(booking.copy(status = newStatus))
    }

    suspend fun updateOrderStatus(order: FoodOrder, newStatus: String) {
        orderDao.updateOrder(order.copy(status = newStatus))
    }
}
