package com.katafract.parkarmor.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.katafract.parkarmor.viewmodel.MainViewModel
import com.katafract.parkarmor.viewmodel.Screen

@Composable
fun ActiveParkingScreen(viewModel: MainViewModel) {
    val activeParking by viewModel.activeParking.collectAsState()
    val activeTimer by viewModel.activeTimer.collectAsState()
    val isPro by viewModel.isPro.collectAsState()
    val context = LocalContext.current
    var showNicknameEdit by remember { mutableStateOf(false) }
    var nicknameInput by remember { mutableStateOf("") }
    var showTimerInput by remember { mutableStateOf(false) }
    var timerMinutes by remember { mutableStateOf("30") }
    var showNotesEdit by remember { mutableStateOf(false) }
    var notesInput by remember { mutableStateOf("") }

    if (activeParking == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val parking = activeParking!!
    nicknameInput = parking.nickname ?: ""
    notesInput = parking.notes

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "PARKARMOR",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        if (isPro) "Pro" else "Free",
                        modifier = Modifier.padding(8.dp, 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
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
                        IconButton(onClick = { showNicknameEdit = !showNicknameEdit }, modifier = Modifier.padding(0.dp)) {
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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.setNickname(nicknameInput)
                                    showNicknameEdit = false
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.width(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save")
                            }
                            OutlinedButton(
                                onClick = { showNicknameEdit = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel")
                            }
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
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (activeTimer != null) {
                        val timeRemaining = (activeTimer!!.expiresAt - System.currentTimeMillis()) / 1000 / 60
                        val hours = timeRemaining / 60
                        val mins = timeRemaining % 60
                        val timeText = if (hours > 0) {
                            "$hours h ${mins} m remaining"
                        } else {
                            "$mins m remaining"
                        }
                        Text(
                            timeText,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (timeRemaining > 30) Color(0xFF10B981) else Color(0xFFF59E0B)
                        )
                    } else {
                        Text(
                            "No timer set",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (!showTimerInput) {
                        Button(
                            onClick = { showTimerInput = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (activeTimer != null) "Update Timer" else "Set Timer")
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { timerMinutes = kotlin.math.max((timerMinutes.toIntOrNull() ?: 30) - 15, 15).toString() },
                                modifier = Modifier.weight(0.2f)
                            ) {
                                Text("-")
                            }
                            TextField(
                                value = timerMinutes,
                                onValueChange = { timerMinutes = it },
                                label = { Text("Minutes") },
                                modifier = Modifier.weight(0.6f),
                                singleLine = true
                            )
                            OutlinedButton(
                                onClick = { timerMinutes = kotlin.math.min((timerMinutes.toIntOrNull() ?: 30) + 15, 240).toString() },
                                modifier = Modifier.weight(0.2f)
                            ) {
                                Text("+")
                            }
                        }

                        // Preset buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(15, 30, 45, 60).forEach { preset ->
                                OutlinedButton(
                                    onClick = { timerMinutes = preset.toString() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("${preset}m")
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    timerMinutes.toIntOrNull()?.let { minutes ->
                                        viewModel.startTimer(minutes, context)
                                        showTimerInput = false
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Start")
                            }
                            OutlinedButton(
                                onClick = { showTimerInput = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel")
                            }
                        }
                    }

                    if (activeTimer != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { viewModel.stopTimer(context) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.width(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stop Timer")
                        }
                    }
                }
            }
        }

        item {
            Text(
                "Notes & Photos",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            if (!isPro) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Photos & Notes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Upgrade to Pro for unlimited photos and notes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
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
                                "Notes",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { showNotesEdit = !showNotesEdit },
                                modifier = Modifier.padding(0.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit notes")
                            }
                        }

                        if (showNotesEdit) {
                            TextField(
                                value = notesInput,
                                onValueChange = { notesInput = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                label = { Text("Add notes...") },
                                maxLines = 5
                            )
                            Button(
                                onClick = {
                                    viewModel.setNotes(notesInput)
                                    showNotesEdit = false
                                },
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .padding(top = 8.dp)
                            ) {
                                Text("Save")
                            }
                        } else {
                            Text(
                                notesInput.takeIf { it.isNotEmpty() } ?: "No notes yet",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (notesInput.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { /* Camera launcher will be added */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.width(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Photo")
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
                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.width(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Navigate")
                }

                OutlinedButton(
                    onClick = { viewModel.deactivateParking() },
                    modifier = Modifier.weight(1f)
                ) {
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
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.width(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Delete Parking")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
