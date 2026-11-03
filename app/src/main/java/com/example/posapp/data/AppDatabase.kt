package com.example.posapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.posapp.data.dao.PosDao
import com.example.posapp.data.dao.TaxDao
import com.example.posapp.data.dao.UserDao
import com.example.posapp.data.entities.*

@Database(
    entities = [
        Product::class, 
        Category::class, 
        Order::class, 
        OrderLine::class, 
        User::class, 
        BusinessProfile::class,
        PurchaseOrder::class,
        Tax::class,
        CashTransaction::class,
        Customer::class,
        StockLog::class
    ],
    version = 11, // Bumped to 11 for StockLog table
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun posDao(): PosDao
    abstract fun userDao(): UserDao
    abstract fun taxDao(): TaxDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE products ADD COLUMN imageUri TEXT")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE products ADD COLUMN saleTaxId INTEGER")
                db.execSQL("ALTER TABLE products ADD COLUMN purchaseTaxId INTEGER")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `cash_transactions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `type` TEXT NOT NULL, `amount` REAL NOT NULL, `reason` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `customers` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `phone` TEXT NOT NULL)")
                db.execSQL("ALTER TABLE orders ADD COLUMN customerName TEXT")
                db.execSQL("ALTER TABLE orders ADD COLUMN customerPhone TEXT")
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE categories ADD COLUMN imageUri TEXT")
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `stock_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `productId` INTEGER NOT NULL, `changeAmount` REAL NOT NULL, `type` TEXT NOT NULL, `reason` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pos_database"
                )
                .addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}