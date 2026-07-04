package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CartItem
import com.example.data.FoodMerchant
import com.example.data.FoodMenu
import com.example.data.WalletTransaction
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.*

// Dynamic Color Palette for "UMKM Jabar" (High Density Green combined with West Java Yellow-Gold & Rich Slates)
val JabarGreen = Color(0xFF008444) // High Density Green
val JabarDarkGreen = Color(0xFF005C2E)
val JabarGold = Color(0xFFF9A825) // Golden amber
val JabarLightGold = Color(0xFFFFF9C4)
val JabarSlateBg = Color(0xFFF8FAF9) // High Density Slate BG
val JabarCardBg = Color(0xFFFFFFFF)

// Simple Format Currency to Rupiah (IDR)
fun formatRupiah(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    return format.format(amount).replace("Rp", "Rp ").replace(",00", "")
}

// --- Main App Scaffolding ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UmkmJabarApp(
    viewModel: UmkmViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val walletState by viewModel.walletState.collectAsState()
    val transactionsState by viewModel.transactionsState.collectAsState()
    val cartItemsState by viewModel.cartItemsState.collectAsState()
    val foodOrdersState by viewModel.foodOrdersState.collectAsState()
    val rideBookingsState by viewModel.rideBookingsState.collectAsState()
    val merchantsState by viewModel.merchantsState.collectAsState()
    val menusState by viewModel.menusState.collectAsState()

    val activeBooking by viewModel.activeBooking.collectAsState()
    val activeOrder by viewModel.activeOrder.collectAsState()

    // Screen States: HOME, FOOD, MERCH_DETAIL, CART, RIDE_BOOK, WALLET, ACTIVITY, TOPUP, TRANSFER
    var currentScreen by remember { mutableStateOf("HOME") }
    val selectedMerchant by viewModel.selectedMerchant.collectAsState()
    var selectedRideType by remember { mutableStateOf("MOTOR") } // "MOTOR" or "CAR"

    // Inputs for Wallet Actions
    var topUpAmountStr by remember { mutableStateOf("") }
    var transferAmountStr by remember { mutableStateOf("") }
    var transferPhone by remember { mutableStateOf("") }
    var transferName by remember { mutableStateOf("") }

    // Inputs for Ride Hailing
    var rideOrigin by remember { mutableStateOf("") }
    var rideDestination by remember { mutableStateOf("") }
    var calculatedPrice by remember { mutableStateOf(0.0) }

    // Alert dialogs
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successDialogMsg by remember { mutableStateOf("") }

    fun showMsg(msg: String) {
        snackbarMessage = msg
    }

    Scaffold(
        bottomBar = {
            if (currentScreen in listOf("HOME", "ACTIVITY", "WALLET")) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentScreen == "HOME",
                        onClick = { currentScreen = "HOME" },
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Beranda") },
                        label = { Text("Beranda", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = JabarGreen,
                            selectedTextColor = JabarGreen,
                            indicatorColor = JabarGreen.copy(alpha = 0.15f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == "ACTIVITY",
                        onClick = { currentScreen = "ACTIVITY" },
                        icon = { Icon(Icons.Filled.History, contentDescription = "Aktivitas") },
                        label = { Text("Aktivitas", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = JabarGreen,
                            selectedTextColor = JabarGreen,
                            indicatorColor = JabarGreen.copy(alpha = 0.15f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == "WALLET",
                        onClick = { currentScreen = "WALLET" },
                        icon = { Icon(Icons.Filled.AccountBalanceWallet, contentDescription = "Dompet") },
                        label = { Text("Dompet", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = JabarGreen,
                            selectedTextColor = JabarGreen,
                            indicatorColor = JabarGreen.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(JabarSlateBg)
                .padding(innerPadding)
        ) {
            // Screen Switcher
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    "HOME" -> HomeScreen(
                        walletBalance = walletState?.balance ?: 500000.0,
                        activeBooking = activeBooking,
                        activeOrder = activeOrder,
                        merchants = merchantsState,
                        onFoodClick = { currentScreen = "FOOD" },
                        onRideClick = { type ->
                            selectedRideType = type
                            rideOrigin = ""
                            rideDestination = ""
                            calculatedPrice = 0.0
                            currentScreen = "RIDE_BOOK"
                        },
                        onTopUpClick = {
                            topUpAmountStr = ""
                            currentScreen = "TOPUP"
                        },
                        onTransferClick = {
                            transferAmountStr = ""
                            transferPhone = ""
                            transferName = ""
                            currentScreen = "TRANSFER"
                        },
                        onHistoryClick = { currentScreen = "WALLET" },
                        onMerchantSelect = { merchant ->
                            viewModel.selectMerchant(merchant)
                            currentScreen = "MERCH_DETAIL"
                        },
                        onDismissBooking = { viewModel.dismissActiveBooking() },
                        onDismissOrder = { viewModel.dismissActiveOrder() },
                        onAdminClick = { currentScreen = "ADMIN_LOGIN" }
                    )

                    "FOOD" -> FoodListScreen(
                        merchants = merchantsState,
                        onBackClick = { currentScreen = "HOME" },
                        onMerchantSelect = { merchant ->
                            viewModel.selectMerchant(merchant)
                            currentScreen = "MERCH_DETAIL"
                        },
                        cartCount = cartItemsState.sumOf { it.quantity },
                        onCartClick = { currentScreen = "CART" }
                    )

                    "MERCH_DETAIL" -> selectedMerchant?.let { merchant ->
                        MerchantDetailScreen(
                            merchant = merchant,
                            menus = menusState.filter { it.merchantId == merchant.id },
                            cartItems = cartItemsState,
                            onBackClick = { currentScreen = "FOOD" },
                            onAddToCart = { menu ->
                                viewModel.addToCart(menu)
                                showMsg("${menu.name} ditambahkan ke keranjang!")
                            },
                            onCartClick = { currentScreen = "CART" }
                        )
                    }

                    "CART" -> CartScreen(
                        cartItems = cartItemsState,
                        walletBalance = walletState?.balance ?: 0.0,
                        onBackClick = { currentScreen = "FOOD" },
                        onQtyIncrease = { item -> viewModel.increaseQty(item) },
                        onQtyDecrease = { item -> viewModel.decreaseQty(item) },
                        onClearCart = { viewModel.clearCart() },
                        onCheckout = {
                            viewModel.checkoutFood(
                                onSuccess = {
                                    successDialogMsg = "Pesanan makanan Anda berhasil dibuat! Driver JabarFood sedang menjemput makanan."
                                    showSuccessDialog = true
                                    currentScreen = "HOME"
                                },
                                onError = { err -> showMsg(err) }
                            )
                        }
                    )

                    "RIDE_BOOK" -> RideBookingScreen(
                        serviceType = selectedRideType,
                        walletBalance = walletState?.balance ?: 0.0,
                        origin = rideOrigin,
                        destination = rideDestination,
                        price = calculatedPrice,
                        onOriginChange = {
                            rideOrigin = it
                            // simulate dynamic pricing based on input length
                            calculatedPrice = if (rideOrigin.isNotEmpty() && rideDestination.isNotEmpty()) {
                                (12000 + (rideOrigin.length + rideDestination.length) * 500).toDouble()
                            } else 0.0
                        },
                        onDestinationChange = {
                            rideDestination = it
                            calculatedPrice = if (rideOrigin.isNotEmpty() && rideDestination.isNotEmpty()) {
                                (12000 + (rideOrigin.length + rideDestination.length) * 500).toDouble()
                            } else 0.0
                        },
                        onBackClick = { currentScreen = "HOME" },
                        onBook = {
                            viewModel.bookRide(
                                serviceType = selectedRideType,
                                origin = rideOrigin,
                                destination = rideDestination,
                                price = calculatedPrice,
                                onSuccess = {
                                    successDialogMsg = "Pencarian pengemudi berhasil! Driver sedang dalam perjalanan menjemput Anda."
                                    showSuccessDialog = true
                                    currentScreen = "HOME"
                                },
                                onError = { err -> showMsg(err) }
                            )
                        }
                    )

                    "WALLET" -> WalletScreen(
                        walletState = walletState,
                        transactions = transactionsState,
                        onTopUpClick = {
                            topUpAmountStr = ""
                            currentScreen = "TOPUP"
                        },
                        onTransferClick = {
                            transferAmountStr = ""
                            transferPhone = ""
                            transferName = ""
                            currentScreen = "TRANSFER"
                        }
                    )

                    "TOPUP" -> TopUpScreen(
                        amountText = topUpAmountStr,
                        onAmountChange = { topUpAmountStr = it },
                        onBackClick = { currentScreen = "HOME" },
                        onSubmit = { amount ->
                            viewModel.topUp(amount, "Top Up JabarPay")
                            successDialogMsg = "Top Up sebesar ${formatRupiah(amount)} berhasil ditambahkan ke saldo JabarPay Anda!"
                            showSuccessDialog = true
                            currentScreen = "HOME"
                        }
                    )

                    "TRANSFER" -> TransferScreen(
                        amountText = transferAmountStr,
                        phoneNumber = transferPhone,
                        recipientName = transferName,
                        walletBalance = walletState?.balance ?: 0.0,
                        onAmountChange = { transferAmountStr = it },
                        onPhoneChange = { transferPhone = it },
                        onNameChange = { transferName = it },
                        onBackClick = { currentScreen = "HOME" },
                        onSubmit = { amount, phone, name ->
                            viewModel.transfer(
                                amount = amount,
                                phone = phone,
                                name = name,
                                onSuccess = {
                                    successDialogMsg = "Transfer sebesar ${formatRupiah(amount)} kepada $name ($phone) berhasil dikirim!"
                                    showSuccessDialog = true
                                    currentScreen = "HOME"
                                },
                                onError = { err -> showMsg(err) }
                            )
                        }
                    )

                    "ACTIVITY" -> ActivityHistoryScreen(
                        orders = foodOrdersState,
                        rides = rideBookingsState
                    )

                    "ADMIN_LOGIN" -> AdminLoginScreen(
                        onBackClick = { currentScreen = "HOME" },
                        onLoginSuccess = {
                            currentScreen = "ADMIN_DASHBOARD"
                        }
                    )

                    "ADMIN_DASHBOARD" -> AdminDashboardScreen(
                        merchants = merchantsState,
                        menus = menusState,
                        orders = foodOrdersState,
                        rides = rideBookingsState,
                        onBackClick = { currentScreen = "HOME" },
                        onAddMerchant = { merchant -> viewModel.addMerchant(merchant) },
                        onUpdateMerchant = { merchant -> viewModel.updateMerchant(merchant) },
                        onDeleteMerchant = { merchant -> viewModel.deleteMerchant(merchant) },
                        onAddMenu = { menu -> viewModel.addMenu(menu) },
                        onUpdateMenu = { menu -> viewModel.updateMenu(menu) },
                        onDeleteMenu = { menu -> viewModel.deleteMenu(menu) },
                        onUpdateOrderStatus = { order, newStatus -> viewModel.updateOrderStatus(order, newStatus) },
                        onUpdateRideStatus = { ride, newStatus -> viewModel.updateRideStatus(ride, newStatus) }
                    )
                }
            }

            // Floating Snackbar / Toast Custom Banner
            snackbarMessage?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF323232)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = msg,
                            color = Color.White,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = { snackbarMessage = null },
                            colors = ButtonDefaults.textButtonColors(contentColor = JabarGreen)
                        ) {
                            Text("OK", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                LaunchedEffect(msg) {
                    delay(3000)
                    if (snackbarMessage == msg) {
                        snackbarMessage = null
                    }
                }
            }

            // Success Dialog
            if (showSuccessDialog) {
                AlertDialog(
                    onDismissRequest = { showSuccessDialog = false },
                    icon = {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = "Sukses",
                            tint = JabarGreen,
                            modifier = Modifier.size(56.dp)
                        )
                    },
                    title = {
                        Text(
                            "Transaksi Berhasil!",
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    },
                    text = {
                        Text(
                            successDialogMsg,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { showSuccessDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = JabarGreen)
                        ) {
                            Text("Selesai")
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}

// ==========================================
// SCREEN 1: HOME SCREEN
// ==========================================
@Composable
fun HomeScreen(
    walletBalance: Double,
    activeBooking: com.example.data.RideBooking?,
    activeOrder: com.example.data.FoodOrder?,
    merchants: List<FoodMerchant>,
    onFoodClick: () -> Unit,
    onRideClick: (String) -> Unit,
    onTopUpClick: () -> Unit,
    onTransferClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onMerchantSelect: (FoodMerchant) -> Unit,
    onDismissBooking: () -> Unit,
    onDismissOrder: () -> Unit,
    onAdminClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_scroll"),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // High Density Header with Location and Search bar
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(JabarGreen)
                    .padding(top = 16.dp, bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = "Lokasi Anda",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "LOKASI ANDA",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Gedung Sate, Bandung, Jawa Barat",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            Icons.Filled.Notifications,
                            contentDescription = "Notifikasi",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Filled.AccountCircle,
                            contentDescription = "Admin / Profil",
                            tint = Color.White,
                            modifier = Modifier
                                .size(26.dp)
                                .clickable { onAdminClick() }
                                .testTag("admin_profile_button")
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Search Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(24.dp))
                            .clickable { onFoodClick() }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = "Cari",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Cari Seblak, Batagor, atau Transport...",
                            color = Color.Gray.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // WALLET CARD (JabarPay) - Compact High Density Style
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)), // slate-100 border
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("wallet_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1.1f)
                            .padding(end = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(JabarGreen.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.AccountBalanceWallet,
                                contentDescription = "Wallet Icon",
                                tint = JabarGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "JABARPAY",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Text(
                                text = formatRupiah(walletBalance),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray
                            )
                        }
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(32.dp)
                            .background(Color(0xFFE2E8F0))
                    )

                    // Right side compact wallet buttons
                    Row(
                        modifier = Modifier
                            .weight(1.9f)
                            .padding(start = 4.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        WalletCompactButton(
                            icon = Icons.Filled.AddCircle,
                            text = "Isi Saldo",
                            onClick = onTopUpClick,
                            testTag = "top_up_button"
                        )
                        WalletCompactButton(
                            icon = Icons.Filled.Send,
                            text = "Bayar",
                            onClick = onTransferClick,
                            testTag = "transfer_button"
                        )
                        WalletCompactButton(
                            icon = Icons.Filled.History,
                            text = "Riwayat",
                            onClick = onHistoryClick,
                            testTag = "history_button"
                        )
                    }
                }
            }
        }

        // ACTIVE TRACKERS
        if (activeBooking != null) {
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    ActiveTrackingCard(
                        title = "Driver Sedang Menuju Lokasi Anda",
                        statusText = when (activeBooking.status) {
                            "FINDING_DRIVER" -> "Mencari Pengemudi..."
                            "ON_WAY" -> "Driver: ${activeBooking.driverName} (${activeBooking.driverVehicle})"
                            else -> "Selesai"
                        },
                        progress = when (activeBooking.status) {
                            "FINDING_DRIVER" -> 0.3f
                            "ON_WAY" -> 0.7f
                            else -> 1f
                        },
                        icon = Icons.Filled.DirectionsCar,
                        info = "${activeBooking.origin} ➔ ${activeBooking.destination}",
                        onDismiss = onDismissBooking
                    )
                }
            }
        }

        if (activeOrder != null) {
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    ActiveTrackingCard(
                        title = "Pengantaran Makanan JabarFood",
                        statusText = when (activeOrder.status) {
                            "PREPARING" -> "Makanan sedang disiapkan oleh UMKM..."
                            "ON_THE_WAY" -> "Driver sedang mengirim kuliner hangat Anda..."
                            else -> "Selesai diantar"
                        },
                        progress = when (activeOrder.status) {
                            "PREPARING" -> 0.4f
                            "ON_THE_WAY" -> 0.8f
                            else -> 1f
                        },
                        icon = Icons.Filled.Fastfood,
                        info = "${activeOrder.merchantName} (${formatRupiah(activeOrder.totalAmount)})",
                        onDismiss = onDismissOrder
                    )
                }
            }
        }

        // SERVICES QUICK ACTIONS GRID (HIGH DENSITY 4x2)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Layanan UMKM Jabar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1E293B) // slate-800
                )

                // Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ServiceGridButtonHighDensity(
                        icon = Icons.Filled.Restaurant,
                        title = "JabarFood",
                        containerColor = Color(0xFFFEF2F2), // red-50
                        iconColor = Color(0xFFEF4444), // red-500
                        onClick = onFoodClick,
                        testTag = "service_food"
                    )
                    ServiceGridButtonHighDensity(
                        icon = Icons.Filled.TwoWheeler,
                        title = "JabarRide",
                        containerColor = Color(0xFFF0FDF4), // green-50
                        iconColor = Color(0xFF16A34A), // green-600
                        onClick = { onRideClick("MOTOR") },
                        testTag = "service_ride_motor"
                    )
                    ServiceGridButtonHighDensity(
                        icon = Icons.Filled.DirectionsCar,
                        title = "JabarCar",
                        containerColor = Color(0xFFEFF6FF), // blue-50
                        iconColor = Color(0xFF2563EB), // blue-600
                        onClick = { onRideClick("CAR") },
                        testTag = "service_ride_car"
                    )
                    ServiceGridButtonHighDensity(
                        icon = Icons.Filled.Store,
                        title = "JabarMart",
                        containerColor = Color(0xFFFFF7ED), // orange-50
                        iconColor = Color(0xFFF97316), // orange-500
                        onClick = onFoodClick, // linked to food for flow
                        testTag = "service_mart"
                    )
                }

                // Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ServiceGridButtonHighDensity(
                        icon = Icons.Filled.Bolt,
                        title = "Pulsa",
                        containerColor = Color(0xFFFAF5FF), // purple-50
                        iconColor = Color(0xFF9333EA), // purple-600
                        onClick = onTopUpClick,
                        testTag = "service_pulsa"
                    )
                    ServiceGridButtonHighDensity(
                        icon = Icons.Filled.Receipt,
                        title = "Tagihan",
                        containerColor = Color(0xFFECFEFF), // cyan-50
                        iconColor = Color(0xFF0891B2), // cyan-600
                        onClick = onHistoryClick,
                        testTag = "service_bills"
                    )
                    ServiceGridButtonHighDensity(
                        icon = Icons.Filled.LocalShipping,
                        title = "Kirim",
                        containerColor = Color(0xFFFEF3C7), // amber-50
                        iconColor = Color(0xFFD97706), // amber-600
                        onClick = onTransferClick,
                        testTag = "service_send"
                    )
                    ServiceGridButtonHighDensity(
                        icon = Icons.Filled.GridView,
                        title = "Lainnya",
                        containerColor = Color(0xFFF1F5F9), // slate-100
                        iconColor = Color(0xFF64748B), // slate-500
                        onClick = onFoodClick,
                        testTag = "service_more"
                    )
                }
            }
        }

        // WEST JAVA THEMED ADVERTISING HERO BANNER
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = JabarCardBg),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp) // slightly more compact for high density
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val bannerResId = R.drawable.img_umkm_banner_1783176876902
                        Image(
                            painter = painterResource(id = bannerResId),
                            contentDescription = "Promo Jabar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Gradient Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Festival Kuliner Jabar",
                                color = JabarGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Diskon JabarPay s.d. 40% untuk produk lokal Batagor, Seblak & Surabi bakar!",
                                color = Color.White,
                                fontSize = 11.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // RECOMMENDATIONS HEADER
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Khusus UMKM Jawa Barat",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "Lihat Semua",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = JabarGreen,
                    modifier = Modifier.clickable { onFoodClick() }
                )
            }
        }

        items(merchants) { merchant ->
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = JabarCardBg),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9)), // slate-100 equivalent border
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onMerchantSelect(merchant) }
                        .testTag("merchant_card_${merchant.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(JabarGreen.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(merchant.emoji, fontSize = 28.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = merchant.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = merchant.description,
                                fontSize = 11.sp,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = "Star",
                                    tint = JabarGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = merchant.rating.toString(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "•  ${merchant.distance}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "•  Ongkir ${formatRupiah(merchant.deliveryFee)}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WalletCompactButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .testTag(testTag)
    ) {
        Icon(
            icon,
            contentDescription = text,
            tint = JabarGreen,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF334155) // slate-700
        )
    }
}

