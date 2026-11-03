package com.example.posapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashManagementScreen(viewModel: PosViewModel, navController: NavController) {
    val cashTransactions by viewModel.cashTransactions.collectAsState(initial = emptyList())

    var showMenuGrid by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("CASH_IN") } // CASH_IN, CASH_OUT, EXPENSE, LEDGER

    var showAddDialog by remember { mutableStateOf(false) }
    var inputAmount by remember { mutableStateOf("") }
    var inputReason by remember { mutableStateOf("") }
    
    // Group transactions day by day for the table ledger
    val dailyLedger = remember(cashTransactions) {
        cashTransactions.groupBy {
            val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.map { (dateMillis, txs) ->
            val dateStr = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(dateMillis)
            val inAmt = txs.filter { it.type == "CASH_IN" }.sumOf { it.amount }
            val outAmt = txs.filter { it.type == "CASH_OUT" }.sumOf { it.amount }
            val expAmt = txs.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            Triple(dateMillis, dateStr, listOf(inAmt, outAmt, expAmt))
        }.sortedByDescending { it.first }
    }

    if (showMenuGrid) {
        MenuGridBottomSheet(
            navController = navController,
            onDismissRequest = { showMenuGrid = false }
        )
    }

    if (showAddDialog) {
        val dialogType = if (selectedTab == "LEDGER") "CASH_IN" else selectedTab
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = when (dialogType) {
                        "CASH_IN" -> "Add Cash In"
                        "CASH_OUT" -> "Add Cash Out"
                        else -> "Add Expense"
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = inputAmount,
                        onValueChange = { inputAmount = it },
                        label = { Text("Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputReason,
                        onValueChange = { inputReason = it },
                        label = { Text("Reason / Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = inputAmount.toDoubleOrNull()
                        if (amt != null && inputReason.isNotBlank()) {
                            viewModel.insertCashTransaction(dialogType, amt, inputReason)
                            showAddDialog = false
                            inputAmount = ""
                            inputReason = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ThemeMaroonPrimary)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
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
                    title = { Text("Cash Management", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.onBackground) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                    modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.padding(bottom = 80.dp),
                    containerColor = ThemeMaroonPrimary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Transaction")
                }
            },
            bottomBar = {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SharedBottomBar(
                        navController = navController,
                        currentRoute = Screen.CashManagement.route,
                        onMenuClick = { showMenuGrid = true }
                    )
                }
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Tab Selection
                TabRow(
                    selectedTabIndex = when (selectedTab) {
                        "CASH_IN" -> 0
                        "CASH_OUT" -> 1
                        "EXPENSE" -> 2
                        else -> 3
                    },
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    indicator = { },
                    divider = { }
                ) {
                    Tab(
                        selected = selectedTab == "CASH_IN",
                        onClick = { selectedTab = "CASH_IN" },
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedTab == "CASH_IN") MaterialTheme.colorScheme.surface else Color.Transparent)
                    ) {
                        Text("In", modifier = Modifier.padding(vertical = 12.dp), fontWeight = if (selectedTab == "CASH_IN") FontWeight.Bold else FontWeight.Medium)
                    }
                    Tab(
                        selected = selectedTab == "CASH_OUT",
                        onClick = { selectedTab = "CASH_OUT" },
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedTab == "CASH_OUT") MaterialTheme.colorScheme.surface else Color.Transparent)
                    ) {
                        Text("Out", modifier = Modifier.padding(vertical = 12.dp), fontWeight = if (selectedTab == "CASH_OUT") FontWeight.Bold else FontWeight.Medium)
                    }
                    Tab(
                        selected = selectedTab == "EXPENSE",
                        onClick = { selectedTab = "EXPENSE" },
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedTab == "EXPENSE") MaterialTheme.colorScheme.surface else Color.Transparent)
                    ) {
                        Text("Exp", modifier = Modifier.padding(vertical = 12.dp), fontWeight = if (selectedTab == "EXPENSE") FontWeight.Bold else FontWeight.Medium)
                    }
                    Tab(
                        selected = selectedTab == "LEDGER",
                        onClick = { selectedTab = "LEDGER" },
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedTab == "LEDGER") MaterialTheme.colorScheme.surface else Color.Transparent)
                    ) {
                        Text("Ledger", modifier = Modifier.padding(vertical = 12.dp), fontWeight = if (selectedTab == "LEDGER") FontWeight.Bold else FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == "LEDGER") {
                    // Daily Ledger Table View
                    if (dailyLedger.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No ledger records found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxWidth().shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.05f)),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                                // Table Header
                                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                                    Text("Date", modifier = Modifier.weight(1.2f), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("In", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.End, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Out", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.End, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Exp", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.End, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                HorizontalDivider()
                                
                                // Table Rows
                                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 100.dp)) {
                                    items(dailyLedger) { entry ->
                                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text(entry.second, modifier = Modifier.weight(1.2f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                            Text("${"%.0f".format(entry.third[0])}", modifier = Modifier.weight(1f), textAlign = TextAlign.End, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                                            Text("${"%.0f".format(entry.third[1])}", modifier = Modifier.weight(1f), textAlign = TextAlign.End, color = Color(0xFFFF9800), fontWeight = FontWeight.Bold)
                                            Text("${"%.0f".format(entry.third[2])}", modifier = Modifier.weight(1f), textAlign = TextAlign.End, color = Color(0xFFE91E63), fontWeight = FontWeight.Bold)
                                        }
                                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Standard List View for In/Out/Exp
                    val displayList = cashTransactions.filter { it.type == selectedTab }

                    if (displayList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No records found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(displayList) { tx ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth().shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.05f)),
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier.size(48.dp).background(
                                                if (selectedTab == "CASH_IN") Color(0xFFE8F5E9) else Color(0xFFFFEBEE), 
                                                CircleShape
                                            ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Info, 
                                                contentDescription = null,
                                                tint = if (selectedTab == "CASH_IN") Color(0xFF4CAF50) else if(selectedTab == "CASH_OUT") Color(0xFFFF9800) else Color(0xFFF44336),
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
                                                text = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(tx.timestamp),
                                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "₹ ${"%.2f".format(tx.amount)}",
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                            IconButton(onClick = { viewModel.deleteCashTransaction(tx.id) }, modifier = Modifier.size(24.dp)) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
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
}