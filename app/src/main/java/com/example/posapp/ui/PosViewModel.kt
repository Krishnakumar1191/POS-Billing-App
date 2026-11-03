package com.example.posapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.posapp.data.PosRepository
import com.example.posapp.data.PrinterConfig
import com.example.posapp.data.ShopProfile
import com.example.posapp.data.entities.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

data class OverviewStats(
    val saleToday: Int = 0,
    val cashInToday: Double = 0.0,
    val moneyInToday: Double = 0.0,
    val expenseToday: Double = 0.0
)

data class AggregateMetrics(
    val monthlySaleCount: Int = 0,
    val monthlySaleRevenue: Double = 0.0,
    val monthlyCashInTotal: Double = 0.0,
    val monthlyExpenseTotal: Double = 0.0
)

data class DailyChartData(
    val dayLabel: String,
    val saleAmount: Double,
    val expenseAmount: Double
)

class PosViewModel(private val repository: PosRepository) : ViewModel() {
    
    val products = repository.allProducts.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val categories = repository.allCategories.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val orders = repository.allOrders.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val cashTransactions = repository.allCashTransactions.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val taxes = repository.allTaxes.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val customers = repository.allCustomers.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    private val _cart = MutableStateFlow<List<OrderLine>>(emptyList())
    val cart: StateFlow<List<OrderLine>> = _cart.asStateFlow()
    
    private val _cartSubtotal = MutableStateFlow(0.0)
    val cartSubtotal: StateFlow<Double> = _cartSubtotal.asStateFlow()

    private val _cartTaxTotal = MutableStateFlow(0.0)
    val cartTaxTotal: StateFlow<Double> = _cartTaxTotal.asStateFlow()

    private val _cartTotal = MutableStateFlow(0.0)
    val cartTotal: StateFlow<Double> = _cartTotal.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _printerConfig = MutableStateFlow(repository.getPrinterConfig())
    val printerConfig: StateFlow<PrinterConfig> = _printerConfig.asStateFlow()

    private val _shopProfile = MutableStateFlow(repository.getShopProfile())
    val shopProfile: StateFlow<ShopProfile> = _shopProfile.asStateFlow()

    fun savePrinterConfig(config: PrinterConfig) {
        repository.savePrinterConfig(config)
        _printerConfig.value = config
    }

    fun saveShopProfile(profile: ShopProfile) {
        repository.saveShopProfile(profile)
        _shopProfile.value = profile
    }

    private fun isToday(timestamp: Long): Boolean {
        val calendarNow = Calendar.getInstance()
        val calendarTs = Calendar.getInstance().apply { timeInMillis = timestamp }
        return calendarNow.get(Calendar.YEAR) == calendarTs.get(Calendar.YEAR) &&
               calendarNow.get(Calendar.DAY_OF_YEAR) == calendarTs.get(Calendar.DAY_OF_YEAR)
    }
    
    private fun isThisMonth(timestamp: Long): Boolean {
        val calendarNow = Calendar.getInstance()
        val calendarTs = Calendar.getInstance().apply { timeInMillis = timestamp }
        return calendarNow.get(Calendar.YEAR) == calendarTs.get(Calendar.YEAR) &&
               calendarNow.get(Calendar.MONTH) == calendarTs.get(Calendar.MONTH)
    }