@Composable
fun ServiceGridButtonHighDensity(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    containerColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(containerColor, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            color = Color(0xFF334155), // slate-700
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
fun WalletActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(JabarGreen.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = text,
                tint = JabarDarkGreen,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.DarkGray
        )
    }
}

@Composable
fun ServiceGridButton(
    emoji: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = JabarCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .width(104.dp)
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(JabarGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ActiveTrackingCard(
    title: String,
    statusText: String,
    progress: Float,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    info: String,
    onDismiss: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = JabarDarkGreen),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = JabarGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(statusText, color = Color.White, fontSize = 12.sp)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Tutup", tint = Color.White.copy(alpha = 0.6f))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                color = JabarGold,
                trackColor = Color.White.copy(alpha = 0.2f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = info,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Light
            )
        }
    }
}

// ==========================================
// SCREEN 2: JABARFOOD SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodListScreen(
    merchants: List<FoodMerchant>,
    onBackClick: () -> Unit,
    onMerchantSelect: (FoodMerchant) -> Unit,
    cartCount: Int,
    onCartClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredMerchants = merchants.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.tags.any { tag -> tag.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jabar Food", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        IconButton(onClick = onCartClick) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = "Keranjang", tint = JabarGreen)
                        }
                        if (cartCount > 0) {
                            Badge(
                                containerColor = JabarGold,
                                contentColor = Color.Black,
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Text(cartCount.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(JabarSlateBg)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari seblak, batagor, cendol Elizabeth...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Cari") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("food_search_input"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = JabarGreen,
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            // Category Chips Row
            val categories = listOf("Semua", "Pedas", "Legendaris", "Minuman", "Traditional")
            var selectedCat by remember { mutableStateOf("Semua") }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                items(categories) { cat ->
                    val isSel = cat == selectedCat
                    FilterChip(
                        selected = isSel,
                        onClick = {
                            selectedCat = cat
                            searchQuery = if (cat == "Semua") "" else cat
                        },
                        label = { Text(cat, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = JabarGreen,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Food Merchant List
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (filteredMerchants.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("UMKM kuliner tidak ditemukan. Coba kata kunci lain!", color = Color.Gray, textAlign = TextAlign.Center)
                        }
                    }
                }

                items(filteredMerchants) { merchant ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = JabarCardBg),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMerchantSelect(merchant) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(JabarGreen.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(merchant.emoji, fontSize = 32.sp)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(merchant.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.DarkGray)
                                Text(merchant.description, fontSize = 12.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Star, "Rating", tint = JabarGold, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(merchant.rating.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("•  ${merchant.distance}", fontSize = 11.sp, color = Color.Gray)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("•  Ongkir ${formatRupiah(merchant.deliveryFee)}", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN 3: MERCHANT DETAIL (LOCAL MENU)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantDetailScreen(
    merchant: FoodMerchant,
    menus: List<FoodMenu>,
    cartItems: List<CartItem>,
    onBackClick: () -> Unit,
    onAddToCart: (FoodMenu) -> Unit,
    onCartClick: () -> Unit
) {
    val totalItemsInCart = cartItems.filter { it.merchantId == merchant.id }.sumOf { it.quantity }
    val totalSubtotal = cartItems.filter { it.merchantId == merchant.id }.sumOf { it.price * it.quantity }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(merchant.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                // Header Banner Information
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(JabarGreen.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(merchant.emoji, fontSize = 32.sp)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(merchant.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Star, null, tint = JabarGold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(merchant.rating.toString(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(merchant.distance, color = Color.Gray, fontSize = 14.sp)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(merchant.description, color = Color.Gray, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            merchant.tags.forEach { tag ->
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(tag, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        "Daftar Menu UMKM",
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // Food Items List
                items(menus) { menu ->
                    Card(
                        shape = RoundedCornerShape(0.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(JabarSlateBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(menu.emoji, fontSize = 28.sp)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(menu.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.DarkGray)
                                Text(menu.description, color = Color.Gray, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(formatRupiah(menu.price), color = JabarGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { onAddToCart(menu) },
                                colors = ButtonDefaults.buttonColors(containerColor = JabarGreen),
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp).testTag("add_item_${menu.id}")
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Tambah", tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Tambah", fontSize = 12.sp)
                            }
                        }
                    }
                    Divider(color = JabarSlateBg)
                }
            }

            // Bottom Cart Banner
            if (totalItemsInCart > 0) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = JabarGreen),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(64.dp)
                        .clickable(onClick = onCartClick)
                        .testTag("floating_cart_bar")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("$totalItemsInCart Item Terpilih", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(formatRupiah(totalSubtotal), color = JabarLightGold, fontSize = 12.sp)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Lihat Keranjang", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Filled.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN 4: FOOD CART (CHECKOUT)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    cartItems: List<CartItem>,
    walletBalance: Double,
    onBackClick: () -> Unit,
    onQtyIncrease: (CartItem) -> Unit,
    onQtyDecrease: (CartItem) -> Unit,
    onClearCart: () -> Unit,
    onCheckout: () -> Unit
) {
    val subtotal = cartItems.sumOf { it.price * it.quantity }
    val deliveryFee = if (cartItems.isNotEmpty()) 10000.0 else 0.0
    val serviceFee = if (cartItems.isNotEmpty()) 2000.0 else 0.0
    val totalBill = subtotal + deliveryFee + serviceFee
    val isBalanceSufficient = walletBalance >= totalBill

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Keranjang JabarFood", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    if (cartItems.isNotEmpty()) {
                        TextButton(onClick = onClearCart) {
                            Text("Bersihkan", color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(JabarSlateBg)
        ) {
            if (cartItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🛒", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Keranjang belanja kosong", fontWeight = FontWeight.Bold, color = Color.DarkGray)
                        Text("Pilih jajanan lokal Jabar favoritmu terlebih dahulu!", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            "Detail Item Kuliner (${cartItems.first().merchantName})",
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray,
                            fontSize = 14.sp
                        )
                    }

                    items(cartItems) { item ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(formatRupiah(item.price), color = JabarGreen, fontSize = 12.sp)
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    IconButton(
                                        onClick = { onQtyDecrease(item) },
                                        modifier = Modifier.size(32.dp).background(JabarSlateBg, CircleShape)
                                    ) {
                                        Icon(Icons.Filled.Remove, "Kurang", modifier = Modifier.size(16.dp))
                                    }
                                    Text(item.quantity.toString(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    IconButton(
                                        onClick = { onQtyIncrease(item) },
                                        modifier = Modifier.size(32.dp).background(JabarSlateBg, CircleShape).testTag("plus_item_${item.foodItemId}")
                                    ) {
                                        Icon(Icons.Filled.Add, "Tambah", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Rincian Pembayaran", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                PaymentRow("Subtotal Makanan", formatRupiah(subtotal))
                                PaymentRow("Ongkos Kirim JabarFood", formatRupiah(deliveryFee))
                                PaymentRow("Biaya Layanan Jabar", formatRupiah(serviceFee))
                                Divider(modifier = Modifier.padding(vertical = 12.dp), color = JabarSlateBg)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Total Pembayaran", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.DarkGray)
                                    Text(formatRupiah(totalBill), fontWeight = FontWeight.Black, fontSize = 16.sp, color = JabarGreen)
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.AccountBalanceWallet, null, tint = JabarGreen)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Metode: Saldo JabarPay", fontSize = 12.sp, color = Color.Gray)
                                        Text(formatRupiah(walletBalance), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                                if (!isBalanceSufficient) {
                                    Text("Saldo Kurang", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                } else {
                                    Text("Cukup", color = JabarGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Checkout Button Section
                Card(
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onCheckout,
                        enabled = isBalanceSufficient,
                        colors = ButtonDefaults.buttonColors(containerColor = JabarGreen, disabledContainerColor = Color.LightGray),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(48.dp)
                            .testTag("checkout_button")
                    ) {
                        Text("Pesan Sekarang & Bayar", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 13.sp)
        Text(value, color = Color.DarkGray, fontSize = 13.sp)
    }
}

// ==========================================
// SCREEN 5: RIDE BOOKING SCREEN (MOTOR/CAR)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideBookingScreen(
    serviceType: String, // MOTOR vs CAR
    walletBalance: Double,
    origin: String,
    destination: String,
    price: Double,
    onOriginChange: (String) -> Unit,
    onDestinationChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onBook: () -> Unit
) {
    val title = if (serviceType == "MOTOR") "Jabar Motor" else "Jabar Mobil"
    val subtitle = if (serviceType == "MOTOR") "Ojek Motor Gesit" else "Taksel Mobil Nyaman"
    val iconEmoji = if (serviceType == "MOTOR") "🏍️" else "🚗"
    val canBook = origin.isNotEmpty() && destination.isNotEmpty() && walletBalance >= price

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(JabarSlateBg)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Mock Map Visualizer
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFFE3F2FD), Color(0xFFC8E6C9))
                                    )
                                )
                        ) {
                            // Draw mock roads and pins
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Peta Lokal Jabar", color = JabarDarkGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(iconEmoji, fontSize = 24.sp)
                                }

                                Column {
                                    if (origin.isNotEmpty()) {
                                        Text("🟢 Penjemputan: $origin", fontSize = 11.sp, color = JabarDarkGreen, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (destination.isNotEmpty()) {
                                        Text("🔴 Pengantaran: $destination", fontSize = 11.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Text(
                                    "Simulasi Rute GPS Aktif Wilayah Jawa Barat",
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // Booking Input Form
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Rencana Perjalanan", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = origin,
                                onValueChange = onOriginChange,
                                label = { Text("Pilih Lokasi Jemput") },
                                placeholder = { Text("Contoh: Gedung Sate, Bandung") },
                                leadingIcon = { Text("🟢", modifier = Modifier.padding(start = 8.dp)) },
                                modifier = Modifier.fillMaxWidth().testTag("ride_origin_input"),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = JabarGreen,
                                    unfocusedBorderColor = Color.LightGray
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = destination,
                                onValueChange = onDestinationChange,
                                label = { Text("Pilih Lokasi Tujuan") },
                                placeholder = { Text("Contoh: Braga Street, Bandung") },
                                leadingIcon = { Text("🔴", modifier = Modifier.padding(start = 8.dp)) },
                                modifier = Modifier.fillMaxWidth().testTag("ride_destination_input"),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = JabarGreen,
                                    unfocusedBorderColor = Color.LightGray
                                )
                            )
                        }
                    }
                }

                // Pricing Summary
                if (price > 0) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(subtitle, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("JabarPay Terintegrasi", fontSize = 11.sp, color = JabarGreen)
                                    }
                                    Text(formatRupiah(price), fontWeight = FontWeight.Black, fontSize = 20.sp, color = JabarGreen)
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Divider(color = JabarSlateBg)
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Saldo JabarPay Anda:", fontSize = 12.sp, color = Color.Gray)
                                    Text(
                                        text = formatRupiah(walletBalance),
                                        fontWeight = FontWeight.Bold,
                                        color = if (walletBalance >= price) JabarGreen else Color.Red
                                    )
                                }
                                if (walletBalance < price) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Saldo Anda tidak mencukupi. Silakan lakukan Top Up terlebih dahulu.", color = Color.Red, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Book Button Section
            Card(
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onBook,
                    enabled = canBook,
                    colors = ButtonDefaults.buttonColors(containerColor = JabarGreen, disabledContainerColor = Color.LightGray),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(48.dp)
                        .testTag("book_ride_submit")
                ) {
                    Text("Pesan Sekarang & Antar", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }
}

// ==========================================
// SCREEN 6: WALLET SCREEN (JABARPAY)
// ==========================================
@Composable
fun WalletScreen(
    walletState: com.example.data.Wallet?,
    transactions: List<WalletTransaction>,
    onTopUpClick: () -> Unit,
    onTransferClick: () -> Unit
) {
    val balance = walletState?.balance ?: 500000.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JabarSlateBg)
    ) {
        // Upper Wallet Banner
        Card(
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = JabarGreen),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Dompet Digital", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                Text("JabarPay", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)

                Spacer(modifier = Modifier.height(16.dp))

                Text("Saldo Anda", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                Text(formatRupiah(balance), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)

                Spacer(modifier = Modifier.height(8.dp))
                Text("Nomor Virtual Account: ${walletState?.phoneNumber ?: "0812-3456-7890"}", color = JabarLightGold, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = onTopUpClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = JabarDarkGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Add, null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Isi Saldo")
                    }

                    Button(
                        onClick = onTransferClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f), contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Send, null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Transfer")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Transactions History Section
        Text(
            "Riwayat Transaksi JabarPay",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.DarkGray,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (transactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Belum ada riwayat transaksi.", color = Color.Gray)
                    }
                }
            }

            items(transactions) { tx ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (tx.type in listOf("TOP_UP", "TRANSFER_IN")) JabarGreen.copy(alpha = 0.1f)
                                        else Color.Red.copy(alpha = 0.1f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (tx.type in listOf("TOP_UP", "TRANSFER_IN")) "⬇️" else "⬆️",
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = when (tx.type) {
                                        "TOP_UP" -> "Isi Saldo JabarPay"
                                        "FOOD_PAYMENT" -> "Bayar JabarFood"
                                        "RIDE_PAYMENT" -> "Bayar Layanan Ride"
                                        "TRANSFER_OUT" -> "Transfer Keluar"
                                        else -> "Transfer Masuk"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = tx.recipientOrSource,
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Text(
                            text = if (tx.type in listOf("TOP_UP", "TRANSFER_IN")) "+ " + formatRupiah(tx.amount)
                            else "- " + formatRupiah(tx.amount),
                            color = if (tx.type in listOf("TOP_UP", "TRANSFER_IN")) JabarGreen else Color.Red,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN 7: TOP UP JABARPAY
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopUpScreen(
    amountText: String,
    onAmountChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onSubmit: (Double) -> Unit
) {
    val amount = amountText.toDoubleOrNull() ?: 0.0
    val quickAmounts = listOf(20000.0, 50000.0, 100000.0, 200000.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Isi Saldo JabarPay", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(JabarSlateBg)
                .padding(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Masukkan Nominal Top Up", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = onAmountChange,
                        placeholder = { Text("Contoh: 100000") },
                        prefix = { Text("Rp ", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("top_up_amount_field"),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JabarGreen,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Pilihan Instan", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickAmounts.forEach { qa ->
                            Button(
                                onClick = { onAmountChange(qa.toInt().toString()) },
                                colors = ButtonDefaults.buttonColors(containerColor = JabarGreen.copy(alpha = 0.1f), contentColor = JabarDarkGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(4.dp)
                            ) {
                                Text(formatRupiah(qa).replace("Rp ", ""), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onSubmit(amount) },
                enabled = amount > 0,
                colors = ButtonDefaults.buttonColors(containerColor = JabarGreen, disabledContainerColor = Color.LightGray),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("top_up_submit")
            ) {
                Text("Isi Saldo Sekarang", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

// ==========================================
// SCREEN 8: TRANSFER JABARPAY
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    amountText: String,
    phoneNumber: String,
    recipientName: String,
    walletBalance: Double,
    onAmountChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onSubmit: (Double, String, String) -> Unit
) {
    val amount = amountText.toDoubleOrNull() ?: 0.0
    val canSubmit = amount > 0 && phoneNumber.isNotEmpty() && recipientName.isNotEmpty() && walletBalance >= amount

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transfer JabarPay", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(JabarSlateBg)
                .padding(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Detail Penerima", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = recipientName,
                        onValueChange = onNameChange,
                        label = { Text("Nama Penerima") },
                        placeholder = { Text("Contoh: Mang Koko") },
                        modifier = Modifier.fillMaxWidth().testTag("transfer_name_field"),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JabarGreen,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = onPhoneChange,
                        label = { Text("Nomor Telepon") },
                        placeholder = { Text("Contoh: 0812-9999-8888") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth().testTag("transfer_phone_field"),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JabarGreen,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = onAmountChange,
                        label = { Text("Nominal Transfer") },
                        placeholder = { Text("Contoh: 50000") },
                        prefix = { Text("Rp ", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("transfer_amount_field"),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JabarGreen,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Saldo JabarPay Anda:", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            text = formatRupiah(walletBalance),
                            fontWeight = FontWeight.Bold,
                            color = if (walletBalance >= amount) JabarGreen else Color.Red
                        )
                    }
                    if (walletBalance < amount && amount > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Saldo Anda tidak mencukupi untuk transfer!", color = Color.Red, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onSubmit(amount, phoneNumber, recipientName) },
                enabled = canSubmit,
                colors = ButtonDefaults.buttonColors(containerColor = JabarGreen, disabledContainerColor = Color.LightGray),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("transfer_submit")
            ) {
                Text("Kirim Transfer Sekarang", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

// ==========================================
// SCREEN 9: ACTIVITY HISTORY SCREEN
// ==========================================
@Composable
fun ActivityHistoryScreen(
    orders: List<com.example.data.FoodOrder>,
    rides: List<com.example.data.RideBooking>
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = JabarFood, 1 = Transportasi

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JabarSlateBg)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = JabarGreen
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("JabarFood", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Transportasi", fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (selectedTab == 0) {
                if (orders.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Belum ada riwayat pesanan kuliner.", color = Color.Gray)
                        }
                    }
                }

                items(orders) { order ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🍲", fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(order.merchantName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Status: ${order.status}", fontSize = 11.sp, color = JabarGreen, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text(
                                    text = formatRupiah(order.totalAmount),
                                    fontWeight = FontWeight.Black,
                                    color = Color.DarkGray,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = JabarSlateBg)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = order.itemsSummary,
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                if (rides.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Belum ada riwayat perjalanan ojek/mobil.", color = Color.Gray)
                        }
                    }
                }

                items(rides) { ride ->
                    val typeEmoji = if (ride.serviceType == "MOTOR") "🏍️" else "🚗"
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(typeEmoji, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(if (ride.serviceType == "MOTOR") "Jabar Motor" else "Jabar Mobil", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Driver: ${ride.driverName}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                                Text(
                                    text = formatRupiah(ride.price),
                                    fontWeight = FontWeight.Black,
                                    color = Color.DarkGray,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = JabarSlateBg)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Dari: ${ride.origin}", fontSize = 11.sp, color = Color.Gray)
                                    Text("Ke: ${ride.destination}", fontSize = 11.sp, color = Color.Gray)
                                }
                                Text(
                                    text = when (ride.status) {
                                        "FINDING_DRIVER" -> "Mencari Driver"
                                        "ON_WAY" -> "Dalam Perjalanan"
                                        else -> "Selesai"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (ride.status == "COMPLETED") JabarGreen else JabarGold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLoginScreen(
    onBackClick: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JabarSlateBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🏢", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "UMKM Jabar Admin Portal",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = JabarGreen,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Silakan masuk untuk mengelola menu dan UMKM",
            fontSize = 13.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        errorMessage = null
                    },
                    label = { Text("Username Admin") },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = JabarGreen) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JabarGreen,
                        focusedLabelColor = JabarGreen
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("admin_username_input")
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    label = { Text("Password Admin") },
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = JabarGreen) },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (isPasswordVisible) "Sembunyikan password" else "Tampilkan password"
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JabarGreen,
                        focusedLabelColor = JabarGreen
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("admin_password_input")
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        if (username == "Abnatan" && password == "Abnatan15041984") {
                            onLoginSuccess()
                        } else {
                            errorMessage = "Username atau password salah!"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("admin_login_submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = JabarGreen)
                ) {
                    Text("Masuk Sebagai Admin", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onBackClick) {
            Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = JabarGreen)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Kembali ke Beranda", color = JabarGreen, fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    merchants: List<FoodMerchant>,
    menus: List<FoodMenu>,
    orders: List<com.example.data.FoodOrder>,
    rides: List<com.example.data.RideBooking>,
    onBackClick: () -> Unit,
    onAddMerchant: (FoodMerchant) -> Unit,
    onUpdateMerchant: (FoodMerchant) -> Unit,
    onDeleteMerchant: (FoodMerchant) -> Unit,
    onAddMenu: (FoodMenu) -> Unit,
    onUpdateMenu: (FoodMenu) -> Unit,
    onDeleteMenu: (FoodMenu) -> Unit,
    onUpdateOrderStatus: (com.example.data.FoodOrder, String) -> Unit,
    onUpdateRideStatus: (com.example.data.RideBooking, String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Merchant, 1: Menu, 2: Aktivitas
    val tabs = listOf("UMKM Merchant", "Menu Makanan", "Aktivitas Jabar")

    // Merchant dialog state
    var showMerchantDialog by remember { mutableStateOf(false) }
    var editingMerchant by remember { mutableStateOf<FoodMerchant?>(null) }
    var merchantName by remember { mutableStateOf("") }
    var merchantDesc by remember { mutableStateOf("") }
    var merchantTags by remember { mutableStateOf("") }
    var merchantEmoji by remember { mutableStateOf("🍲") }
    var merchantDistance by remember { mutableStateOf("1.0 km") }
    var merchantDeliveryFee by remember { mutableStateOf("8000") }

    // Menu dialog state
    var showMenuDialog by remember { mutableStateOf(false) }
    var editingMenu by remember { mutableStateOf<FoodMenu?>(null) }
    var menuMerchantId by remember { mutableStateOf(0) }
    var menuName by remember { mutableStateOf("") }
    var menuDesc by remember { mutableStateOf("") }
    var menuPrice by remember { mutableStateOf("") }
    var menuEmoji by remember { mutableStateOf("🍲") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel Admin Jabar", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = JabarGreen)
            )
        },
        containerColor = JabarSlateBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = JabarGreen
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                        selectedContentColor = JabarGreen,
                        unselectedContentColor = Color.Gray
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    0 -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Button(
                                onClick = {
                                    editingMerchant = null
                                    merchantName = ""
                                    merchantDesc = ""
                                    merchantTags = "Kuliner"
                                    merchantEmoji = "🍲"
                                    merchantDistance = "1.0 km"
                                    merchantDeliveryFee = "8000"
                                    showMerchantDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = JabarGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .testTag("admin_add_merchant_button")
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Tambah Merchant Baru", color = Color.White, fontWeight = FontWeight.Bold)
                            }

                            if (merchants.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Belum ada merchant. Silakan tambahkan.", color = Color.Gray)
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .weight(1f),
                                    contentPadding = PaddingValues(bottom = 16.dp, start = 16.dp, end = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(merchants) { merchant ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            shape = RoundedCornerShape(12.dp),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(50.dp)
                                                        .background(JabarGreen.copy(alpha = 0.1f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(merchant.emoji, fontSize = 26.sp)
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(merchant.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                    Text(merchant.description, color = Color.Gray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                    Text("Ongkir: ${formatRupiah(merchant.deliveryFee)} • ${merchant.distance}", color = Color.Gray, fontSize = 11.sp)
                                                }
                                                Row {
                                                    IconButton(onClick = {
                                                        editingMerchant = merchant
                                                        merchantName = merchant.name
                                                        merchantDesc = merchant.description
                                                        merchantTags = merchant.tagsString
                                                        merchantEmoji = merchant.emoji
                                                        merchantDistance = merchant.distance
                                                        merchantDeliveryFee = merchant.deliveryFee.toInt().toString()
                                                        showMerchantDialog = true
                                                    }) {
                                                        Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = JabarGold)
                                                    }
                                                    IconButton(onClick = { onDeleteMerchant(merchant) }) {
                                                        Icon(Icons.Filled.Delete, contentDescription = "Hapus", tint = Color.Red)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            if (merchants.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Tambahkan merchant terlebih dahulu untuk mengelola menu.", color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
                                }
                            } else {
                                var selectedMerchantFilterIndex by remember { mutableStateOf(0) }
                                if (selectedMerchantFilterIndex >= merchants.size) {
                                    selectedMerchantFilterIndex = 0
                                }
                                val activeMerchant = merchants[selectedMerchantFilterIndex]

                                Text("Pilih Merchant untuk mengelola Menu:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.DarkGray, modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp))
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(merchants.size) { idx ->
                                        val m = merchants[idx]
                                        val isSelected = idx == selectedMerchantFilterIndex
                                        Surface(
                                            modifier = Modifier.clickable { selectedMerchantFilterIndex = idx },
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (isSelected) JabarGreen else Color.White,
                                            border = BorderStroke(1.dp, if (isSelected) JabarGreen else Color.LightGray)
                                        ) {
                                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Text(m.emoji, fontSize = 14.sp)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(m.name, color = if (isSelected) Color.White else Color.DarkGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        editingMenu = null
                                        menuMerchantId = activeMerchant.id
                                        menuName = ""
                                        menuDesc = ""
                                        menuPrice = ""
                                        menuEmoji = "🍲"
                                        showMenuDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = JabarGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                        .testTag("admin_add_menu_button")
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Tambah Menu untuk ${activeMerchant.name}", color = Color.White, fontWeight = FontWeight.Bold)
                                }

                                val merchantMenus = menus.filter { it.merchantId == activeMerchant.id }

                                if (merchantMenus.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                                        Text("Belum ada menu di merchant ini.", color = Color.Gray)
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize().weight(1f),
                                        contentPadding = PaddingValues(bottom = 16.dp, start = 16.dp, end = 16.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        items(merchantMenus) { menu ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier.size(44.dp).background(JabarGreen.copy(alpha = 0.08f), CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(menu.emoji, fontSize = 22.sp)
                                                    }
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(menu.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                        Text(menu.description, color = Color.Gray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                        Text(formatRupiah(menu.price), color = JabarGreen, fontWeight = FontWeight.Black, fontSize = 12.sp)
                                                    }
                                                    Row {
                                                        IconButton(onClick = {
                                                            editingMenu = menu
                                                            menuMerchantId = menu.merchantId
                                                            menuName = menu.name
                                                            menuDesc = menu.description
                                                            menuPrice = menu.price.toInt().toString()
                                                            menuEmoji = menu.emoji
                                                            showMenuDialog = true
                                                        }) {
                                                            Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = JabarGold)
                                                        }
                                                        IconButton(onClick = { onDeleteMenu(menu) }) {
                                                            Icon(Icons.Filled.Delete, contentDescription = "Hapus", tint = Color.Red)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Text("Kelola Semua Transaksi / Pesanan Aktif", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.DarkGray)
                            }

                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = JabarGreen.copy(alpha = 0.05f)),
                                    border = BorderStroke(1.dp, JabarGreen.copy(alpha = 0.2f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Pemesanan Makanan (${orders.size})", fontWeight = FontWeight.Bold, color = JabarGreen, fontSize = 13.sp)
                                    }
                                }
                            }

                            if (orders.isEmpty()) {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                        Text("Belum ada transaksi pemesanan makanan.", color = Color.Gray, fontSize = 12.sp)
                                    }
                                }
                            } else {
                                items(orders) { order ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.White)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Column {
                                                    Text(order.merchantName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                    Text(order.itemsSummary, fontSize = 11.sp, color = Color.Gray)
                                                }
                                                Text(formatRupiah(order.totalAmount), fontWeight = FontWeight.Black, fontSize = 13.sp, color = JabarGreen)
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Status: ${order.status}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (order.status == "DELIVERED") JabarGreen else JabarGold)
                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    if (order.status == "PREPARING") {
                                                        Button(
                                                            onClick = { onUpdateOrderStatus(order, "ON_THE_WAY") },
                                                            colors = ButtonDefaults.buttonColors(containerColor = JabarGold),
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                            modifier = Modifier.height(28.dp)
                                                        ) {
                                                            Text("Kirim", fontSize = 10.sp, color = Color.White)
                                                        }
                                                    }
                                                    if (order.status != "DELIVERED") {
                                                        Button(
                                                            onClick = { onUpdateOrderStatus(order, "DELIVERED") },
                                                            colors = ButtonDefaults.buttonColors(containerColor = JabarGreen),
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                            modifier = Modifier.height(28.dp)
                                                        ) {
                                                            Text("Selesai", fontSize = 10.sp, color = Color.White)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = JabarGold.copy(alpha = 0.05f)),
                                    border = BorderStroke(1.dp, JabarGold.copy(alpha = 0.2f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Pemesanan Ojek / Mobil (${rides.size})", fontWeight = FontWeight.Bold, color = JabarGold, fontSize = 13.sp)
                                    }
                                }
                            }

                            if (rides.isEmpty()) {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                        Text("Belum ada transaksi pemesanan transportasi.", color = Color.Gray, fontSize = 12.sp)
                                    }
                                }
                            } else {
                                items(rides) { ride ->
                                    val icon = if (ride.serviceType == "MOTOR") "🏍️" else "🚗"
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.White)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(icon, fontSize = 20.sp)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text("${ride.origin} ke ${ride.destination}", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                        Text("Driver: ${ride.driverName} (${ride.driverVehicle})", fontSize = 11.sp, color = Color.Gray)
                                                    }
                                                }
                                                Text(formatRupiah(ride.price), fontWeight = FontWeight.Black, fontSize = 13.sp, color = Color.DarkGray)
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Status: ${ride.status}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (ride.status == "COMPLETED") JabarGreen else JabarGold)
                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    if (ride.status == "FINDING_DRIVER") {
                                                        Button(
                                                            onClick = { onUpdateRideStatus(ride, "ON_WAY") },
                                                            colors = ButtonDefaults.buttonColors(containerColor = JabarGold),
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                            modifier = Modifier.height(28.dp)
                                                        ) {
                                                            Text("Jalan", fontSize = 10.sp, color = Color.White)
                                                        }
                                                    }
                                                    if (ride.status != "COMPLETED") {
                                                        Button(
                                                            onClick = { onUpdateRideStatus(ride, "COMPLETED") },
                                                            colors = ButtonDefaults.buttonColors(containerColor = JabarGreen),
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                            modifier = Modifier.height(28.dp)
                                                        ) {
                                                            Text("Selesai", fontSize = 10.sp, color = Color.White)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showMerchantDialog) {
        AlertDialog(
            onDismissRequest = { showMerchantDialog = false },
            title = { Text(if (editingMerchant == null) "Tambah Merchant" else "Edit Merchant", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = merchantName,
                        onValueChange = { merchantName = it },
                        label = { Text("Nama Merchant") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = merchantDesc,
                        onValueChange = { merchantDesc = it },
                        label = { Text("Deskripsi") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = merchantTags,
                        onValueChange = { merchantTags = it },
                        label = { Text("Tags (koma pemisah, misal: Pedas, Kuah)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = merchantEmoji,
                            onValueChange = { merchantEmoji = it },
                            label = { Text("Emoji") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = merchantDistance,
                            onValueChange = { merchantDistance = it },
                            label = { Text("Jarak") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = merchantDeliveryFee,
                        onValueChange = { merchantDeliveryFee = it },
                        label = { Text("Ongkir (IDR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val fee = merchantDeliveryFee.toDoubleOrNull() ?: 8000.0
                        if (editingMerchant == null) {
                            onAddMerchant(
                                FoodMerchant(
                                    name = merchantName,
                                    description = merchantDesc,
                                    tagsString = merchantTags,
                                    emoji = merchantEmoji,
                                    distance = merchantDistance,
                                    deliveryFee = fee
                                )
                            )
                        } else {
                            onUpdateMerchant(
                                editingMerchant!!.copy(
                                    name = merchantName,
                                    description = merchantDesc,
                                    tagsString = merchantTags,
                                    emoji = merchantEmoji,
                                    distance = merchantDistance,
                                    deliveryFee = fee
                                )
                            )
                        }
                        showMerchantDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JabarGreen)
                ) {
                    Text("Simpan", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMerchantDialog = false }) {
                    Text("Batal", color = Color.Gray)
                }
            },
            containerColor = Color.White
        )
    }

    if (showMenuDialog) {
        AlertDialog(
            onDismissRequest = { showMenuDialog = false },
            title = { Text(if (editingMenu == null) "Tambah Menu" else "Edit Menu", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = menuName,
                        onValueChange = { menuName = it },
                        label = { Text("Nama Makanan/Minuman") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = menuDesc,
                        onValueChange = { menuDesc = it },
                        label = { Text("Deskripsi Detail") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = menuPrice,
                        onValueChange = { menuPrice = it },
                        label = { Text("Harga (IDR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = menuEmoji,
                        onValueChange = { menuEmoji = it },
                        label = { Text("Emoji") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val price = menuPrice.toDoubleOrNull() ?: 15000.0
                        if (editingMenu == null) {
                            onAddMenu(
                                FoodMenu(
                                    merchantId = menuMerchantId,
                                    name = menuName,
                                    description = menuDesc,
                                    price = price,
                                    emoji = menuEmoji
                                )
                            )
                        } else {
                            onUpdateMenu(
                                editingMenu!!.copy(
                                    name = menuName,
                                    description = menuDesc,
                                    price = price,
                                    emoji = menuEmoji
                                )
                            )
                        }
                        showMenuDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JabarGreen)
                ) {
                    Text("Simpan", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMenuDialog = false }) {
                    Text("Batal", color = Color.Gray)
                }
            },
            containerColor = Color.White
        )
    }
}
