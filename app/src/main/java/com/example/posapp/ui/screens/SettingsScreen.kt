package com.example.posapp.ui.screens

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Build
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
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.posapp.MainActivity
import com.example.posapp.data.PrinterConfig
import com.example.posapp.data.ShopProfile
import com.example.posapp.data.entities.Order
import com.example.posapp.data.entities.OrderLine
import com.example.posapp.ui.PosViewModel
import com.example.posapp.ui.components.AppBackground
import com.example.posapp.ui.components.MenuGridBottomSheet
import com.example.posapp.ui.components.SharedBottomBar
import com.example.posapp.ui.navigation.Screen
import com.example.posapp.ui.theme.ThemeMaroonPrimary
import com.example.posapp.utils.DatabaseBackupHelper
import com.example.posapp.utils.PrinterHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: PosViewModel, navController: NavController) {
    var showMenuGrid by remember { mutableStateOf(false) }

    if (showMenuGrid) {
        MenuGridBottomSheet(navController = navController, onDismissRequest = { showMenuGrid = false })
    }

    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Settings", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.onBackground) },
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
                Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {
                    SharedBottomBar(navController = navController, currentRoute = Screen.Settings.route, onMenuClick = { showMenuGrid = true })
                }
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SettingsMenuCard(
                    title = "Printer Configuration",
                    icon = Icons.Default.Settings,
                    onClick = { navController.navigate(Screen.PrinterSettings.route) }
                )
                SettingsMenuCard(
                    title = "My Profile",
                    icon = Icons.Default.Person,
                    onClick = { navController.navigate(Screen.ProfileSettings.route) }
                )
                SettingsMenuCard(
                    title = "Backup & Restore",
                    icon = Icons.Default.Build,
                    onClick = { navController.navigate(Screen.BackupRestore.route) }
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Button(
                    onClick = {
                        viewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true } // Clear entire backstack
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(64.dp).padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("LOGOUT", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun SettingsMenuCard(title: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = ThemeMaroonPrimary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onBackground)
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Go", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val backupHelper = remember { DatabaseBackupHelper(context) }
    
    // File Picker for Restore
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val success = withContext(Dispatchers.IO) {
                    backupHelper.restoreDatabase(uri)
                }
                if (success) {
                    Toast.makeText(context, "Restore successful! Restarting app...", Toast.LENGTH_LONG).show()
                    
                    // Force restart the app for the new DB to be properly instantiated
                    val intent = Intent(context, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    context.startActivity(intent)
                    if (context is Activity) {
                        context.finish()
                    }
                    Runtime.getRuntime().exit(0)
                } else {
                    Toast.makeText(context, "Restore failed! Invalid backup file.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Permission for Backup (pre Android 11)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            coroutineScope.launch {
                val path = withContext(Dispatchers.IO) { backupHelper.backupDatabase() }
                if (path != null) {
                    Toast.makeText(context, "Backup Saved:\n$path", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Backup Failed!", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Storage permission required for backup", Toast.LENGTH_SHORT).show()
        }
    }

    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Backup & Restore", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onBackground) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                    modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth().shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = ThemeMaroonPrimary, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Secure your POS Data", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Create a safe local backup of your database, or restore an existing one.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                        
                        Spacer(modifier = Modifier.height(32.dp))

                        // Backup Button
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                        permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                        return@Button
                                    }
                                }
                                coroutineScope.launch {
                                    val path = withContext(Dispatchers.IO) { backupHelper.backupDatabase() }
                                    if (path != null) {
                                        Toast.makeText(context, "Backup Saved:\n$path", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "Backup Failed!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ThemeMaroonPrimary)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("BACKUP DATABASE", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Restore Button
                        OutlinedButton(
                            onClick = { launcher.launch("*/*") },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ThemeMaroonPrimary)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("RESTORE DATABASE", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Warning: Restoring will overwrite all current data and forcefully restart the application.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrinterSettingsScreen(viewModel: PosViewModel, navController: NavController) {
    val currentConfig by viewModel.printerConfig.collectAsState()
    var selectedType by remember { mutableStateOf(currentConfig.type) }
    var printerAddress by remember { mutableStateOf(currentConfig.address) }
    var selectedWidth by remember { mutableStateOf(currentConfig.width) } // "58" or "80"

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val printerHelper = remember { PrinterHelper(context) }
    var isScanning by remember { mutableStateOf(false) }
    val btDevices = remember { mutableStateListOf<Pair<String, String>>() }
    val wifiDevices = remember { mutableStateListOf<Pair<String, String>>() }

    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

    fun startWifiScan() {
        wifiDevices.clear()
        isScanning = true
        Toast.makeText(context, "Scanning local network for Wi-Fi printers...", Toast.LENGTH_SHORT).show()

        coroutineScope.launch(Dispatchers.IO) {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            @Suppress("DEPRECATION")
            val ipInt = wifiManager.connectionInfo.ipAddress
            val subnetPrefix = if (ipInt != 0) {
                String.format(
                    Locale.getDefault(),
                    "%d.%d.%d",
                    (ipInt and 0xff),
                    (ipInt shr 8 and 0xff),
                    (ipInt shr 16 and 0xff)
                )
            } else {
                "192.168.1"
            }

            (1..254).map { i ->
                launch {
                    val targetIp = "$subnetPrefix.$i"
                    try {
                        val socket = Socket()
                        socket.connect(InetSocketAddress(targetIp, 9100), 250)
                        socket.close()
                        withContext(Dispatchers.Main) {
                            if (wifiDevices.none { it.second == targetIp }) {
                                wifiDevices.add("Thermal Printer ($targetIp)" to targetIp)
                            }
                        }
                    } catch (_: Exception) {}
                }
            }

            delay(3000)
            withContext(Dispatchers.Main) {
                isScanning = false
                if (wifiDevices.isEmpty()) {
                    Toast.makeText(context, "No Wi-Fi printers found on $subnetPrefix.*", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Found ${wifiDevices.size} Wi-Fi printer(s)!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            try {
                btDevices.clear()
                bluetoothAdapter?.bondedDevices?.forEach { btDevices.add((it.name ?: "Unknown") to it.address) }
                bluetoothAdapter?.startDiscovery()
                isScanning = true
                Toast.makeText(context, "Scanning for Bluetooth printers...", Toast.LENGTH_SHORT).show()
            } catch (e: SecurityException) { }
        } else {
            Toast.makeText(context, "Permissions required", Toast.LENGTH_SHORT).show()
        }
    }

    fun startBtScan() {
        val hasPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }

        if (hasPermissions) {
            btDevices.clear()
            try {
                bluetoothAdapter?.bondedDevices?.forEach { btDevices.add((it.name ?: "Unknown") to it.address) }
                bluetoothAdapter?.startDiscovery()
                isScanning = true
                Toast.makeText(context, "Scanning for Bluetooth printers...", Toast.LENGTH_SHORT).show()
            } catch (e: SecurityException) {}
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissionLauncher.launch(arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION))
            } else {
                permissionLauncher.launch(arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION))
            }
        }
    }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, intent: Intent) {
                when (intent.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        device?.let {
                            try {
                                val name = it.name ?: "Unknown Printer"
                                val address = it.address
                                if (btDevices.none { d -> d.second == address }) {
                                    btDevices.add(name to address)
                                }
                            } catch (e: SecurityException) {}
                        }
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> { isScanning = false }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        context.registerReceiver(receiver, filter)

        onDispose {
            try { context.unregisterReceiver(receiver) } catch (e: Exception) {}
            try { bluetoothAdapter?.cancelDiscovery() } catch (e: SecurityException) {}
        }
    }

    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Printer Configuration", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onBackground) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                    modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth().weight(1f).shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Connection Type", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Surface(
                                modifier = Modifier.weight(1f).clickable { selectedType = "BLUETOOTH"; isScanning = false },
                                shape = RoundedCornerShape(12.dp), color = if (selectedType == "BLUETOOTH") ThemeMaroonPrimary else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text("Bluetooth", modifier = Modifier.padding(16.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = if (selectedType == "BLUETOOTH") Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Surface(
                                modifier = Modifier.weight(1f).clickable { selectedType = "WIFI"; isScanning = false },
                                shape = RoundedCornerShape(12.dp), color = if (selectedType == "WIFI") ThemeMaroonPrimary else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text("Wi-Fi", modifier = Modifier.padding(16.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = if (selectedType == "WIFI") Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Paper Width Selection
                        Text("Paper Width", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Surface(
                                modifier = Modifier.weight(1f).clickable { selectedWidth = "58" },
                                shape = RoundedCornerShape(12.dp), color = if (selectedWidth == "58") ThemeMaroonPrimary else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text("58 mm (Mobile)", modifier = Modifier.padding(16.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = if (selectedWidth == "58") Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Surface(
                                modifier = Modifier.weight(1f).clickable { selectedWidth = "80" },
                                shape = RoundedCornerShape(12.dp), color = if (selectedWidth == "80") ThemeMaroonPrimary else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text("80 mm (e-POS)", modifier = Modifier.padding(16.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = if (selectedWidth == "80") Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = printerAddress, onValueChange = { printerAddress = it },
                            label = { Text(if (selectedType == "WIFI") "Printer IP Address" else "Bluetooth Device MAC") },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeMaroonPrimary, focusedLabelColor = ThemeMaroonPrimary)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.savePrinterConfig(PrinterConfig(selectedType, printerAddress, selectedWidth))
                                Toast.makeText(context, "Printer Settings Saved!", Toast.LENGTH_SHORT).show()
                                navController.navigateUp()
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ThemeMaroonPrimary)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Configuration", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                if (printerAddress.isBlank()) {
                                    Toast.makeText(context, "Please enter an IP address or MAC first", Toast.LENGTH_SHORT).show()
                                    return@OutlinedButton
                                }
                                viewModel.savePrinterConfig(PrinterConfig(selectedType, printerAddress, selectedWidth))
                                
                                coroutineScope.launch {
                                    try {
                                        val testOrder = Order(
                                            orderNumber = "TEST-001",
                                            timestamp = System.currentTimeMillis(),
                                            totalAmount = 100.0,
                                            customerName = "Test Customer"
                                        )
                                        val testItem = listOf(
                                            OrderLine(
                                                orderId = 0,
                                                productId = 1,
                                                quantity = 1.0,
                                                unitPrice = 100.0,
                                                lineTotal = 100.0
                                            )
                                        )
                                        printerHelper.printReceipt(
                                            order = testOrder,
                                            items = testItem,
                                            method = "Test",
                                            shopName = "POS TEST SHOP",
                                            width = selectedWidth.toIntOrNull() ?: 58,
                                            isWifi = selectedType == "WIFI",
                                            address = printerAddress
                                        )
                                        Toast.makeText(context, "Test print sent successfully!", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Test failed: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ThemeMaroonPrimary)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("TEST PRINTER CONNECTION", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (selectedType == "BLUETOOTH") "Paired Bluetooth Devices" else "Available Wi-Fi Printers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.weight(1f))
                            if (isScanning) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = ThemeMaroonPrimary, strokeWidth = 2.dp)
                            } else {
                                TextButton(onClick = { if (selectedType == "BLUETOOTH") startBtScan() else startWifiScan() }) {
                                    Text("SCAN", color = ThemeMaroonPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        val listToShow = if (selectedType == "BLUETOOTH") btDevices else wifiDevices

                        if (listToShow.isEmpty() && !isScanning) {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("No printers found. Tap SCAN.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            LazyColumn(modifier = Modifier.weight(1f)) {
                                items(listToShow) { device ->
                                    ListItem(
                                        headlineContent = { Text(device.first, fontWeight = FontWeight.Bold) },
                                        supportingContent = { Text(device.second) },
                                        modifier = Modifier.clickable {
                                            printerAddress = device.second
                                            viewModel.savePrinterConfig(PrinterConfig(selectedType, printerAddress, selectedWidth))
                                            Toast.makeText(context, "Selected: ${device.first}", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(viewModel: PosViewModel, navController: NavController) {
    val profile by viewModel.shopProfile.collectAsState()
    var shopName by remember { mutableStateOf(profile.name) }
    var shopAddress by remember { mutableStateOf(profile.address) }
    var shopPhone by remember { mutableStateOf(profile.phone) }
    var shopEmail by remember { mutableStateOf(profile.email) }
    var bankName by remember { mutableStateOf(profile.bankName) }
    var accountNo by remember { mutableStateOf(profile.accountNo) }
    var upiId by remember { mutableStateOf(profile.upiId) }

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("My Profile", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onBackground) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                    modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth().weight(1f).shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        ItemContentProfile(
                            shopName = shopName, onShopNameChange = { shopName = it },
                            shopAddress = shopAddress, onShopAddressChange = { shopAddress = it },
                            shopPhone = shopPhone, onShopPhoneChange = { shopPhone = it },
                            shopEmail = shopEmail, onShopEmailChange = { shopEmail = it },
                            bankName = bankName, onBankNameChange = { bankName = it },
                            accountNo = accountNo, onAccountNoChange = { accountNo = it },
                            upiId = upiId, onUpiIdChange = { upiId = it },
                            onSave = {
                                viewModel.saveShopProfile(ShopProfile(shopName, shopAddress, shopPhone, shopEmail, bankName, accountNo, upiId))
                                Toast.makeText(context, "Profile Saved!", Toast.LENGTH_LONG).show()
                                navController.navigateUp()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ItemContentProfile(
    shopName: String, onShopNameChange: (String) -> Unit,
    shopAddress: String, onShopAddressChange: (String) -> Unit,
    shopPhone: String, onShopPhoneChange: (String) -> Unit,
    shopEmail: String, onShopEmailChange: (String) -> Unit,
    bankName: String, onBankNameChange: (String) -> Unit,
    accountNo: String, onAccountNoChange: (String) -> Unit,
    upiId: String, onUpiIdChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Text("Shop Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ThemeMaroonPrimary)
    
    OutlinedTextField(
        value = shopName, onValueChange = onShopNameChange, label = { Text("Shop Name") },
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeMaroonPrimary, focusedLabelColor = ThemeMaroonPrimary)
    )
    OutlinedTextField(
        value = shopAddress, onValueChange = onShopAddressChange, label = { Text("Address") },
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeMaroonPrimary, focusedLabelColor = ThemeMaroonPrimary)
    )
    OutlinedTextField(
        value = shopPhone, onValueChange = onShopPhoneChange, label = { Text("Phone") },
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeMaroonPrimary, focusedLabelColor = ThemeMaroonPrimary)
    )
    OutlinedTextField(
        value = shopEmail, onValueChange = onShopEmailChange, label = { Text("Email ID") },
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeMaroonPrimary, focusedLabelColor = ThemeMaroonPrimary)
    )

    Spacer(modifier = Modifier.height(8.dp))
    Text("Bank Details (For UPI)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ThemeMaroonPrimary)

    OutlinedTextField(
        value = bankName, onValueChange = onBankNameChange, label = { Text("Bank Name") },
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeMaroonPrimary, focusedLabelColor = ThemeMaroonPrimary)
    )
    OutlinedTextField(
        value = accountNo, onValueChange = onAccountNoChange, label = { Text("Account Number") },
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeMaroonPrimary, focusedLabelColor = ThemeMaroonPrimary)
    )
    OutlinedTextField(
        value = upiId, onValueChange = onUpiIdChange, label = { Text("UPI ID (e.g. merchant@okicici)") },
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeMaroonPrimary, focusedLabelColor = ThemeMaroonPrimary)
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    Token("Save Profile") {
        onSave()
    }
}

@Composable
fun Token(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ThemeMaroonPrimary)
    ) {
        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
    }
}