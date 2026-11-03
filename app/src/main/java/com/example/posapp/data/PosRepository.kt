package com.example.posapp.data

import android.content.SharedPreferences
import android.util.Patterns
import androidx.core.content.edit
import com.example.posapp.data.dao.PosDao
import com.example.posapp.data.dao.TaxDao
import com.example.posapp.data.dao.UserDao
import com.example.posapp.data.entities.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class ShopProfile(
    val name: String = "",
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val bankName: String = "",
    val accountNo: String = "",
    val upiId: String = ""
)

data class PrinterConfig(
    val type: String = "BLUETOOTH",
    val address: String = "",
    val width: String = "58" // "58" for mobile printer or "80" for e-POS thermal printer
)

class PosRepository(
    private val posDao: PosDao, 
    private val userDao: UserDao,
    private val taxDao: TaxDao,
    private val sharedPrefs: SharedPreferences
) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val syncScope = CoroutineScope(Dispatchers.IO)

    val allProducts: Flow<List<Product>> = posDao.getAllProducts()
    val allCategories: Flow<List<Category>> = posDao.getAllCategories()
    val allOrders: Flow<List<Order>> = posDao.getAllOrders()
    val allPurchaseOrders: Flow<List<PurchaseOrder>> = posDao.getAllPurchaseOrders()
    val allCashTransactions: Flow<List<CashTransaction>> = posDao.getAllCashTransactions()
    val allTaxes: Flow<List<Tax>> = taxDao.getAllTaxes()
    val allCustomers: Flow<List<Customer>> = posDao.getAllCustomers()

    // --- PRODUCTS ---
    suspend fun insertProduct(product: Product): Long {
        val id = posDao.insertProduct(product)
        if (product.type == "product" && product.stock > 0) {
            posDao.insertStockLog(
                StockLog(
                    productId = id,
                    changeAmount = product.stock,
                    type = "INITIAL",
                    reason = "Initial Stock",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        syncScope.launch {
            try {
                firestore.collection("products").document(id.toString()).set(product.copy(id = id))
            } catch (e: Exception) { e.printStackTrace() }
        }
        return id
    }

    suspend fun updateProduct(product: Product) {
        posDao.updateProduct(product)
        syncScope.launch {
            try {
                firestore.collection("products").document(product.id.toString()).set(product)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    suspend fun deleteProduct(id: Long) {
        posDao.deleteProduct(id)
        syncScope.launch {
            try {
                firestore.collection("products").document(id.toString()).delete()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    suspend fun getProductById(id: Long): Product? = posDao.getProductById(id)

    // --- CATEGORIES ---
    suspend fun insertCategory(category: Category) = posDao.insertCategory(category)
    suspend fun updateCategory(category: Category) = posDao.updateCategory(category)
    suspend fun deleteCategory(id: Long) = posDao.deleteCategory(id)
    suspend fun getCategoryById(id: Long): Category? = posDao.getCategoryById(id)

    // --- CUSTOMERS ---
    suspend fun insertCustomer(customer: Customer) = posDao.insertCustomer(customer)

    // --- ORDERS ---
    suspend fun createOrder(order: Order, lines: List<OrderLine>, products: List<Product>) {
        posDao.createOrderWithLines(order, lines, products)
        
        syncScope.launch {
            try {
                val orderMap = hashMapOf(
                    "orderNumber" to order.orderNumber,
                    "timestamp" to order.timestamp,
                    "totalAmount" to order.totalAmount,
                    "customerName" to order.customerName,
                    "customerPhone" to order.customerPhone,
                    "lines" to lines.map {
                        mapOf(
                            "productId" to it.productId,
                            "quantity" to it.quantity,
                            "unitPrice" to it.unitPrice,
                            "lineTotal" to it.lineTotal
                        )
                    }
                )
                firestore.collection("orders").document(order.orderNumber).set(orderMap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- INVENTORY ADJUSTMENTS ---
    fun getLogsForProduct(productId: Long): Flow<List<StockLog>> = posDao.getLogsForProduct(productId)

    suspend fun adjustStock(productId: Long, newStock: Double, currentStock: Double, reason: String) {
        val diff = newStock - currentStock
        if (diff != 0.0) {
            posDao.setProductStock(productId, newStock)
            posDao.insertStockLog(
                StockLog(
                    productId = productId,
                    changeAmount = diff,
                    type = "ADJUSTMENT",
                    reason = reason.ifBlank { "Inventory Adjustment" },
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    // --- CASH TRANSACTIONS ---
    suspend fun insertCashTransaction(transaction: CashTransaction) = posDao.insertCashTransaction(transaction)
    suspend fun deleteCashTransaction(id: Long) = posDao.deleteCashTransaction(id)

    // --- AUTHENTICATION ---
    suspend fun registerUser(user: User): Long {
        val authResult = auth.createUserWithEmailAndPassword(user.email, user.passwordHash).await()
        val id = userDao.registerUser(user)
        syncScope.launch {
            try {
                val firebaseUserId = authResult.user?.uid ?: UUID.randomUUID().toString()
                val userMap = mapOf(
                    "id" to id, 
                    "name" to user.name, 
                    "email" to user.email, 
                    "mobileNumber" to user.mobileNumber, 
                    "businessName" to user.businessName
                )
                firestore.collection("users").document(firebaseUserId).set(userMap)
            } catch (e: Exception) { e.printStackTrace() }
        }
        return id
    }

    suspend fun loginUser(identifier: String, passwordHash: String): User? {
        val localUser = userDao.loginUser(identifier, passwordHash)
        val loginEmail = localUser?.email ?: if (Patterns.EMAIL_ADDRESS.matcher(identifier).matches()) identifier else throw Exception("Account not found locally.")
        auth.signInWithEmailAndPassword(loginEmail, passwordHash).await()
        return localUser
    }

    suspend fun getUserById(id: Long): User? = userDao.getUserById(id)
    suspend fun insertBusinessProfile(profile: BusinessProfile): Long = userDao.insertBusinessProfile(profile)

    fun saveLoggedInUser(userId: Long) {
        sharedPrefs.edit { putLong("logged_in_user_id", userId) }
    }

    fun getLoggedInUserId(): Long {
        return sharedPrefs.getLong("logged_in_user_id", -1L)
    }

    fun clearLoggedInUser() {
        sharedPrefs.edit { remove("logged_in_user_id") }
    }

    // Printer Settings
    fun savePrinterConfig(config: PrinterConfig) {
        sharedPrefs.edit { 
            putString("printer_type", config.type)
            putString("printer_address", config.address)
            putString("printer_width", config.width)
        }
    }

    fun getPrinterConfig(): PrinterConfig {
        return PrinterConfig(
            type = sharedPrefs.getString("printer_type", "BLUETOOTH") ?: "BLUETOOTH",
            address = sharedPrefs.getString("printer_address", "") ?: "",
            width = sharedPrefs.getString("printer_width", "58") ?: "58"
        )
    }
    
    // Shop Profile Settings
    fun saveShopProfile(profile: ShopProfile) {
        sharedPrefs.edit {
            putString("shop_name", profile.name)
            putString("shop_address", profile.address)
            putString("shop_phone", profile.phone)
            putString("shop_email", profile.email)
            putString("shop_bank_name", profile.bankName)
            putString("shop_account_no", profile.accountNo)
            putString("shop_upi_id", profile.upiId)
        }
    }

    fun getShopProfile(): ShopProfile {
        return ShopProfile(
            name = sharedPrefs.getString("shop_name", "") ?: "",
            address = sharedPrefs.getString("shop_address", "") ?: "",
            phone = sharedPrefs.getString("shop_phone", "") ?: "",
            email = sharedPrefs.getString("shop_email", "") ?: "",
            bankName = sharedPrefs.getString("shop_bank_name", "") ?: "",
            accountNo = sharedPrefs.getString("shop_account_no", "") ?: "",
            upiId = sharedPrefs.getString("shop_upi_id", "") ?: ""
        )
    }

    suspend fun insertTax(tax: Tax): Long = taxDao.insertTax(tax)
    suspend fun updateTax(tax: Tax) = taxDao.updateTax(tax)
    suspend fun deleteTax(id: Long) = taxDao.deleteTax(id)
    suspend fun getTaxById(id: Long): Tax? = taxDao.getTaxById(id)
}