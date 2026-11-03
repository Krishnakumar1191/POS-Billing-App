package com.example.posapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.posapp.data.ShopProfile
import com.example.posapp.data.entities.CashTransaction
import com.example.posapp.data.entities.Order
import com.example.posapp.data.entities.Product
import com.example.posapp.ui.PosViewModel
import com.example.posapp.ui.components.AppBackground
import com.example.posapp.ui.components.MenuGridBottomSheet
import com.example.posapp.ui.components.SharedBottomBar
import com.example.posapp.ui.navigation.Screen
import com.example.posapp.ui.theme.ThemeMaroonPrimary
import com.example.posapp.utils.ReportExporter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: PosViewModel, navController: NavController) {
    var showMenuGrid by remember { mutableStateOf(false) }
    var selectedReportView by remember { mutableStateOf(0) } // 0 = Hub, 1 = Sale Report, 2 = Expense Report, 3 = Stock Summary
    val shopProfile by viewModel.shopProfile.collectAsState()

    if (showMenuGrid) {
        MenuGridBottomSheet(
            navController = navController,
            onDismissRequest = { showMenuGrid = false }
        )
    }

    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (selectedReportView) {
                                1 -> "Sale Report"
                                2 -> "Expense Report"
                                3 -> "Stock Summary Report"
                                else -> "Reports Hub"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                    modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (selectedReportView != 0) {
                                    selectedReportView = 0
                                } else {
                                    navController.navigateUp()
                                }
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                )
            },
            bottomBar = {
                Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {
                    SharedBottomBar(navController = navController, currentRoute = Screen.Reports.route, onMenuClick = { showMenuGrid = true })
                }
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                when (selectedReportView) {
                    0 -> ReportsHubView(onSelectReport = { selectedReportView = it })
                    1 -> SaleReportView(viewModel = viewModel, shopProfile = shopProfile)
                    2 -> ExpenseReportView(viewModel = viewModel, shopProfile = shopProfile)
                    3 -> StockSummaryReportView(viewModel = viewModel, shopProfile = shopProfile)
                }
            }
        }
    }
}

// --- 1. REPORTS HUB VIEW ---
@Composable
fun ReportsHubView(onSelectReport: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(ThemeMaroonPrimary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = ThemeMaroonPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "TRANSACTION AND STOCK REPORTS",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        ReportNavCard(
            title = "Sale Report",
            subtitle = "Detailed breakdown of sales revenue, order history & tax summary",
            icon = Icons.Default.PointOfSale,
            iconBgColor = Color(0xFF4CAF50),
            onClick = { onSelectReport(1) }
        )

        ReportNavCard(
            title = "Expense Report",
            subtitle = "Detailed breakdown of cash-outs, vendor payments & operational costs",
            icon = Icons.Default.Payments,
            iconBgColor = Color(0xFFF44336),
            onClick = { onSelectReport(2) }
        )

        ReportNavCard(
            title = "Stock Summary Report",
            subtitle = "Overview of inventory quantities, cost valuation & low-stock alerts",
            icon = Icons.Default.Inventory2,
            iconBgColor = Color(0xFF2196F3),
            onClick = { onSelectReport(3) }
        )
    }
}

