package com.example.posapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.posapp.ui.PosViewModel
import com.example.posapp.ui.components.AppBackground
import com.example.posapp.ui.components.MenuGridBottomSheet
import com.example.posapp.ui.components.SharedBottomBar
import com.example.posapp.ui.navigation.Screen
import com.example.posapp.ui.theme.ThemeMaroonPrimary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: PosViewModel, navController: NavController) {
    val overviewStats by viewModel.overviewStats.collectAsState()
    val aggregateMetrics by viewModel.aggregateMetrics.collectAsState()
    val lowStockProducts by viewModel.lowStockProducts.collectAsState()
    val lowStockTotalCount by viewModel.lowStockTotalCount.collectAsState()
    val weeklyChartData by viewModel.weeklyChartData.collectAsState()
    
    val orders by viewModel.orders.collectAsState(emptyList())
    val cashTransactions by viewModel.cashTransactions.collectAsState(emptyList())

    var recentSegment by remember { mutableStateOf("sale") }
    var showMenuGrid by remember { mutableStateOf(false) }

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
                        Text("Business Analytics", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = MaterialTheme.colorScheme.onBackground) 
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.background
                    ),
                    actions = {
                        IconButton(onClick = { navController.navigate(Screen.Reports.route) }) {
                            Icon(Icons.Default.BarChart, contentDescription = "Reports", tint = ThemeMaroonPrimary)
                        }
//                        IconButton(onClick = { /* Help */ }) {
//                            Icon(Icons.Default.Info, contentDescription = "Help", tint = MaterialTheme.colorScheme.onSurfaceVariant)
//                        }
//                        IconButton(onClick = { /* Sync */ }) {
//                            Icon(Icons.Default.Refresh, contentDescription = "Sync", tint = MaterialTheme.colorScheme.onSurfaceVariant)
//                        }
                    }
                )
            },
            bottomBar = {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SharedBottomBar(
                        navController = navController,
                        currentRoute = Screen.Dashboard.route,
                        onMenuClick = { showMenuGrid = true }
                    )
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(
                    start = 16.dp, 
                    end = 16.dp, 
                    top = 16.dp, 
                    bottom = 16.dp
                ), 
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Detailed Reports Banner Card
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate(Screen.Reports.route) }
                            .shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(20.dp),
                        color = ThemeMaroonPrimary
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.BarChart, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Detailed Reports Hub", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Sale, Expense & Stock Summary Reports", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
                        }
                    }
                }

                // Quick Overview Card
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth().shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Quick Overview - Today", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.Star, contentDescription = null, tint = ThemeMaroonPrimary)
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                StatItem(label = "Sales", value = "${overviewStats.saleToday}", color = Color(0xFF4CAF50))
                                StatItem(label = "Revenue", value = "₹ ${"%.2f".format(overviewStats.moneyInToday)}", color = MaterialTheme.colorScheme.onBackground)
                                StatItem(label = "Cash In", value = "₹ ${"%.2f".format(overviewStats.cashInToday)}", color = Color(0xFFFF9800))
                                StatItem(label = "Expense", value = "₹ ${"%.2f".format(overviewStats.expenseToday)}", color = Color(0xFFF44336))
                            }
                        }
                    }
                }

                // Weekly Transactions Chart (Updated to match sample report bar chart style with grid lines)
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth().shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Weekly Transactions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.weight(1f))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF4CAF50), CircleShape))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sale", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Box(modifier = Modifier.size(8.dp).background(Color(0xFFF44336), CircleShape))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Expense", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))

                            if (weeklyChartData.isEmpty()) {
                                 Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                                     Text("No data available yet", color = Color.Gray)
                                 }
                            } else {
                                val maxAmount = weeklyChartData.maxOfOrNull { maxOf(it.saleAmount, it.expenseAmount) } ?: 0.0
                                val bufferedMax = if (maxAmount > 0) maxAmount * 1.1 else 100.0 

                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(150.dp)
                                    ) {
                                        // Professional Report Grid Lines
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val canvasWidth = size.width
                                            val canvasHeight = size.height
                                            val stepY = canvasHeight / 4f

                                            for (i in 0..4) {
                                                val y = stepY * i
                                                drawLine(
                                                    color = Color.LightGray.copy(alpha = 0.4f),
                                                    start = Offset(0f, y),
                                                    end = Offset(canvasWidth, y),
                                                    strokeWidth = 1.dp.toPx()
                                                )
                                            }
                                        }

                                        // Solid Report Bars
                                        Row(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Bottom
                                        ) {
                                            weeklyChartData.forEach { data ->
                                                val saleHeightFraction = if (bufferedMax > 0) (data.saleAmount / bufferedMax).toFloat().coerceIn(0f, 1f) else 0f
                                                val expenseHeightFraction = if (bufferedMax > 0) (data.expenseAmount / bufferedMax).toFloat().coerceIn(0f, 1f) else 0f
                                                
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxHeight(),
                                                    verticalAlignment = Alignment.Bottom,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxHeight(),
                                                        verticalAlignment = Alignment.Bottom,
                                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                                    ) {
                                                        // Sale Bar (Green)
                                                        Box(
                                                            modifier = Modifier
                                                                .width(9.dp)
                                                                .fillMaxHeight(if (data.saleAmount > 0) maxOf(saleHeightFraction, 0.04f) else 0f)
                                                                .background(Color(0xFF4CAF50), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                        )
                                                        // Expense Bar (Red)
                                                        Box(
                                                            modifier = Modifier
                                                                .width(9.dp)
                                                                .fillMaxHeight(if (data.expenseAmount > 0) maxOf(expenseHeightFraction, 0.04f) else 0f)
                                                                .background(Color(0xFFF44336), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Weekday Labels Row (Placed cleanly below the chart area so they are never clipped)
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        weeklyChartData.forEach { data ->
                                            Box(
                                                modifier = Modifier.width(30.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = data.dayLabel, 
                                                    style = MaterialTheme.typography.bodySmall, 
                                                    color = Color.Gray, 
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Metrics Grid
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        MetricCard(
                            modifier = Modifier.weight(1f),
                            label = "Monthly Sales",
                            value = "${aggregateMetrics.monthlySaleCount}",
                            icon = Icons.Default.ShoppingCart,
                            iconBgColor = Color(0xFF9C27B0)
                        )
                        MetricCard(
                            modifier = Modifier.weight(1f),
                            label = "Sale Revenue",
                            value = "₹ ${"%.2f".format(aggregateMetrics.monthlySaleRevenue)}",
                            icon = Icons.Default.ThumbUp, 
                            iconBgColor = Color(0xFF2196F3)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        MetricCard(
                            modifier = Modifier.weight(1f),
                            label = "Total Cash In",
                            value = "₹ ${"%.2f".format(aggregateMetrics.monthlyCashInTotal)}",
                            icon = Icons.Default.ShoppingCart, 
                            iconBgColor = Color(0xFFFF9800)
                        )
                        MetricCard(
                            modifier = Modifier.weight(1f),
                            label = "Total Expense",
                            value = "₹ ${"%.2f".format(aggregateMetrics.monthlyExpenseTotal)}",
                            icon = Icons.Default.List, 
                            iconBgColor = Color(0xFFE91E63)
                        )
                    }
                }

                // Low Stock Section
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth().shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Text("Low Stock Alert", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.weight(1f))
                                Text("View all", color = ThemeMaroonPrimary, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { })
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFFF3E0), RoundedCornerShape(12.dp))
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF9800))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("${lowStockTotalCount} items below threshold", color = Color(0xFFE65100), fontWeight = FontWeight.Medium)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            if (lowStockProducts.isEmpty()) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("No low stock products right now", color = Color.Gray)
                                }
                            } else {
                                lowStockProducts.forEach { product ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Info, contentDescription = null, tint = Color.LightGray) 
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(product.name, fontWeight = FontWeight.Bold)
                                            Text("Available Qty: ${product.stock}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFFFEBEE), RoundedCornerShape(16.dp))
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("${product.stock}", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Recent Transactions
                item {
                    Text("Recent Transactions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    TabRow(
                        selectedTabIndex = if (recentSegment == "sale") 0 else 1,
                        modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        indicator = { },
                        divider = { }
                    ) {
                        Tab(
                            selected = recentSegment == "sale",
                            onClick = { recentSegment = "sale" },
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (recentSegment == "sale") MaterialTheme.colorScheme.surface else Color.Transparent)
                        ) {
                            Text("Sales", modifier = Modifier.padding(vertical = 12.dp), fontWeight = if (recentSegment == "sale") FontWeight.Bold else FontWeight.Medium)
                        }
                        Tab(
                            selected = recentSegment == "expense",
                            onClick = { recentSegment = "expense" },
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (recentSegment == "expense") MaterialTheme.colorScheme.surface else Color.Transparent)
                        ) {
                            Text("Expenses", modifier = Modifier.padding(vertical = 12.dp), fontWeight = if (recentSegment == "expense") FontWeight.Bold else FontWeight.Medium)
                        }
                    }
                }

                if (recentSegment == "sale") {
                    val displayList = orders.take(5)
                    if (displayList.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("No transactions found", color = Color.Gray)
                            }
                        }
                    } else {
                        items(displayList) { tx ->
                            Surface(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.05f)), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
                                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(48.dp).background(Color(0xFFE8F5E9), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowUp,
                                            contentDescription = null,
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Sale #${tx.orderNumber}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(tx.timestamp),
                                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "₹ ${"%.2f".format(tx.totalAmount)}",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    }
                } else {
                    val displayList = cashTransactions.filter { it.type == "EXPENSE" || it.type == "CASH_OUT" }.take(5)
                    if (displayList.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("No expenses found", color = Color.Gray)
                            }
                        }
                    } else {
                        items(displayList) { tx ->
                            Surface(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.05f)), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
                                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(48.dp).background(Color(0xFFFFEBEE), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = Color(0xFFF44336),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = tx.reason,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(tx.timestamp),
                                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "₹ ${"%.2f".format(tx.amount)}",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = color)
    }
}

@Composable
fun MetricCard(modifier: Modifier = Modifier, label: String, value: String, icon: ImageVector, iconBgColor: Color) {
    Surface(
        modifier = modifier.shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(iconBgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}