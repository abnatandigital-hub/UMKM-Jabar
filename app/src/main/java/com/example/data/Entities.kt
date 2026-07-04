package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallet")
data class Wallet(
    @PrimaryKey val id: Int = 1,
    val balance: Double = 500000.0, // default IDR 500.000,00
    val pin: String = "123456",
    val phoneNumber: String = "0812-3456-7890"
)

@Entity(tableName = "wallet_transactions")
data class WalletTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "TOP_UP", "FOOD_PAYMENT", "RIDE_PAYMENT", "TRANSFER_OUT", "TRANSFER_IN"
    val amount: Double,
    val recipientOrSource: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey val foodItemId: Int,
    val name: String,
    val price: Double,
    val quantity: Int,
    val merchantId: Int,
    val merchantName: String
)

@Entity(tableName = "food_orders")
data class FoodOrder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val merchantName: String,
    val itemsSummary: String,
    val totalAmount: Double,
    val status: String, // "PREPARING", "ON_THE_WAY", "DELIVERED"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "ride_bookings")
data class RideBooking(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val serviceType: String, // "MOTOR", "CAR"
    val origin: String,
    val destination: String,
    val price: Double,
    val driverName: String,
    val driverPhone: String,
    val driverVehicle: String,
    val status: String, // "FINDING_DRIVER", "ON_WAY", "COMPLETED"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "merchants")
data class FoodMerchant(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val rating: Double = 4.5,
    val distance: String = "1.0 km",
    val deliveryFee: Double = 8000.0,
    val description: String,
    val tagsString: String = "Kuliner",
    val emoji: String = "🍲"
) {
    val tags: List<String>
        get() = tagsString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}

@Entity(tableName = "menus")
data class FoodMenu(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val merchantId: Int,
    val name: String,
    val description: String,
    val price: Double,
    val emoji: String = "🍲"
)
