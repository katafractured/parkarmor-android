package com.katafract.parkarmor

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.katafract.parkarmor.ui.ActiveParkingScreen
import com.katafract.parkarmor.ui.HistoryScreen
import com.katafract.parkarmor.ui.MapScreen
import com.katafract.parkarmor.ui.SaveConfirmScreen
import com.katafract.parkarmor.ui.theme.ParkArmorTheme
import com.katafract.parkarmor.viewmodel.MainViewModel
import com.katafract.parkarmor.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ParkArmorTheme {
                val viewModel: MainViewModel = viewModel()
                RequestLocationPermissions()
                MainScreen(viewModel)
            }
        }
    }

    @Composable
    private fun RequestLocationPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { }

        LaunchedEffect(Unit) {
            val missingPermissions = permissions.filter {
                ContextCompat.checkSelfPermission(this@MainActivity, it) != PackageManager.PERMISSION_GRANTED
            }
            if (missingPermissions.isNotEmpty()) {
                launcher.launch(missingPermissions.toTypedArray())
            }
        }
    }
}

@Composable
private fun MainScreen(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val activeParking by viewModel.activeParking.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Map") },
                    label = { Text("Map") },
                    selected = currentScreen == Screen.MAP,
                    onClick = { viewModel.switchScreen(Screen.MAP) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History") },
                    selected = currentScreen == Screen.HISTORY,
                    onClick = { viewModel.switchScreen(Screen.HISTORY) }
                )
                if (activeParking != null) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Parked") },
                        label = { Text("Parked") },
                        selected = currentScreen == Screen.ACTIVE_PARKING,
                        onClick = { viewModel.switchScreen(Screen.ACTIVE_PARKING) }
                    )
                }
            }
        }
    ) { innerPadding ->
        when (currentScreen) {
            Screen.MAP -> MapScreen(viewModel = viewModel)
            Screen.SAVE_CONFIRM -> SaveConfirmScreen(viewModel = viewModel)
            Screen.ACTIVE_PARKING -> ActiveParkingScreen(viewModel = viewModel)
            Screen.HISTORY -> HistoryScreen(viewModel = viewModel)
        }
    }
}
