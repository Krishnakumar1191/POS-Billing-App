package com.example.posapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.posapp.data.entities.Product
import com.example.posapp.ui.PosViewModel
import com.example.posapp.ui.components.AppBackground
import com.example.posapp.ui.components.MenuGridBottomSheet
import com.example.posapp.ui.components.SharedBottomBar
import com.example.posapp.ui.navigation.Screen
import com.example.posapp.ui.theme.ThemeMaroonPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosHomeScreen(viewModel: PosViewModel, navController: NavController) {
    val products by viewModel.products.collectAsState(initial = emptyList())
    val categories by viewModel.categories.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val subtotal by viewModel.cartSubtotal.collectAsState()
    val taxTotal by viewModel.cartTaxTotal.collectAsState()
    val total by viewModel.cartTotal.collectAsState()

    var showMenuGrid by remember { mutableStateOf(false) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) } // null means "All"

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
                    title = { Text("Menu", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = MaterialTheme.colorScheme.onBackground) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.background
                    ),
                    modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                        }
                    },
                    actions = {
                        Box(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(44.dp)
                                .background(MaterialTheme.colorScheme.surface, CircleShape)
                                .shadow(elevation = 2.dp, shape = CircleShape, spotColor = Color.Black.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        currentRoute = Screen.Home.route,
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
                // Main Area (Products)
                Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    // Dynamic Category Pills
                    LazyRow(
                        contentPadding = PaddingValues(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // The "All" static category
                        item {
                            Surface(
                                modifier = Modifier.clickable { selectedCategoryId = null },
                                shape = RoundedCornerShape(24.dp),
                                color = if (selectedCategoryId == null) ThemeMaroonPrimary else MaterialTheme.colorScheme.surface,
                                shadowElevation = if (selectedCategoryId == null) 8.dp else 2.dp
                            ) {
                                Text(
                                    text = "All",
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                    color = if (selectedCategoryId == null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Map over actual SQLite categories
                        items(categories) { category ->
                            Surface(
                                modifier = Modifier.clickable { selectedCategoryId = category.id },
                                shape = RoundedCornerShape(24.dp),
                                color = if (selectedCategoryId == category.id) ThemeMaroonPrimary else MaterialTheme.colorScheme.surface,
                                shadowElevation = if (selectedCategoryId == category.id) 8.dp else 2.dp
                            ) {
                                Text(
                                    text = category.name,
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                    color = if (selectedCategoryId == category.id) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Product Grid (Filtered)
                    val filteredProducts = if (selectedCategoryId == null) {
                        products
                    } else {
                        products.filter { it.categoryId == selectedCategoryId }
                    }

                    if (filteredProducts.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No products in this category", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredProducts) { product ->
                                ProductCard(product) {
                                    viewModel.addProductToCart(product)
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Cart Section
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Current Order", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                            IconButton(onClick = { viewModel.clearCart() }) {
                                Icon(Icons.Default.Delete, contentDescription = "Clear", tint = MaterialTheme.colorScheme.error)
                            }
                        }

                        if (cart.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                Text("Cart is empty", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
                                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(cart) { line ->
                                    val product = products.find { it.id == line.productId }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(product?.name ?: "Unknown Item", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                            Text("₹${"%.2f".format(line.unitPrice)}", style = MaterialTheme.typography.bodyMedium, color = ThemeMaroonPrimary, fontWeight = FontWeight.Bold)
                                        }
                                        
                                        // +/- Quantity Controller
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).padding(4.dp)
                                        ) {
                                            IconButton(onClick = { viewModel.decrementProductInCart(line.productId) }, modifier = Modifier.size(32.dp)) {
                                                Text("-", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                            }
                                            Text("${line.quantity.toInt()}", modifier = Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Bold)
                                            IconButton(onClick = { product?.let { viewModel.addProductToCart(it) } }, modifier = Modifier.size(32.dp)) {
                                                Text("+", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text("₹${"%.2f".format(line.lineTotal)}", fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                            }
                        }

                        // Order Summary Bottom Sheet inside Cart
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("₹${"%.2f".format(total)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = ThemeMaroonPrimary)
                                    }
                                    Button(
                                        onClick = { navController.navigate(Screen.Checkout.route) },
                                        enabled = cart.isNotEmpty(),
                                        modifier = Modifier.width(140.dp).height(56.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ThemeMaroonPrimary)
                                    ) {
                                        Text("PAY", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCard(product: Product, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp) // Adjusted height for image space
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUri.isNullOrBlank()) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(40.dp))
                } else {
                    AsyncImage(
                        model = product.imageUri,
                        contentDescription = "Product Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₹${"%.2f".format(product.price)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = ThemeMaroonPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(ThemeMaroonPrimary, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}