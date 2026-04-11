package com.katafract.parkarmor.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.katafract.parkarmor.billing.BillingManager
import com.katafract.parkarmor.data.ParkingDatabase
import com.katafract.parkarmor.data.ParkingLocation
import com.katafract.parkarmor.data.ParkingTimer
import com.katafract.parkarmor.services.LocationService
import com.katafract.parkarmor.services.ParkingTimerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

enum class Screen {
    MAP,
    SAVE_CONFIRM,
    ACTIVE_PARKING,
    HISTORY
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val locationService = LocationService(application.applicationContext)
    private val database = ParkingDatabase.getInstance(application.applicationContext)
    private val dao = database.parkingDao()

    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation: StateFlow<LatLng?> = _currentLocation.asStateFlow()

    private val _currentScreen = MutableStateFlow<Screen>(Screen.MAP)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _activeParking = MutableStateFlow<ParkingLocation?>(null)
    val activeParking: StateFlow<ParkingLocation?> = _activeParking.asStateFlow()

    private val _allLocations = MutableStateFlow<List<ParkingLocation>>(emptyList())
    val allLocations: StateFlow<List<ParkingLocation>> = _allLocations.asStateFlow()

    private val _activeTimer = MutableStateFlow<ParkingTimer?>(null)
    val activeTimer: StateFlow<ParkingTimer?> = _activeTimer.asStateFlow()

    private val _loadingLocation = MutableStateFlow(false)
    val loadingLocation: StateFlow<Boolean> = _loadingLocation.asStateFlow()

    private val _currentAddress = MutableStateFlow("")
    val currentAddress: StateFlow<String> = _currentAddress.asStateFlow()

    private val _timerMinutes = MutableStateFlow(30)
    val timerMinutes: StateFlow<Int> = _timerMinutes.asStateFlow()

    private val _timerRunning = MutableStateFlow(false)
    val timerRunning: StateFlow<Boolean> = _timerRunning.asStateFlow()

    private val _isPro = MutableStateFlow(false)
    val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _capturedPhotoUri = MutableStateFlow<Uri?>(null)
    val capturedPhotoUri: StateFlow<Uri?> = _capturedPhotoUri.asStateFlow()

    private val billingManager = BillingManager(
        application.applicationContext,
        onPurchaseSuccess = { _, productId ->
            if (productId == BillingManager.PROD_PRO) {
                _isPro.value = true
            }
        },
        onPurchaseError = { error ->
            // Log error or show to user
        }
    )

    init {
        billingManager.connect()
        observeBillingStatus()
        refreshLocation()
        observeActiveParkingAndLocations()
    }

    private fun observeBillingStatus() {
        viewModelScope.launch {
            billingManager.isPro.collect { isPro ->
                _isPro.value = isPro
            }
        }
    }

    fun refreshLocation() {
        viewModelScope.launch {
            _loadingLocation.value = true
            try {
                val location = locationService.getCurrentLocation()
                _currentLocation.value = location
                if (location != null) {
                    val address = locationService.getAddressFromLocation(
                        location.latitude,
                        location.longitude
                    ) ?: "Unknown location"
                    _currentAddress.value = address
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _loadingLocation.value = false
            }
        }
    }

    fun saveCurrentLocation(nickname: String? = null, notes: String = "") {
        viewModelScope.launch {
            val location = _currentLocation.value ?: return@launch
            try {
                val address = locationService.getAddressFromLocation(
                    location.latitude,
                    location.longitude
                ) ?: ""

                val parkingLocation = ParkingLocation(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    address = address,
                    notes = notes,
                    nickname = nickname,
                    isActive = true
                )

                // Deactivate any previous active location
                val previousActive = dao.getActiveLocation().firstOrNull()
                if (previousActive != null) {
                    dao.updateLocation(previousActive.copy(isActive = false))
                }

                dao.insertLocation(parkingLocation)
                _currentScreen.value = Screen.ACTIVE_PARKING
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setNickname(nickname: String) {
        viewModelScope.launch {
            val parking = _activeParking.value ?: return@launch
            dao.updateLocation(parking.copy(nickname = nickname))
        }
    }

    fun startTimer(minutes: Int, context: android.content.Context) {
        viewModelScope.launch {
            val parking = _activeParking.value ?: return@launch
            val expiresAt = System.currentTimeMillis() + (minutes * 60 * 1000)
            val timer = ParkingTimer(
                locationId = parking.id,
                expiresAt = expiresAt,
                warningMinutes = 10,
                isActive = true
            )
            dao.insertTimer(timer)

            val intent = Intent(context, ParkingTimerService::class.java).apply {
                putExtra(ParkingTimerService.EXTRA_DURATION_MINUTES, minutes)
                putExtra(ParkingTimerService.EXTRA_ADDRESS, parking.address)
                putExtra(ParkingTimerService.EXTRA_LOCATION_ID, parking.id)
            }
            context.startService(intent)
            _timerRunning.value = true
        }
    }

    fun stopTimer(context: android.content.Context) {
        viewModelScope.launch {
            val parking = _activeParking.value ?: return@launch
            context.stopService(Intent(context, ParkingTimerService::class.java))
            dao.deleteTimerForLocation(parking.id)
            _timerRunning.value = false
        }
    }

    fun deactivateParking() {
        viewModelScope.launch {
            val parking = _activeParking.value ?: return@launch
            dao.updateLocation(parking.copy(isActive = false))
            dao.deleteTimerForLocation(parking.id)
            _currentScreen.value = Screen.MAP
            _timerRunning.value = false
        }
    }

    fun deleteLocation(id: String) {
        viewModelScope.launch {
            val location = _allLocations.value.find { it.id == id } ?: return@launch
            dao.deleteLocation(location)
            dao.deleteTimerForLocation(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            _allLocations.value.filter { !it.isActive }.forEach { location ->
                dao.deleteLocation(location)
            }
        }
    }

    fun loadHistory() {
        viewModelScope.launch {
            dao.getAllLocations().collect { locations ->
                _allLocations.value = locations
            }
        }
    }

    fun switchScreen(screen: Screen) {
        _currentScreen.value = screen
    }

    fun launchBilling(activity: Activity) {
        billingManager.launchPurchase(activity, BillingManager.PROD_PRO)
    }

    fun restorePurchases() {
        billingManager.restorePurchases()
    }

    fun setNotes(notes: String) {
        _notes.value = notes
    }

    fun setCapturedPhoto(uri: Uri?) {
        _capturedPhotoUri.value = uri
    }

    fun setTimerMinutes(minutes: Int) {
        _timerMinutes.value = minutes
    }

    private fun observeActiveParkingAndLocations() {
        viewModelScope.launch {
            dao.getActiveLocation().collect { activeLocation ->
                _activeParking.value = activeLocation
                if (activeLocation != null) {
                    dao.getTimerForLocation(activeLocation.id).collect { timer ->
                        _activeTimer.value = timer
                        _timerRunning.value = timer != null && timer.isActive
                    }
                }
            }
        }

        viewModelScope.launch {
            dao.getAllLocations().collect { locations ->
                _allLocations.value = locations
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        billingManager.disconnect()
    }
}
