package com.example.posapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.posapp.R
import com.example.posapp.ui.navigation.Screen

data class MenuItem(val iconRes: Int, val title: String, val route: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuGridBottomSheet(
    navController: NavController,
    onDismissRequest: () -> Unit
) {
    // Exactly 6 items: 3 columns x 2 rows creates a perfectly balanced grid with zero truncation!
    val menuItems = listOf(
        MenuItem(R.drawable.ic_menu_orders, "Orders", Screen.OrdersHistory.route),
        MenuItem(R.drawable.ic_menu_products, "Products", Screen.Products.route),
        MenuItem(R.drawable.ic_menu_categories, "Categories", Screen.Categories.route), 
        MenuItem(R.drawable.ic_menu_cash, "Cash & Expenses", Screen.CashManagement.route),
        MenuItem(R.drawable.ic_menu_tax, "Tax", Screen.Taxes.route),
        MenuItem(R.drawable.ic_menu_settings, "Settings", Screen.Settings.route)
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp, top = 8.dp)
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Menu",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(3), // 3 columns for 6 items = 2 clean, spacious rows
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(menuItems) { item ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDismissRequest()
                                navController.navigate(item.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = item.iconRes),
                            contentDescription = item.title,
                            modifier = Modifier.size(46.dp) // Generous, clear icon size
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            maxLines = 2, // Allows "Cash & Expenses" to wrap gracefully without cutting off
                            minLines = 2
                        )
                    }
                }
            }
        }
    }
}