    val overviewStats = combine(orders, cashTransactions) { sales, transactions ->
        val todaySales = sales.filter { isToday(it.timestamp) }
        val todayTransactions = transactions.filter { isToday(it.timestamp) }
        
        OverviewStats(
            saleToday = todaySales.size,
            moneyInToday = todaySales.sumOf { it.totalAmount },
            cashInToday = todayTransactions.filter { it.type == "CASH_IN" }.sumOf { it.amount },
            expenseToday = todayTransactions.filter { it.type == "EXPENSE" || it.type == "CASH_OUT" }.sumOf { it.amount }
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, OverviewStats())

    val aggregateMetrics = combine(orders, cashTransactions) { sales, transactions ->
        val monthSales = sales.filter { isThisMonth(it.timestamp) }
        val monthTransactions = transactions.filter { isThisMonth(it.timestamp) }
        
        AggregateMetrics(
            monthlySaleCount = monthSales.size,
            monthlySaleRevenue = monthSales.sumOf { it.totalAmount },
            monthlyCashInTotal = monthTransactions.filter { it.type == "CASH_IN" }.sumOf { it.amount },
            monthlyExpenseTotal = monthTransactions.filter { it.type == "EXPENSE" || it.type == "CASH_OUT" }.sumOf { it.amount }
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, AggregateMetrics())
    
    val weeklyChartData = combine(orders, cashTransactions) { sales, transactions ->
        val chartData = mutableListOf<DailyChartData>()
        val dayLabels = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        
        for (i in 6 downTo 0) {
            val iterCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
            }
            
            val dayStart = iterCal.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            
            val dayEnd = iterCal.apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis
            
            val daySales = sales.filter { it.timestamp in dayStart..dayEnd }.sumOf { it.totalAmount }
            val dayExpenses = transactions.filter { it.timestamp in dayStart..dayEnd && (it.type == "EXPENSE" || it.type == "CASH_OUT") }.sumOf { it.amount }
            
            val dayOfWeek = iterCal.get(Calendar.DAY_OF_WEEK) - 1 // 0-based
            val label = dayLabels.getOrElse(dayOfWeek) { "?" }
            
            chartData.add(DailyChartData(label, daySales, dayExpenses))
        }
        chartData
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Only track low stock for storable products (type == "product")
    val lowStockProducts = products.map { list ->
        list.filter { it.type == "product" && it.stock < 5.0 }.take(5)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val lowStockTotalCount = products.map { list ->
        list.count { it.type == "product" && it.stock < 5.0 }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    // --- CART LOGIC ---
    fun addProductToCart(product: Product) {
        val currentCart = _cart.value.toMutableList()
        val existingItemIndex = currentCart.indexOfFirst { it.productId == product.id }
        
        if (existingItemIndex != -1) {
            val item = currentCart[existingItemIndex]
            currentCart[existingItemIndex] = item.copy(
                quantity = item.quantity + 1.0,
                lineTotal = (item.quantity + 1.0) * item.unitPrice
            )
        } else {
            currentCart.add(
                OrderLine(
                    orderId = 0,
                    productId = product.id,
                    quantity = 1.0,
                    unitPrice = product.price,
                    lineTotal = product.price
                )
            )
        }
        updateCartAndCalculateTotals(currentCart)
    }

    fun removeProductFromCart(productId: Long) {
        val currentCart = _cart.value.filter { it.productId != productId }
        updateCartAndCalculateTotals(currentCart)
    }

    fun decrementProductInCart(productId: Long) {
        val currentCart = _cart.value.toMutableList()
        val existingItemIndex = currentCart.indexOfFirst { it.productId == productId }
        if (existingItemIndex != -1) {
            val item = currentCart[existingItemIndex]
            if (item.quantity > 1) {
                currentCart[existingItemIndex] = item.copy(
                    quantity = item.quantity - 1.0,
                    lineTotal = (item.quantity - 1.0) * item.unitPrice
                )
            } else {
                currentCart.removeAt(existingItemIndex)
            }
            updateCartAndCalculateTotals(currentCart)
        }
    }
    
    fun clearCart() {
        updateCartAndCalculateTotals(emptyList())
    }

    private fun updateCartAndCalculateTotals(newCart: List<OrderLine>) {
        _cart.value = newCart
        
        var totalSubtotal = 0.0
        var totalTaxAmt = 0.0
        var totalFinal = 0.0

        newCart.forEach { line ->
            val product = products.value.find { it.id == line.productId }
            val lineGross = line.quantity * line.unitPrice
            var lineBase = lineGross
            var lineTax = 0.0
            
            if (product != null && product.saleTaxId != null) {
                val tax = taxes.value.find { it.id == product.saleTaxId }
                if (tax != null) {
                    if (tax.amountType == "percent") {
                        if (tax.priceInclude) {
                            lineBase = lineGross / (1 + (tax.amount / 100.0))
                            lineTax = lineGross - lineBase
                        } else {
                            lineBase = lineGross
                            lineTax = lineGross * (tax.amount / 100.0)
                        }
                    } else if (tax.amountType == "fixed") {
                        lineTax = tax.amount * line.quantity
                        if (tax.priceInclude) {
                            lineBase = lineGross - lineTax
                        } else {
                            lineBase = lineGross
                        }
                    }
                }
            }
            totalSubtotal += lineBase
            totalTaxAmt += lineTax
            totalFinal += (lineBase + lineTax)
        }

        _cartSubtotal.value = totalSubtotal
        _cartTaxTotal.value = totalTaxAmt
        _cartTotal.value = totalFinal
    }

    fun checkout(customerName: String?, customerPhone: String?, onComplete: (Order) -> Unit) {
        val currentCart = _cart.value
        if (currentCart.isEmpty()) return
        
        viewModelScope.launch {
            val order = Order(
                orderNumber = "ORD-${UUID.randomUUID().toString().take(8).uppercase()}",
                timestamp = System.currentTimeMillis(),
                totalAmount = cartTotal.value,
                customerName = customerName?.takeIf { it.isNotBlank() },
                customerPhone = customerPhone?.takeIf { it.isNotBlank() }
            )
            repository.createOrder(order, currentCart, products.value)
            clearCart()
            onComplete(order)
        }
    }

    // --- CUSTOMERS ---
    fun insertCustomer(name: String, phone: String) {
        viewModelScope.launch {
            repository.insertCustomer(Customer(name = name, phone = phone))
        }
    }

    // --- INVENTORY LOGS & ADJUSTMENTS ---
    fun getLogsForProduct(productId: Long): Flow<List<StockLog>> {
        return repository.getLogsForProduct(productId)
    }

    fun adjustStock(productId: Long, newStock: Double, currentStock: Double, reason: String) {
        viewModelScope.launch {
            repository.adjustStock(productId, newStock, currentStock, reason)
        }
    }

    // --- CASH TRANSACTIONS (Cash In/Out/Expense) ---
    fun insertCashTransaction(type: String, amount: Double, reason: String) {
        viewModelScope.launch {
            val transaction = CashTransaction(
                type = type,
                amount = amount,
                reason = reason,
                timestamp = System.currentTimeMillis()
            )
            repository.insertCashTransaction(transaction)
        }
    }

    fun deleteCashTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteCashTransaction(id)
        }
    }

    // --- PRODUCTS ---
    fun insertProduct(product: Product, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.insertProduct(product)
            onSuccess()
        }
    }

    fun updateProduct(product: Product, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateProduct(product)
            onSuccess()
        }
    }

