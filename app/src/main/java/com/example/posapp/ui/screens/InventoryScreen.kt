package com.example.posapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.posapp.data.entities.Product
import com.example.posapp.data.entities.StockLog
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
fun InventoryScreen(viewModel: PosViewModel, navController: NavController) {
    val products by viewModel.products.collectAsState(initial = emptyList())
    // Filter only storable products (services don't maintain stock)
    val storableProducts = products.filter { it.type == "product" }

    var showMenuGrid by remember { mutableStateOf(false) }
    
    // Adjustment Dialog State
    var productToAdjust by remember { mutableStateOf<Product?>(null) }
    var adjustStockInput by remember { mutableStateOf("") }
    var adjustReasonInput by remember { mutableStateOf("") }

    // History Dialog State
    var productForHistory by remember { mutableStateOf<Product?>(null) }
    val stockLogs by (productForHistory?.let { viewModel.getLogsForProduct(it.id).collectAsState(initial = emptyList()) } ?: remember { mutableStateOf(emptyList()) })

    if (showMenuGrid) {
        MenuGridBottomSheet(
            navController = navController,
            onDismissRequest = { showMenuGrid = false }
        )
    }

    // Stock Adjustment Dialog
    if (productToAdjust != null) {
        AlertDialog(
            onDismissRequest = { productToAdjust = null },
            title = { Text("Adjust Stock: ${productToAdjust?.name}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Current Stock: ${productToAdjust?.stock}", style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = adjustStockInput,
                        onValueChange = { adjustStockInput = it },
                        label = { Text("New Total Stock") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = adjustReasonInput,
                        onValueChange = { adjustReasonInput = it },
                        label = { Text("Reason (e.g., Stock count, Damaged)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newStock = adjustStockInput.toDoubleOrNull()
                        if (newStock != null && productToAdjust != null) {
                            viewModel.adjustStock(
                                productId = productToAdjust!!.id,
                                newStock = newStock,
                                currentStock = productToAdjust!!.stock,
                                reason = adjustReasonInput
                            )
                            productToAdjust = null
                            adjustStockInput = ""
                            adjustReasonInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ThemeMaroonPrimary)
                ) {
                    Text("Confirm Adjustment", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { productToAdjust = null }) {
                    Text("Cancel", color = ThemeMaroonPrimary)
                }
            }
        )
    }

    // Stock History Dialog
    if (productForHistory != null) {
        AlertDialog(
            onDismissRequest = { productForHistory = null },
            title = { Text("History: ${productForHistory?.name}", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                    if (stockLogs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No stock movement history yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(stockLogs) { log ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier.size(36.dp).background(
                                                if (log.changeAmount > 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                                CircleShape
                                            ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (log.changeAmount > 0) Icons.Default.Add else Icons.Default.Remove,
                                                contentDescription = null,
                                                tint = if (log.changeAmount > 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(log.reason, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(log.timestamp),
                                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = if (log.changeAmount > 0) "+${log.changeAmount}" else "${log.changeAmount}",
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (log.changeAmount > 0) Color(0xFF2E7D32) else Color(0xFFC62828),
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { productForHistory = null },
                    colors = ButtonDefaults.buttonColors(containerColor = ThemeMaroonPrimary)
                ) {
                    Text("Close", color = Color.White)
                }
            }
        )
    }

    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Inventory Management", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.onBackground) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                    modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
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
                        currentRoute = Screen.Inventory.route,
                        onMenuClick = { showMenuGrid = true }
                    )
                }
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { padding ->
            if (storableProducts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No storable products found", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Service products do not maintain stock.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
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
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Product Image or Fallback Icon
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (product.imageUri.isNullOrBlank()) {
                                        Icon(Icons.Default.Inventory2, contentDescription = null, tint = ThemeMaroonPrimary)
                                    } else {
                                        AsyncImage(
                                            model = product.imageUri,
                                            contentDescription = "Product Image",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = product.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Current Stock: ${product.stock}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (product.stock < 5.0) Color(0xFFF44336) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (product.stock < 5.0) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                                
                                // History Button
                                IconButton(onClick = { productForHistory = product }) {
                                    Icon(Icons.Default.History, contentDescription = "View History", tint = ThemeMaroonPrimary)
                                }
                                
                                // Adjust Button
                                IconButton(onClick = { 
                                    productToAdjust = product
                                    adjustStockInput = product.stock.toString()
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Adjust Stock", tint = ThemeMaroonPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}