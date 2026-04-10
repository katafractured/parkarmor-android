# ParkArmor Android — Compilation Checklist

## Pre-Build Verification

### Environment Variables
```bash
# Set these before running any gradle command:
export MAPS_API_KEY="AIzaSyD..."        # Google Maps API key
export KEYSTORE_PATH="/path/to.jks"    # (optional, only needed for release builds)
export KEYSTORE_PASSWORD="password"     # (optional, only needed for release builds)
export KEY_ALIAS="parkarmor"            # (optional, only needed for release builds)
export KEY_PASSWORD="password"          # (optional, only needed for release builds)
```

### Check Gradle Wrapper
```bash
cd /home/artemis/dev/parkarmor-android
ls -la gradle/wrapper/gradle-wrapper.properties
# Should output gradle-wrapper.properties → Gradle 8.7
```

### First Build
```bash
./gradlew clean build --info
```

## Kotlin Compilation Checks

### KSP Processing (Room)
- [ ] Room entities compile without errors
- [ ] Room DAO methods recognized
- [ ] Room database singleton builds
- [ ] Generated code in `build/generated/ksp/debug/`

### Compose Validation
- [ ] All @Composable functions recognized
- [ ] No preview errors
- [ ] Modifier chains valid
- [ ] Navigation state machine compiles

### Coroutines & Flow
- [ ] Suspend functions resolve
- [ ] StateFlow declarations valid
- [ ] LaunchedEffect blocks recognized
- [ ] Lifecycle scope compatible

## Gradle Dependency Resolution

### Compose BOM
```bash
./gradlew dependencies --configuration debugCompileClasspath | grep "compose"
```
Should show:
- compose-ui
- compose-material3
- compose-foundation
- compose-runtime

### Play Services
```bash
./gradlew dependencies --configuration debugCompileClasspath | grep "gms"
```
Should show:
- play-services-location:21.3.0
- play-services-maps:19.0.0
- maps-compose:4.4.1

### Room
```bash
./gradlew dependencies --configuration debugCompileClasspath | grep "room"
```
Should show:
- room-runtime:2.6.1
- room-ktx:2.6.1

## Resource Compilation

### AndroidManifest.xml
- [ ] All permissions declared
- [ ] API key meta-data present
- [ ] Service declaration correct
- [ ] Main activity exported

### Resources
- [ ] strings.xml has all keys
- [ ] colors.xml has primary colors
- [ ] themes.xml inherits Material3
- [ ] backup_rules.xml valid XML

## Build Variants

### Debug Build
```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Release Build (with signing)
```bash
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
# Requires keystore environment variables
```

### App Bundle (Play Store)
```bash
./gradlew bundleRelease
# Output: app/build/outputs/bundle/release/app-release.aab
```

## ProGuard Verification

### Release Build Optimization
- [ ] ProGuard rules apply correctly
- [ ] Room classes kept (not obfuscated)
- [ ] Google Maps API preserved
- [ ] Play Services unobfuscated
- [ ] Custom DAO interfaces kept

```bash
./gradlew assembleRelease --info 2>&1 | grep -i "proguard\|shrink"
```

## Lint Checks

```bash
./gradlew lint
# Check: app/build/reports/lint-results.html
```

Expected warnings (can be ignored):
- NewApi (Material3 requires API 31+, but we target 35)
- UnusedResources (some colors/strings for future use)

Critical errors (must fix):
- None expected in this scaffold

## APK Analysis

After successful debug build:

```bash
# Check APK content
unzip -l app/build/outputs/apk/debug/app-debug.apk | grep "com/katafract"

# Verify manifest
unzip -p app/build/outputs/apk/debug/app-debug.apk AndroidManifest.xml | grep -i "parkarmor\|location\|maps"
```

Expected files:
- classes.dex (bytecode)
- resources.pb (resources)
- AndroidManifest.xml

Expected Kotlin classes:
- MainActivity
- MainViewModel
- ParkingLocation, ParkingTimer, ParkingDao, ParkingDatabase
- LocationService, ParkingTimerService
- MapScreen, ActiveParkingScreen, HistoryScreen, SaveConfirmScreen
- ParkArmorTheme

## Common Compilation Errors & Fixes

### Error: "Unresolved reference: libs.plugins.*"
**Cause:** libs.versions.toml not found or malformed
**Fix:** Ensure `gradle/libs.versions.toml` exists with correct format

### Error: "Cannot resolve symbol 'Maps' or 'Composable'"
**Cause:** Missing imports in gradle dependencies
**Fix:** Check that maps-compose and Compose BOM are in app/build.gradle.kts

### Error: "Cannot access 'ParkingDao': it is package-private"
**Cause:** Room DAO missing @Dao annotation
**Fix:** Verify ParkingDao has `@Dao interface` declaration

### Error: "Key 'MAPS_API_KEY' not found"
**Cause:** Environment variable not set
**Fix:** `export MAPS_API_KEY="your-key"` before gradle invocation

### Error: "Signing key not found"
**Cause:** KEYSTORE_PATH env var missing for release build
**Fix:** Debug builds don't need signing; use `assembleDebug` or set keystore vars

## IDE Integration (Android Studio)

1. Open `/home/artemis/dev/parkarmor-android` as project root
2. Android Studio auto-configures Gradle
3. Wait for indexing & Gradle sync to complete
4. Build > Make Project (Ctrl+F9)
5. Run > Run 'app' (Shift+F10) to deploy to emulator/device

### Emulator Requirements
- API level ≥ 26 (Android 8.0)
- Google Play Services installed (for Maps & Location)
- GPS/Location mock enabled

## Verification Checklist

- [ ] `./gradlew clean build` completes without errors
- [ ] Debug APK generated: `app/build/outputs/apk/debug/app-debug.apk`
- [ ] Manifest validates with no permission warnings
- [ ] Lint report has no critical errors
- [ ] ProGuard optimization succeeds (release build)
- [ ] All Kotlin classes compile
- [ ] Room DAO methods resolved
- [ ] Compose screens render (no preview errors)
- [ ] Google Maps key injected into manifest
- [ ] Material3 theme recognized
- [ ] Bottom navigation components build

## Next: Deploy to Device/Emulator

```bash
# Install debug APK
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Or via Android Studio:
# Run > Run 'app' → select device
```

---

**Expected Result:** ParkArmor app launches with Map screen, "Save Parking" FAB, and empty history tab.
