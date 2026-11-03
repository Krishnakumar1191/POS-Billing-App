package com.example.posapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersHistoryScreen(viewModel: PosViewModel, navController: NavController) {
    val orders by viewModel.orders.collectAsState(initial = emptyList())
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
                    title = { Text("Order History", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.onBackground) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                    modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                        }
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
                        currentRoute = Screen.OrdersHistory.route,
                        onMenuClick = { showMenuGrid = true }
                    )
                }
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { padding ->
            if (orders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No orders found", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(orders) { order ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.05f)),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(48.dp).background(Color(0xFFE8F5E9), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ShoppingCart,
                                            contentDescription = null,
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Order #${order.orderNumber}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(order.timestamp),
                                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "₹ ${"%.2f".format(order.totalAmount)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = ThemeMaroonPrimary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Surface(color = Color(0xFF4CAF50).copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                                            Text(
                                                text = "Completed",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF2E7D32),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                fontWeight = FontWeight.Bold
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
    }
}