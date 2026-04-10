package com.katafract.parkarmor.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
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
    private val timerService = ParkingTimerService()

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

    init {
        refreshLocation()
        observeActiveParkingAndLocations()
    }

    fun refreshLocation() {
        viewModelScope.launch {
            _loadingLocation.value = true
            try {
                val location = locationService.getCurrentLocation()
                _currentLocation.value = location
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
                val address = locationService.getAddressFromLocation(location.latitude, location.longitude) ?: ""

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

    fun setParkingTimer(minutes: Int) {
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
            timerService.setTimer(
                getApplication(),
                parking.id,
                expiresAt,
                10
            )
        }
    }

    fun deactivateParking() {
        viewModelScope.launch {
            val parking = _activeParking.value ?: return@launch
            dao.updateLocation(parking.copy(isActive = false))
            timerService.cancelTimer(getApplication(), parking.id)
            dao.deleteTimerForLocation(parking.id)
            _currentScreen.value = Screen.MAP
        }
    }

    fun deleteLocation(id: String) {
        viewModelScope.launch {
            val location = _allLocations.value.find { it.id == id } ?: return@launch
            dao.deleteLocation(location)
            timerService.cancelTimer(getApplication(), id)
            dao.deleteTimerForLocation(id)
        }
    }

    fun switchScreen(screen: Screen) {
        _currentScreen.value = screen
    }

    private fun observeActiveParkingAndLocations() {
        viewModelScope.launch {
            dao.getActiveLocation().collect { activeLocation ->
                _activeParking.value = activeLocation
                if (activeLocation != null) {
                    dao.getTimerForLocation(activeLocation.id).collect { timer ->
                        _activeTimer.value = timer
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
}
