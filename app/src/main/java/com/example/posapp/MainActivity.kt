package com.example.posapp

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.posapp.data.AppDatabase
import com.example.posapp.data.PosRepository
import com.example.posapp.ui.PosViewModel
import com.example.posapp.ui.PosViewModelFactory
import com.example.posapp.ui.navigation.AppNavigation
import com.example.posapp.ui.theme.POSAPPTheme
import com.example.posapp.utils.AutoBackupWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Force the app to ALWAYS stay in Light Mode and ignore the system's Dark Mode settings
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        
        val database = AppDatabase.getDatabase(this)
        val sharedPrefs = getSharedPreferences("pos_prefs", MODE_PRIVATE)
        val repository = PosRepository(database.posDao(), database.userDao(), database.taxDao(), sharedPrefs)
        
        // Explicitly set the status bar and navigation bar to transparent
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            )
        )

        // Schedule Automatic Periodic 30-Minute ZIP Database Backup to Documents/POSAPP/Autobackup
        try {
            val autoBackupWork = PeriodicWorkRequestBuilder<AutoBackupWorker>(30, TimeUnit.MINUTES)
                .setConstraints(Constraints.NONE)
                .build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "posapp_auto_backup",
                ExistingPeriodicWorkPolicy.KEEP,
                autoBackupWork
            )

            // Trigger an initial backup in background on launch
            Thread {
                AutoBackupWorker.createDatabaseZipBackup(this)
            }.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        setContent {
            POSAPPTheme {
                val viewModel: PosViewModel = viewModel(factory = PosViewModelFactory(repository))
                
                Box(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}