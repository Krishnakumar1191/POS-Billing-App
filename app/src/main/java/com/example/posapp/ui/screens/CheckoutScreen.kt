package com.example.posapp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.posapp.data.entities.Customer
import com.example.posapp.ui.PosViewModel
import com.example.posapp.ui.components.AppBackground
import com.example.posapp.ui.navigation.Screen
import com.example.posapp.ui.theme.ThemeMaroonPrimary
import com.example.posapp.utils.PrinterHelper
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun generateQrCode(text: String, size: Int): Bitmap? {
    return try {
        val bitMatrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(viewModel: PosViewModel, navController: NavController) {
    val total by viewModel.cartTotal.collectAsState()
    val cartItems by viewModel.cart.collectAsState()
    val printerConfig by viewModel.printerConfig.collectAsState()
    val shopProfile by viewModel.shopProfile.collectAsState()
    
    val allCustomers by viewModel.customers.collectAsState()
    
    var selectedPaymentMethod by remember { mutableStateOf("Cash") }
    
    // Customer Selection State
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var customerDropdownExpanded by remember { mutableStateOf(false) }
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val printerHelper = remember { PrinterHelper(context) }

    fun executePrintAndCheckout() {
        val itemsToPrint = cartItems.toList()
        viewModel.checkout(
            customerName = selectedCustomer?.name,
            customerPhone = selectedCustomer?.phone
        ) { order ->
            Toast.makeText(context, "Payment Confirmed! Order #${order.orderNumber}", Toast.LENGTH_LONG).show()
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    printerHelper.printReceipt(
                        order = order, 
                        items = itemsToPrint, 
                        method = selectedPaymentMethod,
                        shopName = shopProfile.name.ifBlank { "BILLING POS" },
                        width = printerConfig.width.toIntOrNull() ?: 58,
                        isWifi = printerConfig.type == "WIFI",
                        address = printerConfig.address 
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions[Manifest.permission.BLUETOOTH_CONNECT] == true
        } else {
            permissions[Manifest.permission.BLUETOOTH] == true
        }
        
        if (granted) {
            executePrintAndCheckout()
        } else {
            Toast.makeText(context, "Bluetooth Permission Denied. Skipping receipt print.", Toast.LENGTH_LONG).show()
            executePrintAndCheckout()
        }
    }

    // Dialog for adding a new customer
    if (showAddCustomerDialog) {
        var newCustName by remember { mutableStateOf("") }
        var newCustPhone by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showAddCustomerDialog = false },
            title = { Text("Add New Customer", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = newCustName,
                        onValueChange = { newCustName = it },
                        label = { Text("Customer Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newCustPhone,
                        onValueChange = { newCustPhone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCustName.isNotBlank()) {
                            viewModel.insertCustomer(newCustName, newCustPhone)
                            showAddCustomerDialog = false
                            Toast.makeText(context, "Customer Added", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ThemeMaroonPrimary)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomerDialog = false }) {
                    Text("Cancel", color = ThemeMaroonPrimary)
                }
            }
        )
    }

    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Payment", fontWeight = FontWeight.ExtraBold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                    modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                
                // Top Row: Amount (Left) and Customer Selection (Right)
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    
                    // Total Box
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Total Amount", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("₹${"%.2f".format(total)}", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.ExtraBold, color = ThemeMaroonPrimary)
                    }

                    // Customer Selection Box
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = ThemeMaroonPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Customer Detail", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            ExposedDropdownMenuBox(
                                expanded = customerDropdownExpanded,
                                onExpandedChange = { customerDropdownExpanded = !customerDropdownExpanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedCustomer?.name ?: "Walk-in Customer",
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ThemeMaroonPrimary,
                                        focusedLabelColor = ThemeMaroonPrimary
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = customerDropdownExpanded,
                                    onDismissRequest = { customerDropdownExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Walk-in Customer (None)") },
                                        onClick = { 
                                            selectedCustomer = null
                                            customerDropdownExpanded = false 
                                        }
                                    )
                                    allCustomers.forEach { cust ->
                                        DropdownMenuItem(
                                            text = { Text("${cust.name} - ${cust.phone}") },
                                            onClick = { 
                                                selectedCustomer = cust
                                                customerDropdownExpanded = false 
                                            }
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Explicit TextButton for creating a new customer
                            TextButton(
                                onClick = { showAddCustomerDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = ThemeMaroonPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Create New Customer", color = ThemeMaroonPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                Text("Select Payment Method", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    PaymentMethodCard(
                        name = "Cash", 
                        isSelected = selectedPaymentMethod == "Cash",
                        onClick = { selectedPaymentMethod = "Cash" },
                        modifier = Modifier.weight(1f)
                    )
                    PaymentMethodCard(
                        name = "Bank", 
                        isSelected = selectedPaymentMethod == "Bank",
                        onClick = { selectedPaymentMethod = "Bank" },
                        modifier = Modifier.weight(1f)
                    )
                    PaymentMethodCard(
                        name = "UPI", 
                        isSelected = selectedPaymentMethod == "UPI",
                        onClick = { selectedPaymentMethod = "UPI" },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (selectedPaymentMethod == "UPI") {
                    if (shopProfile.upiId.isBlank()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        ) {
                            Text(
                                "Please fill the bank details (UPI ID) in the My Profile page to enable QR Code generation.",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 8.dp,
                            modifier = Modifier.size(220.dp).padding(16.dp)
                        ) {
                            val qrBitmap = remember(total, shopProfile.upiId) {
                                val shopNameEncoded = Uri.encode(shopProfile.name.ifBlank { "POS Billing" })
                                val upiString = "upi://pay?pa=${shopProfile.upiId}&pn=${shopNameEncoded}&am=${"%.2f".format(total)}"
                                generateQrCode(upiString, 500)
                            }
                            
                            if (qrBitmap != null) {
                                Image(
                                    bitmap = qrBitmap.asImageBitmap(),
                                    contentDescription = "UPI QR Code",
                                    modifier = Modifier.fillMaxSize().padding(8.dp)
                                )
                            } else {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Failed to generate QR")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Scan with any UPI App", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        if (selectedPaymentMethod == "UPI" && shopProfile.upiId.isBlank()) {
                            Toast.makeText(context, "Please configure UPI ID in Profile first", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (printerConfig.type == "BLUETOOTH") {
                            val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                            } else {
                                ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                            }
                            
                            if (!hasPermission) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    permissionLauncher.launch(arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN))
                                } else {
                                    permissionLauncher.launch(arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN))
                                }
                                return@Button 
                            }
                        }
                        
                        executePrintAndCheckout()
                    },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ThemeMaroonPrimary)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Payment Confirmed", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun PaymentMethodCard(name: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) ThemeMaroonPrimary else MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = if (isSelected) 8.dp else 0.dp
    ) {
        Box(modifier = Modifier.padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}