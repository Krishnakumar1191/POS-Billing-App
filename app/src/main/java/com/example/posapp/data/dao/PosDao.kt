package com.example.posapp.data.dao

import androidx.room.*
import com.example.posapp.data.entities.Category
import com.example.posapp.data.entities.Order
import com.example.posapp.data.entities.OrderLine
import com.example.posapp.data.entities.Product
import com.example.posapp.data.entities.PurchaseOrder
import com.example.posapp.data.entities.CashTransaction
import com.example.posapp.data.entities.Customer
import com.example.posapp.data.entities.StockLog
import kotlinx.coroutines.flow.Flow

@Dao
interface PosDao {
    // --- Products ---
    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: Long)

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): Product?

    @Query("UPDATE products SET stock = stock - :quantity WHERE id = :productId")
    suspend fun reduceProductStock(productId: Long, quantity: Double)

    @Query("UPDATE products SET stock = :newStock WHERE id = :productId")
    suspend fun setProductStock(productId: Long, newStock: Double)

    // --- Categories ---
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Update
    suspend fun updateCategory(category: Category)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategory(id: Long)

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryById(id: Long): Category?

    // --- Customers ---
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    // --- Orders ---
    @Insert
    suspend fun insertOrder(order: Order): Long

    @Insert
    suspend fun insertOrderLines(orderLines: List<OrderLine>)

    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<Order>>
    
    @Query("SELECT * FROM purchase_orders ORDER BY timestamp DESC")
    fun getAllPurchaseOrders(): Flow<List<PurchaseOrder>>

    @Query("SELECT * FROM order_lines WHERE orderId = :orderId")
    suspend fun getOrderLinesForOrder(orderId: Long): List<OrderLine>

    // --- Stock Logs ---
    @Query("SELECT * FROM stock_logs WHERE productId = :productId ORDER BY timestamp DESC")
    fun getLogsForProduct(productId: Long): Flow<List<StockLog>>

    @Insert
    suspend fun insertStockLog(log: StockLog): Long

    @Transaction
    suspend fun createOrderWithLines(order: Order, lines: List<OrderLine>, products: List<Product>) {
        val orderId = insertOrder(order)
        val linesWithOrderId = lines.map { it.copy(orderId = orderId) }
        insertOrderLines(linesWithOrderId)
        
        // Update stock only for storable products (type == "product")
        for (line in lines) {
            val product = products.find { it.id == line.productId }
            if (product != null && product.type == "product") {
                reduceProductStock(line.productId, line.quantity)
                insertStockLog(
                    StockLog(
                        productId = product.id,
                        changeAmount = -line.quantity,
                        type = "SALE",
                        reason = "Sold in Order #${order.orderNumber}",
                        timestamp = order.timestamp
                    )
                )
            }
        }
    }

    // --- Cash Management ---
    @Query("SELECT * FROM cash_transactions ORDER BY timestamp DESC")
    fun getAllCashTransactions(): Flow<List<CashTransaction>>

    @Insert
    suspend fun insertCashTransaction(transaction: CashTransaction): Long
    
    @Query("DELETE FROM cash_transactions WHERE id = :id")
    suspend fun deleteCashTransaction(id: Long)
}