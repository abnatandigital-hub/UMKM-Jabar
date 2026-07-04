package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class UmkmViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UmkmRepository(application)

    // --- Flows ---
    val walletState: StateFlow<Wallet?> = repository.getWallet()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val transactionsState: StateFlow<List<WalletTransaction>> = repository.getTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartItemsState: StateFlow<List<CartItem>> = repository.getCartItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val foodOrdersState: StateFlow<List<FoodOrder>> = repository.getFoodOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rideBookingsState: StateFlow<List<RideBooking>> = repository.getRideBookings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Dynamic database flows ---
    val merchantsState: StateFlow<List<FoodMerchant>> = repository.getMerchants()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val menusState: StateFlow<List<FoodMenu>> = repository.getMenus()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Admin CRUD Operations ---
    fun addMerchant(merchant: FoodMerchant) {
        viewModelScope.launch {
            repository.addMerchant(merchant)
        }
    }

    fun updateMerchant(merchant: FoodMerchant) {
        viewModelScope.launch {
            repository.updateMerchant(merchant)
        }
    }

    fun deleteMerchant(merchant: FoodMerchant) {
        viewModelScope.launch {
            repository.deleteMerchant(merchant)
        }
    }

    fun addMenu(menu: FoodMenu) {
        viewModelScope.launch {
            repository.addMenu(menu)
        }
    }

    fun updateMenu(menu: FoodMenu) {
        viewModelScope.launch {
            repository.updateMenu(menu)
        }
    }

    fun deleteMenu(menu: FoodMenu) {
        viewModelScope.launch {
            repository.deleteMenu(menu)
        }
    }

    // --- UI Local Inputs ---
    private val _selectedMerchant = MutableStateFlow<FoodMerchant?>(null)
    val selectedMerchant: StateFlow<FoodMerchant?> = _selectedMerchant.asStateFlow()

    private val _activeBooking = MutableStateFlow<RideBooking?>(null)
    val activeBooking: StateFlow<RideBooking?> = _activeBooking.asStateFlow()

    private val _activeOrder = MutableStateFlow<FoodOrder?>(null)
    val activeOrder: StateFlow<FoodOrder?> = _activeOrder.asStateFlow()

    init {
        viewModelScope.launch {
            // Seed base balance if not present
            repository.checkAndSeedWallet()
        }
    }

    fun selectMerchant(merchant: FoodMerchant) {
        _selectedMerchant.value = merchant
    }

    // --- Wallet Actions ---
    fun topUp(amount: Double, source: String) {
        viewModelScope.launch {
            repository.topUpWallet(amount, source)
        }
    }

    fun transfer(amount: Double, phone: String, name: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val success = repository.transferWallet(amount, phone, name)
            if (success) {
                onSuccess()
            } else {
                onError("Saldo tidak mencukupi untuk transfer!")
            }
        }
    }

    // --- Cart Actions ---
    fun addToCart(menu: FoodMenu, quantity: Int = 1) {
        viewModelScope.launch {
            val merchant = merchantsState.value.firstOrNull { it.id == menu.merchantId } ?: return@launch
            repository.addToCart(menu, merchant, quantity)
        }
    }

    fun increaseQty(item: CartItem) {
        viewModelScope.launch {
            repository.updateCartQuantity(item, item.quantity + 1)
        }
    }

    fun decreaseQty(item: CartItem) {
        viewModelScope.launch {
            repository.updateCartQuantity(item, item.quantity - 1)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    // --- Checkout Food (Simulated Progress) ---
    fun checkoutFood(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val order = repository.checkoutFoodOrder()
            if (order != null) {
                _activeOrder.value = order
                onSuccess()
                simulateFoodOrderLifecycle(order)
            } else {
                onError("Saldo tidak cukup atau keranjang kosong!")
            }
        }
    }

    private fun simulateFoodOrderLifecycle(order: FoodOrder) {
        viewModelScope.launch {
            // 1. Preparing
            delay(5000)
            val updated1 = order.copy(status = "ON_THE_WAY")
            _activeOrder.value = updated1
            repository.updateOrderStatus(updated1, "ON_THE_WAY")

            // 2. Delivering
            delay(8000)
            val updated2 = order.copy(status = "DELIVERED")
            _activeOrder.value = null // clear current active banner/card
            repository.updateOrderStatus(updated2, "DELIVERED")
        }
    }

    // --- Book Ride (Simulated Match & Trip) ---
    fun bookRide(serviceType: String, origin: String, destination: String, price: Double, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val booking = repository.createRideBooking(serviceType, origin, destination, price)
            if (booking != null) {
                _activeBooking.value = booking
                onSuccess()
                simulateRideLifecycle(booking)
            } else {
                onError("Saldo dompet JabarPay Anda tidak mencukupi!")
            }
        }
    }

    private fun simulateRideLifecycle(booking: RideBooking) {
        viewModelScope.launch {
            // 1. Match Driver
            delay(4000)
            val updated1 = booking.copy(status = "ON_WAY")
            _activeBooking.value = updated1
            repository.updateRideStatus(updated1, "ON_WAY")

            // 2. End Trip
            delay(8000)
            val updated2 = booking.copy(status = "COMPLETED")
            _activeBooking.value = null // complete active tracking
            repository.updateRideStatus(updated2, "COMPLETED")
        }
    }

    fun dismissActiveBooking() {
        _activeBooking.value = null
    }

    fun dismissActiveOrder() {
        _activeOrder.value = null
    }

    fun updateOrderStatus(order: FoodOrder, newStatus: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(order, newStatus)
        }
    }

    fun updateRideStatus(booking: RideBooking, newStatus: String) {
        viewModelScope.launch {
            repository.updateRideStatus(booking, newStatus)
        }
    }
}
