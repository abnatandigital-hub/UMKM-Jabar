package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Wallet::class,
        WalletTransaction::class,
        CartItem::class,
        FoodOrder::class,
        RideBooking::class,
        FoodMerchant::class,
        FoodMenu::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun walletDao(): WalletDao
    abstract fun walletTransactionDao(): WalletTransactionDao
    abstract fun cartDao(): CartDao
    abstract fun foodOrderDao(): FoodOrderDao
    abstract fun rideBookingDao(): RideBookingDao
    abstract fun foodMerchantDao(): FoodMerchantDao
    abstract fun foodMenuDao(): FoodMenuDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "umkm_jabar_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