@Composable
fun ReportNavCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBgColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconBgColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconBgColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// --- 2. SALE REPORT VIEW WITH EXPORT ---
@Composable
fun SaleReportView(viewModel: PosViewModel, shopProfile: ShopProfile) {
    val orders by viewModel.orders.collectAsState(initial = emptyList())
    var selectedRange by remember { mutableStateOf("all") }
    var showExportDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val filteredOrders = remember(orders, selectedRange) {
        when (selectedRange) {
            "today" -> orders.filter { isToday(it.timestamp) }
            "week" -> orders.filter { isThisWeek(it.timestamp) }
            "month" -> orders.filter { isThisMonth(it.timestamp) }
            else -> orders
        }
    }

    val totalSalesCount = filteredOrders.size
    val totalRevenue = filteredOrders.sumOf { it.totalAmount }
    val dateRangeText = remember(selectedRange) {
        val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        when (selectedRange) {
            "today" -> df.format(System.currentTimeMillis())
            "week" -> "This Week"
            "month" -> "This Month"
            else -> "All Time"
        }
    }

    if (showExportDialog) {
        ExportFormatDialog(
            onDismiss = { showExportDialog = false },
            onExportPdf = {
                showExportDialog = false
                ReportExporter.exportSaleReportPdf(context, shopProfile, filteredOrders, dateRangeText)
            },
            onExportExcel = {
                showExportDialog = false
                ReportExporter.exportSaleReportExcel(context, shopProfile, filteredOrders, dateRangeText)
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ReportFilterSegment(
                selected = selectedRange,
                onSelect = { selectedRange = it }
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ReportSummaryBox(
                    modifier = Modifier.weight(1f),
                    label = "Total Sales",
                    value = "$totalSalesCount",
                    color = ThemeMaroonPrimary
                )
                ReportSummaryBox(
                    modifier = Modifier.weight(1f),
                    label = "Total Revenue",
                    value = "₹ ${"%.2f".format(totalRevenue)}",
                    color = Color(0xFF4CAF50)
                )
            }

            Text("Sales Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            if (filteredOrders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No sales records found for selected period", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredOrders) { order ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.05f)),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("#${order.orderNumber}", fontWeight = FontWeight.ExtraBold, color = ThemeMaroonPrimary)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(order.timestamp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                                Spacer(modifier = Modifier.height(8.dp))

                                if (!order.customerName.isNullOrBlank()) {
                                    Row {
                                        Text("Customer: ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${order.customerName} (${order.customerPhone ?: "No phone"})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Grand Total", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text("₹ ${"%.2f".format(order.totalAmount)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4CAF50))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button for Export/Download
        FloatingActionButton(
            onClick = {
                if (filteredOrders.isEmpty()) {
                    Toast.makeText(context, "No data to export", Toast.LENGTH_SHORT).show()
                } else {
                    showExportDialog = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 16.dp),
            containerColor = ThemeMaroonPrimary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Download, contentDescription = "Download Report")
        }
    }
}

// --- 3. EXPENSE REPORT VIEW WITH EXPORT ---
@Composable
fun ExpenseReportView(viewModel: PosViewModel, shopProfile: ShopProfile) {
    val cashTransactions by viewModel.cashTransactions.collectAsState(initial = emptyList())
    var selectedRange by remember { mutableStateOf("all") }
    var showExportDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val expenseList = remember(cashTransactions, selectedRange) {
        val filtered = cashTransactions.filter { it.type == "EXPENSE" || it.type == "CASH_OUT" }
        when (selectedRange) {
            "today" -> filtered.filter { isToday(it.timestamp) }
            "week" -> filtered.filter { isThisWeek(it.timestamp) }
            "month" -> filtered.filter { isThisMonth(it.timestamp) }
            else -> filtered
        }
    }

    val totalCount = expenseList.size
    val totalAmount = expenseList.sumOf { it.amount }
    val dateRangeText = remember(selectedRange) {
        val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        when (selectedRange) {
            "today" -> df.format(System.currentTimeMillis())
            "week" -> "This Week"
            "month" -> "This Month"
            else -> "All Time"
        }
    }

    if (showExportDialog) {
        ExportFormatDialog(
            onDismiss = { showExportDialog = false },
            onExportPdf = {
                showExportDialog = false
                ReportExporter.exportExpenseReportPdf(context, shopProfile, expenseList, dateRangeText)
            },
            onExportExcel = {
                showExportDialog = false
                ReportExporter.exportExpenseReportExcel(context, shopProfile, expenseList, dateRangeText)
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ReportFilterSegment(
                selected = selectedRange,
                onSelect = { selectedRange = it }
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ReportSummaryBox(
                    modifier = Modifier.weight(1f),
                    label = "Total Expense Count",
                    value = "$totalCount",
                    color = ThemeMaroonPrimary
                )
                ReportSummaryBox(
                    modifier = Modifier.weight(1f),
                    label = "Total Expense Amount",
                    value = "₹ ${"%.2f".format(totalAmount)}",
                    color = Color(0xFFF44336)
                )
            }

            Text("Expense Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            if (expenseList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No expense records found for selected period", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(expenseList) { tx ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.05f)),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(tx.reason, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(tx.timestamp),
                                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text("₹ ${"%.2f".format(tx.amount)}", fontWeight = FontWeight.ExtraBold, color = Color(0xFFF44336), fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                if (expenseList.isEmpty()) {
                    Toast.makeText(context, "No data to export", Toast.LENGTH_SHORT).show()
                } else {
                    showExportDialog = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 16.dp),
            containerColor = ThemeMaroonPrimary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Download, contentDescription = "Download Report")
        }
    }
}

// --- 4. STOCK SUMMARY REPORT VIEW WITH EXPORT ---
@Composable
fun StockSummaryReportView(viewModel: PosViewModel, shopProfile: ShopProfile) {
    val products by viewModel.products.collectAsState(initial = emptyList())
    val storableProducts = remember(products) { products.filter { it.type == "product" } }
    var showExportDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val totalItems = storableProducts.size
    val totalStockQty = storableProducts.sumOf { it.stock }
    val totalValuation = storableProducts.sumOf { it.stock * it.costPrice }

    if (showExportDialog) {
        ExportFormatDialog(
            onDismiss = { showExportDialog = false },
            onExportPdf = {
                showExportDialog = false
                ReportExporter.exportStockReportPdf(context, shopProfile, storableProducts)
            },
            onExportExcel = {
                showExportDialog = false
                ReportExporter.exportStockReportExcel(context, shopProfile, storableProducts)
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ReportSummaryBox(
                    modifier = Modifier.weight(1f),
                    label = "Total Products",
                    value = "$totalItems",
                    color = ThemeMaroonPrimary
                )
                ReportSummaryBox(
                    modifier = Modifier.weight(1f),
                    label = "Total Stock Qty",
                    value = "%.0f".format(totalStockQty),
                    color = Color(0xFF2196F3)
                )
                ReportSummaryBox(
                    modifier = Modifier.weight(1f),
                    label = "Stock Valuation",
                    value = "₹ ${"%.0f".format(totalValuation)}",
                    color = Color(0xFF4CAF50)
                )
            }

            Text("Stock Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            if (storableProducts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No storable products in inventory", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(storableProducts) { product ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.05f)),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(product.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Cost: ₹${"%.2f".format(product.costPrice)} | Price: ₹${"%.2f".format(product.price)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Qty: ${product.stock}", fontWeight = FontWeight.Bold, color = if (product.stock < 5) Color(0xFFF44336) else MaterialTheme.colorScheme.onBackground)
                                    Text("Val: ₹${"%.2f".format(product.stock * product.costPrice)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                if (storableProducts.isEmpty()) {
                    Toast.makeText(context, "No data to export", Toast.LENGTH_SHORT).show()
                } else {
                    showExportDialog = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 16.dp),
            containerColor = ThemeMaroonPrimary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Download, contentDescription = "Download Report")
        }
    }
}

// --- EXPORT DIALOG ---
@Composable
fun ExportFormatDialog(
    onDismiss: () -> Unit,
    onExportPdf: () -> Unit,
    onExportExcel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Download Report", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Select your preferred download format:", style = MaterialTheme.typography.bodyMedium)
                
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onExportPdf),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = ThemeMaroonPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("PDF Document (.pdf)", fontWeight = FontWeight.Bold)
                            Text("Formatted invoice style layout with header", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onExportExcel),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Excel Spreadsheet (.csv)", fontWeight = FontWeight.Bold)
                            Text("Data table compatible with Excel & Sheets", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = ThemeMaroonPrimary)
            }
        }
    )
}

// --- HELPER COMPONENTS ---
@Composable
fun ReportFilterSegment(selected: String, onSelect: (String) -> Unit) {
    TabRow(
        selectedTabIndex = when (selected) {
            "today" -> 0
            "week" -> 1
            "month" -> 2
            else -> 3
        },
        modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        indicator = { },
        divider = { }
    ) {
        listOf("today" to "Today", "week" to "Week", "month" to "Month", "all" to "All").forEach { pair ->
            Tab(
                selected = selected == pair.first,
                onClick = { onSelect(pair.first) },
                modifier = Modifier
                    .padding(4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selected == pair.first) MaterialTheme.colorScheme.surface else Color.Transparent)
            ) {
                Text(pair.second, modifier = Modifier.padding(vertical = 10.dp), fontWeight = if (selected == pair.first) FontWeight.Bold else FontWeight.Medium, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun ReportSummaryBox(modifier: Modifier = Modifier, label: String, value: String, color: Color) {
    Surface(
        modifier = modifier.shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = color, textAlign = TextAlign.Center)
        }
    }
}

private fun isToday(timestamp: Long): Boolean {
    val calNow = Calendar.getInstance()
    val calTs = Calendar.getInstance().apply { timeInMillis = timestamp }
    return calNow.get(Calendar.YEAR) == calTs.get(Calendar.YEAR) &&
           calNow.get(Calendar.DAY_OF_YEAR) == calTs.get(Calendar.DAY_OF_YEAR)
}

private fun isThisWeek(timestamp: Long): Boolean {
    val calNow = Calendar.getInstance()
    val calTs = Calendar.getInstance().apply { timeInMillis = timestamp }
    return calNow.get(Calendar.YEAR) == calTs.get(Calendar.YEAR) &&
           calNow.get(Calendar.WEEK_OF_YEAR) == calTs.get(Calendar.WEEK_OF_YEAR)
}

private fun isThisMonth(timestamp: Long): Boolean {
    val calNow = Calendar.getInstance()
    val calTs = Calendar.getInstance().apply { timeInMillis = timestamp }
    return calNow.get(Calendar.YEAR) == calTs.get(Calendar.YEAR) &&
           calNow.get(Calendar.MONTH) == calTs.get(Calendar.MONTH)
}