    fun deleteProduct(id: Long) {
        viewModelScope.launch {
            repository.deleteProduct(id)
        }
    }

    suspend fun getProductById(id: Long): Product? {
        return repository.getProductById(id)
    }

    // --- CATEGORIES ---
    fun insertCategory(category: Category, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.insertCategory(category)
            onSuccess()
        }
    }

    fun updateCategory(category: Category, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateCategory(category)
            onSuccess()
        }
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch {
            repository.deleteCategory(id)
        }
    }

    suspend fun getCategoryById(id: Long): Category? {
        return repository.getCategoryById(id)
    }

    // --- AUTHENTICATION FLOW ---
    fun checkAutoLogin(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val userId = repository.getLoggedInUserId()
            if (userId != -1L) {
                val user = repository.getUserById(userId)
                if (user != null) {
                    _currentUser.value = user
                    onResult(true)
                    return@launch
                }
            }
            onResult(false)
        }
    }

    fun login(identifier: String, passwordHash: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val user = repository.loginUser(identifier, passwordHash)
                if (user != null) {
                    _currentUser.value = user
                    repository.saveLoggedInUser(user.id)
                    onSuccess()
                } else {
                    onError("Invalid credentials")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Authentication Failed")
            }
        }
    }

    fun register(user: User, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val id = repository.registerUser(user)
                if (id > 0) {
                    val initialProfile = ShopProfile(
                        name = user.businessName ?: user.companyName ?: "",
                        address = "", 
                        phone = user.mobileNumber,
                        email = user.email,
                        bankName = "",
                        accountNo = "",
                        upiId = ""
                    )
                    saveShopProfile(initialProfile)
                    onSuccess()
                } else {
                    onError("Registration Failed: Error saving user.")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Firebase Authentication Failed")
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        repository.clearLoggedInUser()
    }

    // --- TAX FLOW ---
    fun insertTax(tax: Tax, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.insertTax(tax)
            onSuccess()
        }
    }

    fun updateTax(tax: Tax, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateTax(tax)
            onSuccess()
        }
    }

    fun deleteTax(id: Long) {
        viewModelScope.launch {
            repository.deleteTax(id)
        }
    }

    suspend fun getTaxById(id: Long): Tax? {
        return repository.getTaxById(id)
    }
}

class PosViewModelFactory(private val repository: PosRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PosViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}