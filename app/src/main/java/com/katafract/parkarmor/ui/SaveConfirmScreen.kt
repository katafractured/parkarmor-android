package com.katafract.parkarmor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.katafract.parkarmor.viewmodel.MainViewModel
import com.katafract.parkarmor.viewmodel.Screen

@Composable
fun SaveConfirmScreen(viewModel: MainViewModel) {
    val currentLocation by viewModel.currentLocation.collectAsState()
    var nickname by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Save Parking Location",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Location Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                currentLocation?.let { location ->
                    Text(
                        "Latitude: ${String.format("%.6f", location.latitude)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "Longitude: ${String.format("%.6f", location.longitude)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        TextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("Nickname (optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        TextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes (optional)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            minLines = 3,
            maxLines = 5
        )

        Button(
            onClick = {
                viewModel.saveCurrentLocation(
                    nickname = nickname.ifBlank { null },
                    notes = notes
                )
                viewModel.switchScreen(Screen.ACTIVE_PARKING)
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(0.8f),
            enabled = currentLocation != null
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Text("Save Location")
        }

        OutlinedButton(
            onClick = { viewModel.switchScreen(Screen.MAP) },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(0.8f)
        ) {
            Text("Cancel")
        }
    }
}
