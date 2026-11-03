package com.example.posapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.posapp.ui.screens.*
import com.example.posapp.ui.PosViewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard") // Used as Business Analytics
    object Home : Screen("home") // Used as Sale / POS Terminal
    object Checkout : Screen("checkout")
    object Products : Screen("products")
    object ProductEdit : Screen("product_edit/{productId}") {
        fun createRoute(productId: Long) = "product_edit/$productId"
    }
    object Categories : Screen("categories")
    object CategoryEdit : Screen("category_edit/{categoryId}") {
        fun createRoute(categoryId: Long) = "category_edit/$categoryId"
    }
    object OrdersHistory : Screen("orders_history")
    object CashManagement : Screen("cash_management")
    object Inventory : Screen("inventory")
    object Reports : Screen("reports")
    object Settings : Screen("settings")
    object PrinterSettings : Screen("printer_settings")
    object ProfileSettings : Screen("profile_settings")
    object BackupRestore : Screen("backup_restore")
    object Taxes : Screen("taxes")
    object TaxEdit : Screen("tax_edit/{taxId}") {
        fun createRoute(taxId: Long) = "tax_edit/$taxId"
    }
}

@Composable
fun AppNavigation(viewModel: PosViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(
                viewModel = viewModel,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = viewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(viewModel = viewModel, navController = navController)
        }
        composable(Screen.Home.route) {
            PosHomeScreen(viewModel = viewModel, navController = navController)
        }
        composable(Screen.Checkout.route) {
            CheckoutScreen(viewModel = viewModel, navController = navController)
        }
        composable(Screen.Products.route) {
            ProductsScreen(viewModel = viewModel, navController = navController)
        }
        composable(
            route = Screen.ProductEdit.route,
            arguments = listOf(navArgument("productId") { type = NavType.LongType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getLong("productId") ?: 0L
            ProductEditScreen(viewModel = viewModel, navController = navController, productId = productId)
        }
        composable(Screen.Categories.route) {
            CategoriesScreen(viewModel = viewModel, navController = navController)
        }
        composable(
            route = Screen.CategoryEdit.route,
            arguments = listOf(navArgument("categoryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getLong("categoryId") ?: 0L
            CategoryEditScreen(viewModel = viewModel, navController = navController, categoryId = categoryId)
        }
        composable(Screen.OrdersHistory.route) {
            OrdersHistoryScreen(viewModel = viewModel, navController = navController)
        }
        composable(Screen.CashManagement.route) {
            CashManagementScreen(viewModel = viewModel, navController = navController)
        }
        composable(Screen.Inventory.route) {
            InventoryScreen(viewModel = viewModel, navController = navController)
        }
        composable(Screen.Reports.route) {
            ReportsScreen(viewModel = viewModel, navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(viewModel = viewModel, navController = navController)
        }
        composable(Screen.PrinterSettings.route) {
            PrinterSettingsScreen(viewModel = viewModel, navController = navController)
        }
        composable(Screen.ProfileSettings.route) {
            ProfileSettingsScreen(viewModel = viewModel, navController = navController)
        }
        composable(Screen.BackupRestore.route) {
            BackupRestoreScreen(navController = navController)
        }
        composable(Screen.Taxes.route) {
            TaxListScreen(viewModel = viewModel, navController = navController)
        }
        composable(
            route = Screen.TaxEdit.route,
            arguments = listOf(navArgument("taxId") { type = NavType.LongType })
        ) { backStackEntry ->
            val taxId = backStackEntry.arguments?.getLong("taxId") ?: 0L
            TaxEditScreen(viewModel = viewModel, navController = navController, taxId = taxId)
        }
    }
}