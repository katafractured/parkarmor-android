package com.katafract.parkarmor.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.katafract.parkarmor.viewmodel.MainViewModel
import com.katafract.parkarmor.viewmodel.Screen

@Composable
fun ActiveParkingScreen(viewModel: MainViewModel) {
    val activeParking by viewModel.activeParking.collectAsState()
    val activeTimer by viewModel.activeTimer.collectAsState()
    val context = LocalContext.current
    var showNicknameEdit by remember { mutableStateOf(false) }
    var nicknameInput by remember { mutableStateOf("") }
    var showTimerInput by remember { mutableStateOf(false) }
    var timerMinutes by remember { mutableStateOf("30") }

    if (activeParking == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val parking = activeParking!!
    nicknameInput = parking.nickname ?: ""

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Parked Location",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            parking.nickname?.takeIf { it.isNotBlank() } ?: "Unnamed Parking",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { showNicknameEdit = !showNicknameEdit }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit nickname")
                        }
                    }

                    if (showNicknameEdit) {
                        TextField(
                            value = nicknameInput,
                            onValueChange = { nicknameInput = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            label = { Text("Nickname") },
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                viewModel.setNickname(nicknameInput)
                                showNicknameEdit = false
                            },
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(top = 8.dp)
                        ) {
                            Text("Save")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text(
                        parking.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Parked: " + SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                            .format(Date(parking.savedAt)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Text(
                "Parking Timer",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (activeTimer != null) {
                        val timeRemaining = (activeTimer!!.expiresAt - System.currentTimeMillis()) / 1000 / 60
                        Text(
                            "Time Remaining: ${if (timeRemaining > 0) "${timeRemaining}m" else "Expired"}",
                            style = MaterialTheme.typography.titleMedium
                        )
                    } else {
                        Text(
                            "No timer set",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (!showTimerInput) {
                        Button(
                            onClick = { showTimerInput = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (activeTimer != null) "Update Timer" else "Set Timer")
                        }
                    } else {
                        TextField(
                            value = timerMinutes,
                            onValueChange = { timerMinutes = it },
                            label = { Text("Minutes") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            singleLine = true
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    timerMinutes.toIntOrNull()?.let { minutes ->
                                        viewModel.setParkingTimer(minutes)
                                        showTimerInput = false
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Set")
                            }
                            OutlinedButton(
                                onClick = { showTimerInput = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel")
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val gmmIntentUri = Uri.parse("geo:${parking.latitude},${parking.longitude}?q=${Uri.encode(parking.address)}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                            setPackage("com.google.android.apps.maps")
                        }
                        context.startActivity(mapIntent)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Directions")
                }

                Button(
                    onClick = { viewModel.deactivateParking() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Home, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Found Car")
                }
            }
        }

        item {
            OutlinedButton(
                onClick = {
                    viewModel.deleteLocation(parking.id)
                    viewModel.switchScreen(Screen.MAP)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Delete")
            }
        }
    }
}
