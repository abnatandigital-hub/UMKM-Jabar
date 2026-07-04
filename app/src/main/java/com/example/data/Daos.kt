package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallet WHERE id = 1 LIMIT 1")
    fun getWalletFlow(): Flow<Wallet?>

    @Query("SELECT * FROM wallet WHERE id = 1 LIMIT 1")
    suspend fun getWalletDirect(): Wallet?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWallet(wallet: Wallet)
}

@Dao
interface WalletTransactionDao {
    @Query("SELECT * FROM wallet_transactions ORDER BY timestamp DESC")
    fun getTransactionsFlow(): Flow<List<WalletTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: WalletTransaction)
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun getCartItemsFlow(): Flow<List<CartItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCartItem(item: CartItem)

    @Delete
    suspend fun deleteCartItem(item: CartItem)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}

@Dao
interface FoodOrderDao {
    @Query("SELECT * FROM food_orders ORDER BY timestamp DESC")
    fun getOrdersFlow(): Flow<List<FoodOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: FoodOrder): Long

    @Update
    suspend fun updateOrder(order: FoodOrder)
}

@Dao
interface RideBookingDao {
    @Query("SELECT * FROM ride_bookings ORDER BY timestamp DESC")
    fun getBookingsFlow(): Flow<List<RideBooking>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: RideBooking): Long

    @Update
    suspend fun updateBooking(booking: RideBooking)
}

@Dao
interface FoodMerchantDao {
    @Query("SELECT * FROM merchants ORDER BY id ASC")
    fun getMerchantsFlow(): Flow<List<FoodMerchant>>

    @Query("SELECT COUNT(*) FROM merchants")
    suspend fun getMerchantsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMerchant(merchant: FoodMerchant): Long

    @Update
    suspend fun updateMerchant(merchant: FoodMerchant)

    @Delete
    suspend fun deleteMerchant(merchant: FoodMerchant)
}

@Dao
interface FoodMenuDao {
    @Query("SELECT * FROM menus ORDER BY id ASC")
    fun getMenusFlow(): Flow<List<FoodMenu>>

    @Query("SELECT COUNT(*) FROM menus")
    suspend fun getMenusCount(): Int

    @Query("SELECT * FROM menus WHERE merchantId = :merchantId ORDER BY id ASC")
    fun getMenusByMerchantFlow(merchantId: Int): Flow<List<FoodMenu>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenu(menu: FoodMenu): Long

    @Update
    suspend fun updateMenu(menu: FoodMenu)

    @Delete
    suspend fun deleteMenu(menu: FoodMenu)
}
