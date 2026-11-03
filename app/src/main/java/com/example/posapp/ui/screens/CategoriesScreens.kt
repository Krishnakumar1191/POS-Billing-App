package com.example.posapp.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Category
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.posapp.data.entities.Category
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
fun CategoriesScreen(viewModel: PosViewModel, navController: NavController) {
    val categories by viewModel.categories.collectAsState()
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
                    title = { Text("Categories", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.onBackground) },
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
                    onClick = { navController.navigate(Screen.CategoryEdit.createRoute(0L)) },
                    modifier = Modifier.padding(bottom = 80.dp),
                    containerColor = ThemeMaroonPrimary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Category")
                }
            },
            bottomBar = {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SharedBottomBar(
                        navController = navController,
                        currentRoute = Screen.Categories.route,
                        onMenuClick = { showMenuGrid = true }
                    )
                }
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { padding ->
            if (categories.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No categories found", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(categories) { category ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate(Screen.CategoryEdit.createRoute(category.id)) }
                                .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.05f)),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(64.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (category.imageUri.isNullOrBlank()) {
                                        Icon(Icons.Default.Category, contentDescription = null, tint = ThemeMaroonPrimary)
                                    } else {
                                        AsyncImage(
                                            model = category.imageUri,
                                            contentDescription = "Category Image",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { viewModel.deleteCategory(category.id) }) {
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
fun CategoryEditScreen(viewModel: PosViewModel, navController: NavController, categoryId: Long) {
    var name by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imageUri = it.toString() }
    }

    LaunchedEffect(categoryId) {
        if (categoryId > 0) {
            viewModel.getCategoryById(categoryId)?.let { category ->
                name = category.name
                imageUri = category.imageUri
            }
        }
    }

    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(if (categoryId == 0L) "New Category" else "Edit Category", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.onBackground) },
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
                // Section: Category Image Upload
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
                                contentDescription = "Category Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp))
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth().shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Category Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ThemeMaroonPrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Category Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ThemeMaroonPrimary,
                                focusedLabelColor = ThemeMaroonPrimary
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    // Make a permanent local copy of the image if it's a content URI
                                    var finalImageUri = imageUri
                                    if (finalImageUri != null && finalImageUri!!.startsWith("content://")) {
                                        try {
                                            val inputStream = context.contentResolver.openInputStream(Uri.parse(finalImageUri))
                                            if (inputStream != null) {
                                                val file = File(context.filesDir, "cat_img_${System.currentTimeMillis()}.jpg")
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

                                    val newCategory = Category(
                                        id = categoryId,
                                        name = name,
                                        imageUri = finalImageUri
                                    )
                                    if (categoryId == 0L) {
                                        viewModel.insertCategory(newCategory) { navController.navigateUp() }
                                    } else {
                                        viewModel.updateCategory(newCategory) { navController.navigateUp() }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ThemeMaroonPrimary)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Category", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}