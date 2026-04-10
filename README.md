# ParkArmor — Android Edition

A privacy-first parking location saver app for Android. Save your parked car location with one tap, get directions back, set parking meter reminders.

## Features

- **One-Tap Save** — Save current GPS location as parked spot
- **Map View** — See where you parked on Google Maps
- **Address Reverse Geocoding** — Automatic address lookup
- **Parking Timer** — Optional meter countdown with notifications
- **Walk-Back Navigation** — Direction + distance to parked car
- **History** — View past parking sessions
- **Photos** — Attach photos to parking spots (future)

## Architecture

- **MVVM + Jetpack Compose** — Modern Android UI pattern
- **Room Database** — Local storage for parking locations & timers
- **Google Play Services** — Location & Maps APIs
- **Kotlin Coroutines** — Async operations
- **Material Design 3** — Blue theme (#3B82F6)

## Build Requirements

- Android SDK 35 (compileSdk)
- Min SDK 26 (API level 26 — Android 8.0)
- Java 11
- Gradle 8.7

## Setup

### Environment Variables

Set these before building:

```bash
export MAPS_API_KEY="your-google-maps-api-key"
export KEYSTORE_PATH="/path/to/parkarmor.keystore"
export KEYSTORE_PASSWORD="keystore-password"
export KEY_ALIAS="parkarmor-key"
export KEY_PASSWORD="key-password"
```

### Build APK

```bash
./gradlew assembleDebug      # Debug APK
./gradlew assembleRelease    # Release APK (requires signing config)
```

### Build App Bundle (Play Store)

```bash
./gradlew bundleRelease
```

## Permissions

- `ACCESS_FINE_LOCATION` — GPS location (required)
- `ACCESS_COARSE_LOCATION` — Network-based location
- `ACCESS_BACKGROUND_LOCATION` — Background location (Android 10+)
- `CAMERA` — Take parking spot photos
- `POST_NOTIFICATIONS` — Timer expiry notifications
- `FOREGROUND_SERVICE` — Timer background service
- `VIBRATE` — Haptic feedback

## Project Structure

```
app/
├── build.gradle.kts                     # App build config
├── src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/katafract/parkarmor/
│   │   ├── MainActivity.kt              # Main entry point
│   │   ├── data/
│   │   │   ├── ParkingLocation.kt       # Room entity
│   │   │   ├── ParkingTimer.kt          # Timer entity
│   │   │   ├── ParkingDao.kt            # Room DAO
│   │   │   └── ParkingDatabase.kt       # Room database
│   │   ├── services/
│   │   │   ├── LocationService.kt       # Geolocation & geocoding
│   │   │   └── ParkingTimerService.kt   # Foreground service
│   │   ├── ui/
│   │   │   ├── MapScreen.kt             # Main map + save FAB
│   │   │   ├── ActiveParkingScreen.kt   # Current session
│   │   │   ├── HistoryScreen.kt         # Past sessions
│   │   │   ├── SaveConfirmScreen.kt     # Confirm save dialog
│   │   │   └── theme/Theme.kt           # Blue theme
│   │   ├── utils/
│   │   │   └── LocationUtils.kt         # Haversine, bearing, formatting
│   │   └── viewmodel/
│   │       └── MainViewModel.kt         # MVVM state management
│   └── res/
│       ├── values/
│       │   ├── strings.xml
│       │   ├── colors.xml
│       │   ├── themes.xml
│       │   └── attrs.xml
│       └── xml/
│           ├── backup_rules.xml
│           └── data_extraction_rules.xml
├── proguard-rules.pro                   # Obfuscation rules
└── .github/
    └── workflows/
        └── release.yml                  # GitHub Actions build

gradle/
└── wrapper/
    └── gradle-wrapper.properties        # Gradle 8.7

settings.gradle.kts                      # Root settings
build.gradle.kts                         # Root build config
gradle.properties                        # Gradle properties
gradle/libs.versions.toml               # Dependency versions
```

## Version Bumping

Use the bump script to increment version code and create a git tag:

```bash
scripts/bump 1.0.1
git push origin main
git push origin v1.0.1
```

## GitHub Secrets

For automated Play Store releases, set these GitHub Secrets:

- `MAPS_API_KEY` — Google Maps API key
- `KEYSTORE_PATH` — Path to signing keystore (base64-encoded)
- `KEYSTORE_PASSWORD` — Keystore password
- `KEY_ALIAS` — Key alias in keystore
- `KEY_PASSWORD` — Key password

## Future

- CameraX integration for spot photos
- Expiration time-lapse videos
- Multi-car support (multiple active parkings)
- Integration with Enclave VPN for location privacy
- Apple CarPlay widget (native iOS)

## License

Proprietary — Katafract LLC
