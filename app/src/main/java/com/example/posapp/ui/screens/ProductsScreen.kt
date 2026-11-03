package com.example.posapp.ui.screens

import android.net.Uri
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.posapp.data.entities.Product
import com.example.posapp.ui.PosViewModel
import com.example.posapp.ui.components.AppBackground
import com.example.posapp.ui.components.MenuGridBottomSheet
import com.example.posapp.ui.components.SharedBottomBar
import com.example.posapp.ui.navigation.Screen
import com.example.posapp.ui.theme.ThemeMaroonPrimary
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(viewModel: PosViewModel, navController: NavController) {
    val products by viewModel.products.collectAsState(initial = emptyList())
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
                    title = { Text("Products", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.ProductEdit.createRoute(0L)) },
                    modifier = Modifier.padding(bottom = 80.dp),
                    containerColor = ThemeMaroonPrimary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Product")
                }
            },
            bottomBar = {
                SharedBottomBar(
                    navController = navController,
                    currentRoute = Screen.Products.route,
                    onMenuClick = { showMenuGrid = true }
                )
            }
        ) { padding ->
            if (products.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("No products found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(products) { product ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate(Screen.ProductEdit.createRoute(product.id))
                                }
                                .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.05f)),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (product.imageUri.isNullOrBlank()) {
                                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                    Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Sales: ₹${"%.2f".format(product.price)} | Cost: ₹${"%.2f".format(product.costPrice)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Stock: ${product.stock} | Type: ${product.type}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = { viewModel.deleteProduct(product.id) }) {
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
fun ProductEditScreen(viewModel: PosViewModel, navController: NavController, productId: Long) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("product") }
    var price by remember { mutableStateOf("") }
    var costPrice by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var tracking by remember { mutableStateOf("none") }
    var description by remember { mutableStateOf("") }
    var availableInPos by remember { mutableStateOf(true) }
    var canBePurchased by remember { mutableStateOf(true) }
    var imageUri by remember { mutableStateOf<String?>(null) }
    
    // Category selection
    var categoryId by remember { mutableStateOf<Long?>(null) }
    var categoryExpanded by remember { mutableStateOf(false) }
    val categories by viewModel.categories.collectAsState()
    val selectedCategoryName = categories.find { it.id == categoryId }?.name ?: "No Category"

    // Tax selections
    var saleTaxId by remember { mutableStateOf<Long?>(null) }
    var purchaseTaxId by remember { mutableStateOf<Long?>(null) }
    var saleTaxExpanded by remember { mutableStateOf(false) }
    var purchaseTaxExpanded by remember { mutableStateOf(false) }
    val allTaxes by viewModel.taxes.collectAsState()
    
    val saleTaxes = allTaxes.filter { it.typeTaxUse == "sale" || it.typeTaxUse == "none" }
    val purchaseTaxes = allTaxes.filter { it.typeTaxUse == "purchase" || it.typeTaxUse == "none" }
    
    val selectedSaleTaxName = saleTaxes.find { it.id == saleTaxId }?.name ?: "No Tax"
    val selectedPurchaseTaxName = purchaseTaxes.find { it.id == purchaseTaxId }?.name ?: "No Tax"

    var typeExpanded by remember { mutableStateOf(false) }
    var trackingExpanded by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imageUri = it.toString() }
    }

    LaunchedEffect(productId) {
        if (productId > 0) {
            viewModel.getProductById(productId)?.let { product ->
                name = product.name
                type = product.type
                price = product.price.toString()
                costPrice = product.costPrice.toString()
                stock = product.stock.toString()
                barcode = product.barcode ?: ""
                tracking = product.tracking
                description = product.description ?: ""
                availableInPos = product.availableInPos
                canBePurchased = product.canBePurchased
                categoryId = product.categoryId
                imageUri = product.imageUri
                saleTaxId = product.saleTaxId
                purchaseTaxId = product.purchaseTaxId
            }
        }
    }

    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(if (productId == 0L) "New Product" else "Edit Product", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        Button(
                            onClick = {
                                val parsedPrice = price.toDoubleOrNull() ?: 0.0
                                val parsedCost = costPrice.toDoubleOrNull() ?: 0.0
                                val parsedStock = stock.toDoubleOrNull() ?: 0.0

                                if (name.isNotBlank()) {
                                    // Make a permanent local copy of the image if it's a content URI
                                    var finalImageUri = imageUri
                                    if (finalImageUri != null && finalImageUri!!.startsWith("content://")) {
                                        try {
                                            val inputStream = context.contentResolver.openInputStream(Uri.parse(finalImageUri))
                                            if (inputStream != null) {
                                                val file = File(context.filesDir, "prod_img_${System.currentTimeMillis()}.jpg")
                                                val outputStream = FileOutputStream(file)
                                                inputStream.copyTo(outputStream)
                                                inputStream.close()
                                                outputStream.close()
                                                finalImageUri = Uri.fromFile(file).toString()
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }

                                    val newProduct = Product(
                                        id = productId,
                                        name = name,
                                        price = parsedPrice,
                                        costPrice = parsedCost,
                                        stock = parsedStock,
                                        categoryId = categoryId,
                                        barcode = barcode.ifBlank { null },
                                        type = type,
                                        tracking = tracking,
                                        description = description.ifBlank { null },
                                        availableInPos = availableInPos,
                                        canBePurchased = canBePurchased,
                                        imageUri = finalImageUri,
                                        saleTaxId = saleTaxId,
                                        purchaseTaxId = purchaseTaxId
                                    )
                                    if (productId == 0L) {
                                        viewModel.insertProduct(newProduct) { navController.navigateUp() }
                                    } else {
                                        viewModel.updateProduct(newProduct) { navController.navigateUp() }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ThemeMaroonPrimary),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Save", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.surface)
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Section: Product Image Upload
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.1f))
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp))
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUri.isNullOrBlank()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Add, contentDescription = "Add Image", tint = ThemeMaroonPrimary, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Add Photo", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Product Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp))
                            )
                        }
                    }
                }

                FormSection(title = "General Information") {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Product Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeMaroonPrimary, focusedLabelColor = ThemeMaroonPrimary)
                    )
                    
                    ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = !typeExpanded }) {
                        OutlinedTextField(
                            value = when (type) { "product" -> "Storable Product"; "service" -> "Service"; else -> "Storable Product" },
                            onValueChange = {}, readOnly = true, label = { Text("Product Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeMaroonPrimary, focusedLabelColor = ThemeMaroonPrimary)
                        )
                        ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
//                            DropdownMenuItem(text = { Text("Consumable") }, onClick = { type = "consu"; typeExpanded = false })
                            DropdownMenuItem(text = { Text("Storable Product") }, onClick = { type = "product"; typeExpanded = false })
                            DropdownMenuItem(text = { Text("Service") }, onClick = { type = "service"; typeExpanded = false })
                        }
                    }

                    ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = !categoryExpanded }) {
                        OutlinedTextField(
                            value = selectedCategoryName, onValueChange = {}, readOnly = true, label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeMaroonPrimary, focusedLabelColor = ThemeMaroonPrimary)
                        )
                        ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                            DropdownMenuItem(text = { Text("No Category") }, onClick = { categoryId = null; categoryExpanded = false })
                            categories.forEach { category ->
                                DropdownMenuItem(text = { Text(category.name) }, onClick = { categoryId = category.id; categoryExpanded = false })
                            }
                        }
                    }

                    OutlinedTextField(
                        value = description, onValueChange = { description = it }, label = { Text("Internal Notes / Description") },
                        modifier = Modifier.fillMaxWidth().height(100.dp), maxLines = 3, shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeMaroonPrimary, focusedLabelColor = ThemeMaroonPrimary)
                    )
                }

                FormSection(title = "Pricing & Taxes") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = price, onValueChange = { price = it }, label = { Text("Sales Price") },
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeMaroonPrimary, focusedLabelColor = ThemeMaroonPrimary)
                        )
                        if (type != "service") {
                            OutlinedTextField(
                                value = costPrice, onValueChange = { costPrice = it }, label = { Text("Cost Price") },
                                modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeMaroonPrimary, focusedLabelColor = ThemeMaroonPrimary)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Sales Tax Dropdown
                        ExposedDropdownMenuBox(
                            expanded = saleTaxExpanded, 
                            onExpandedChange = { saleTaxExpanded = !saleTaxExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedSaleTaxName, onValueChange = {}, readOnly = true, label = { Text("Sales Tax") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = saleTaxExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeMaroonPrimary, focusedLabelColor = ThemeMaroonPrimary)
                            )
                            ExposedDropdownMenu(expanded = saleTaxExpanded, onDismissRequest = { saleTaxExpanded = false }) {
                                DropdownMenuItem(text = { Text("No Tax") }, onClick = { saleTaxId = null; saleTaxExpanded = false })
                                saleTaxes.forEach { tax ->
                                    DropdownMenuItem(text = { Text("${tax.name} (${tax.amount})") }, onClick = { saleTaxId = tax.id; saleTaxExpanded = false })
                                }
                            }
                        }

                        // Purchase Tax Dropdown
                        ExposedDropdownMenuBox(
                            expanded = purchaseTaxExpanded, 
                            onExpandedChange = { purchaseTaxExpanded = !purchaseTaxExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedPurchaseTaxName, onValueChange = {}, readOnly = true, label = { Text("Purchase Tax") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = purchaseTaxExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeMaroonPrimary, focusedLabelColor = ThemeMaroonPrimary)
                            )
                            ExposedDropdownMenu(expanded = purchaseTaxExpanded, onDismissRequest = { purchaseTaxExpanded = false }) {
                                DropdownMenuItem(text = { Text("No Tax") }, onClick = { purchaseTaxId = null; purchaseTaxExpanded = false })
                                purchaseTaxes.forEach { tax ->
                                    DropdownMenuItem(text = { Text("${tax.name} (${tax.amount})") }, onClick = { purchaseTaxId = tax.id; purchaseTaxExpanded = false })
                                }
                            }
                        }
                    }
                }

                if (type == "product") {
                    FormSection(title = "Inventory") {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(
                                value = stock, onValueChange = { stock = it }, label = { Text("Quantity Available") },
                                modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeMaroonPrimary, focusedLabelColor = ThemeMaroonPrimary)
                            )
                            
                            ExposedDropdownMenuBox(
                                expanded = trackingExpanded, 
                                onExpandedChange = { trackingExpanded = !trackingExpanded },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = when (tracking) { "lot" -> "By Lots"; "serial" -> "By Unique Serial Number"; else -> "No Tracking" },
                                    onValueChange = {}, readOnly = true, label = { Text("Tracking Mode") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = trackingExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeMaroonPrimary, focusedLabelColor = ThemeMaroonPrimary)
                                )
                                ExposedDropdownMenu(expanded = trackingExpanded, onDismissRequest = { trackingExpanded = false }) {
                                    DropdownMenuItem(text = { Text("No Tracking") }, onClick = { tracking = "none"; trackingExpanded = false })
                                    DropdownMenuItem(text = { Text("By Lots") }, onClick = { tracking = "lot"; trackingExpanded = false })
                                    DropdownMenuItem(text = { Text("By Unique Serial Number") }, onClick = { tracking = "serial"; trackingExpanded = false })
                                }
                            }
                        }
                    }
                }

                FormSection(title = "Settings") {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Checkbox(checked = availableInPos, onCheckedChange = { availableInPos = it }, colors = CheckboxDefaults.colors(checkedColor = ThemeMaroonPrimary))
                        Text("Available in POS", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Checkbox(checked = canBePurchased, onCheckedChange = { canBePurchased = it }, colors = CheckboxDefaults.colors(checkedColor = ThemeMaroonPrimary))
                        Text("Can be Purchased", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun FormSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ThemeMaroonPrimary)
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}