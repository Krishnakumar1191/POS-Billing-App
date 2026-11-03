package com.example.posapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
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
import com.example.posapp.data.entities.Tax
import com.example.posapp.ui.PosViewModel
import com.example.posapp.ui.components.AppBackground
import com.example.posapp.ui.components.MenuGridBottomSheet
import com.example.posapp.ui.components.SharedBottomBar
import com.example.posapp.ui.navigation.Screen
import com.example.posapp.ui.theme.ThemeMaroonPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxListScreen(viewModel: PosViewModel, navController: NavController) {
    val taxes by viewModel.taxes.collectAsState()
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
                    title = { Text("Taxes", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.onBackground) },
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
                    onClick = { navController.navigate(Screen.TaxEdit.createRoute(0L)) },
                    modifier = Modifier.padding(bottom = 80.dp),
                    containerColor = ThemeMaroonPrimary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Tax")
                }
            },
            bottomBar = {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SharedBottomBar(
                        navController = navController,
                        currentRoute = Screen.Taxes.route,
                        onMenuClick = { showMenuGrid = true }
                    )
                }
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { padding ->
            if (taxes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No taxes configured", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(taxes) { tax ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate(Screen.TaxEdit.createRoute(tax.id)) }
                                .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.05f)),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Calculate, contentDescription = null, tint = ThemeMaroonPrimary)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tax.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val amountStr = if (tax.amountType == "percent") "${tax.amount}%" else "₹${tax.amount}"
                                    val incStr = if (tax.priceInclude) "Included in Price" else "Excluded from Price"
                                    Text(
                                        text = "$amountStr | Scope: ${tax.typeTaxUse.uppercase()} | $incStr",
                                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { viewModel.deleteTax(tax.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
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
fun TaxEditScreen(viewModel: PosViewModel, navController: NavController, taxId: Long) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var amountType by remember { mutableStateOf("percent") }
    var typeTaxUse by remember { mutableStateOf("sale") }
    var priceInclude by remember { mutableStateOf(false) }

    var amountTypeExpanded by remember { mutableStateOf(false) }
    var taxUseExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(taxId) {
        if (taxId > 0) {
            viewModel.getTaxById(taxId)?.let { tax ->
                name = tax.name
                amount = tax.amount.toString()
                amountType = tax.amountType
                typeTaxUse = tax.typeTaxUse
                priceInclude = tax.priceInclude
            }
        }
    }

    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(if (taxId == 0L) "New Tax" else "Edit Tax", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.onBackground) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                    modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                )
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth().shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Tax Configuration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ThemeMaroonPrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Tax Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeMaroonPrimary, focusedLabelColor = ThemeMaroonPrimary)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            label = { Text("Amount") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeMaroonPrimary, focusedLabelColor = ThemeMaroonPrimary)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        ExposedDropdownMenuBox(
                            expanded = amountTypeExpanded,
                            onExpandedChange = { amountTypeExpanded = !amountTypeExpanded }
                        ) {
                            OutlinedTextField(
                                value = amountType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Tax Computation") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = amountTypeExpanded) },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeMaroonPrimary, focusedLabelColor = ThemeMaroonPrimary)
                            )
                            ExposedDropdownMenu(
                                expanded = amountTypeExpanded,
                                onDismissRequest = { amountTypeExpanded = false }
                            ) {
                                listOf("percent", "fixed", "group").forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            amountType = option
                                            amountTypeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        ExposedDropdownMenuBox(
                            expanded = taxUseExpanded,
                            onExpandedChange = { taxUseExpanded = !taxUseExpanded }
                        ) {
                            OutlinedTextField(
                                value = typeTaxUse,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Tax Scope") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = taxUseExpanded) },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeMaroonPrimary, focusedLabelColor = ThemeMaroonPrimary)
                            )
                            ExposedDropdownMenu(
                                expanded = taxUseExpanded,
                                onDismissRequest = { taxUseExpanded = false }
                            ) {
                                listOf("sale", "purchase", "none").forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            typeTaxUse = option
                                            taxUseExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                            Checkbox(checked = priceInclude, onCheckedChange = { priceInclude = it }, colors = CheckboxDefaults.colors(checkedColor = ThemeMaroonPrimary))
                            Text("Included in Price", fontWeight = FontWeight.Medium)
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Button(
                            onClick = {
                                val parsedAmount = amount.toDoubleOrNull() ?: 0.0
                                if (name.isNotBlank()) {
                                    val newTax = Tax(
                                        id = taxId,
                                        name = name,
                                        amount = parsedAmount,
                                        amountType = amountType,
                                        typeTaxUse = typeTaxUse,
                                        priceInclude = priceInclude
                                    )
                                    if (taxId == 0L) {
                                        viewModel.insertTax(newTax) { navController.navigateUp() }
                                    } else {
                                        viewModel.updateTax(newTax) { navController.navigateUp() }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ThemeMaroonPrimary)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Tax Rule", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}