package com.katafract.parkarmor.ui

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.katafract.parkarmor.utils.LocationUtils
import com.katafract.parkarmor.viewmodel.MainViewModel
import com.katafract.parkarmor.viewmodel.Screen

@Composable
fun MapScreen(viewModel: MainViewModel) {
    val currentLocation by viewModel.currentLocation.collectAsState()
    val activeParking by viewModel.activeParking.collectAsState()
    val loadingLocation by viewModel.loadingLocation.collectAsState()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            currentLocation ?: LatLng(0.0, 0.0),
            15f
        )
    }

    LaunchedEffect(currentLocation) {
        currentLocation?.let { location ->
            cameraPositionState.animate(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.fromLatLngZoom(location, 15f)
                )
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            currentLocation?.let { current ->
                Marker(
                    position = current,
                    title = "Current Location",
                    snippet = "You are here"
                )
            }

            activeParking?.let { parking ->
                Marker(
                    position = LatLng(parking.latitude, parking.longitude),
                    title = "Parked Here",
                    snippet = parking.nickname ?: parking.address
                )
            }
        }

        // Loading indicator
        if (loadingLocation) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        // Navigation pill (if active parking exists)
        activeParking?.let { parking ->
            currentLocation?.let { current ->
                val distance = LocationUtils.haversineDistance(
                    current.latitude, current.longitude,
                    parking.latitude, parking.longitude
                )
                val bearing = LocationUtils.getBearing(
                    current.latitude, current.longitude,
                    parking.latitude, parking.longitude
                )

                NavigationPill(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp),
                    distance = distance,
                    bearing = bearing,
                    address = parking.address
                )
            }
        }

        // Save Parking FAB
        ExtendedFloatingActionButton(
            onClick = { viewModel.switchScreen(Screen.SAVE_CONFIRM) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            icon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
            text = { Text("Save Parking") },
            containerColor = MaterialTheme.colorScheme.primary
        )

        // Refresh button
        if (!loadingLocation) {
            ExtendedFloatingActionButton(
                onClick = { viewModel.refreshLocation() },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.secondary,
                icon = {},
                text = { Text("Refresh Location") }
            )
        }
    }
}

@Composable
fun NavigationPill(
    modifier: Modifier = Modifier,
    distance: Float,
    bearing: Float,
    address: String
) {
    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
            .fillMaxWidth(0.9f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Default.Navigation,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.padding(4.dp))
            Text(
                "${bearing.toInt()}° • ${LocationUtils.formatDistance(distance)}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            address,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
    }
}
