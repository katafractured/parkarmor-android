# ParkArmor Android — Architecture Guide

## Overview

ParkArmor is a location-based parking app using **MVVM + Jetpack Compose** with **Room persistence** and **Google Play Services**.

## Core Components

### Data Layer (Room)

**Entities:**
- `ParkingLocation` — Saved parking spot with lat/lon, address, timestamp, active status
- `ParkingTimer` — Optional countdown timer linked to a location

**DAO** (`ParkingDao`):
- CRUD for locations and timers
- Flows for reactive updates

**Database** (`ParkingDatabase`):
- Singleton pattern for safe access
- Single instance across app lifetime

### Service Layer

**LocationService** (`LocationService.kt`):
- Uses `FusedLocationProviderClient` for GPS (high-accuracy priority)
- Reverse geocodes coordinates → human-readable addresses via `Geocoder`
- Suspendable functions for coroutine compatibility

**ParkingTimerService** (`ParkingTimerService.kt`):
- Android foreground service for persistent notifications
- Integrates `AlarmManager` for meter expiry alerts
- Posts notification on timer expiry (configurable warning minutes)

### ViewModel

**MainViewModel** (`MainViewModel.kt`):
- Manages UI state (current location, active parking, history)
- Orchestrates data layer via DAO
- Handles permission flows and location updates
- Screen enum: MAP, SAVE_CONFIRM, ACTIVE_PARKING, HISTORY

**State Flows:**
- `currentLocation` — device GPS location (nullable)
- `currentScreen` — active UI screen
- `activeParking` — current parking session (nullable)
- `allLocations` — all saved parkings (sorted by date)
- `activeTimer` — countdown timer for active parking
- `loadingLocation` — location acquisition in progress

### UI Layer (Compose)

**Screens:**
1. **MapScreen** — Primary interface
   - Google Maps with user location + parked car marker
   - "Save Parking" FAB for new parking
   - Navigation pill showing bearing + distance to parked car (if active)
   - Refresh button for GPS

2. **SaveConfirmScreen** — Confirmation dialog
   - Shows lat/lon before save
   - Optional nickname + notes
   - Cancel or confirm

3. **ActiveParkingScreen** — Current session
   - Address + timestamp
   - Editable nickname
   - Timer management
   - "Get Directions" → Google Maps intent
   - "Found Car" deactivation button
   - Delete option

4. **HistoryScreen** — Past parkings
   - List of inactive locations sorted by date
   - Tap for details (future: map view)
   - Delete individual entries

**Bottom Navigation:**
- Dynamically shows "Parked" tab only when active parking exists

**Theme** (`ui/theme/Theme.kt`):
- Primary: #3B82F6 (Blue)
- Dark mode support
- Material Design 3

### Utilities

**LocationUtils** (`utils/LocationUtils.kt`):
- `haversineDistance(lat1, lon1, lat2, lon2)` → meters
- `getBearing(lat1, lon1, lat2, lon2)` → degrees (0-360, 0=North)
- `formatDistance(meters)` → "250 m" or "1.23 km"

## Data Flow

### Saving a Parking Spot

1. User taps "Save Parking" on MapScreen
2. Switch to SaveConfirmScreen
3. User confirms → `viewModel.saveCurrentLocation(nickname, notes)`
4. ViewModel calls `LocationService.getAddressFromLocation()`
5. Create `ParkingLocation` entity
6. Deactivate any previous active parking
7. Insert into Room via DAO
8. Switch to ActiveParkingScreen

### Setting a Timer

1. User enters minutes on ActiveParkingScreen
2. Call `viewModel.setParkingTimer(minutes)`
3. Calculate `expiresAt = now + (minutes * 60s)`
4. Insert `ParkingTimer` entity
5. Schedule AlarmManager callback via ParkingTimerService
6. Timer flow updates UI with countdown

### Deactivating (Found Car)

1. User taps "Found Car"
2. Call `viewModel.deactivateParking()`
3. Update location `isActive = false`
4. Cancel AlarmManager for that timer
5. Delete timer from Room
6. Revert to MapScreen

## Navigation Model

- **Bottom Bar** — Maps, History, + conditional Parked tab
- **Screen Enum** — Controls which composable renders
- **No Fragment Back Stack** — Simple state machine (MAP ↔ SAVE_CONFIRM ↔ ACTIVE_PARKING ↔ HISTORY)

## Permissions & Safety

- **Runtime Permissions** — Requested at MainActivity init
- **Graceful Degradation** — If location denied, `currentLocation` stays null
- **Background Location** — Only on Android 10+ (in manifest + request)
- **Notification** — Requires POST_NOTIFICATIONS on Android 13+
- **Foreground Service** — Timer service includes notification + foreground type

## Testing

*Future:*
- Unit tests for LocationUtils (Haversine, bearing calculations)
- Integration tests for Room DAO
- UI tests for Compose screens (Espresso → modern Compose test API)

## Performance Considerations

- **Location Updates** — Only on refresh button tap (no polling)
- **Database** — Indexed by `locationId` for quick timer lookups
- **Compose Recomposition** — StateFlow only recomposes affected screens
- **Room Pagination** — History loads all records (future: pagination)
- **AlarmManager** — `setExactAndAllowWhileIdle()` for timer accuracy

## Dependencies

See `gradle/libs.versions.toml` for versions. Key libs:

- `androidx.activity:activity-compose` — Compose integration
- `androidx.lifecycle:lifecycle-viewmodel-compose` — ViewModel in Compose
- `androidx.room:room-*` — Persistence
- `com.google.android.gms:play-services-location` — GPS
- `com.google.android.gms:play-services-maps` — Google Maps
- `com.google.maps.android:maps-compose` — Maps Compose wrapper
- `androidx.camera:camera-*` — CameraX (prepared for future photo feature)
- `io.coil-kt:coil-compose` — Image loading

## BuildConfig Integration

Maps API key injected at build time:

```kotlin
buildConfigField("String", "MAPS_API_KEY", "\"${System.getenv("MAPS_API_KEY") ?: ""}\"")
manifestPlaceholders["MAPS_API_KEY"] = System.getenv("MAPS_API_KEY") ?: ""
```

Allows dynamic API key per build variant (debug vs release).

## Known Limitations

- **Address Lookup** — Uses deprecated Geocoder (modern alternative: Google Geocoding API requires network calls)
- **Photos** — Placeholder only, CameraX not integrated
- **Offline Mode** — Requires network for initial address lookup and map tiles
- **Single Active Parking** — Only one parked location at a time
- **No Cloud Sync** — Parking history local-only

## Future Enhancements

- CameraX full integration with photo gallery
- Multi-car parking (multiple active + inactive locations)
- Cloud backup to Enclave object storage
- Integration with WraithVPN for location masking
- Apple Watch companion app
- Share parking location with contacts
