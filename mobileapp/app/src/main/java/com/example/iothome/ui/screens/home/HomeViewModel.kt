package com.example.iothome.ui.screens.home

import androidx.lifecycle.viewModelScope
import com.example.iothome.data.model.* // Yeni modelleri (Device, HomeUiState, WeatherState) import edin
import com.example.iothome.data.mqtt.MqttRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.update

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MqttRepository(application)

    // YENİ UI STATE: Tüm Dashboard'u besler
    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Mock Cihaz Listesi (Görseldeki 3 cihazı içerir)
    private fun getInitialDevices(): List<Device> {
        return listOf(
            // LIGHT: MQTT ile bağlı olan lamba
            Device(id = "salon-ayakli-lamba-1", name = "Light", type = DeviceType.LIGHT, roomId = "lroom", isOn = false, isActuator = true),
            // THERMOSTAT: Sadece gösterim ve toggle için mock
            Device(id = "thermo-main", name = "Thermostat", type = DeviceType.THERMOSTAT, roomId = "lroom", isOn = true, intensity = 17.0, unit = "°C"),
            // FAN: Sadece gösterim ve toggle için mock
            Device(id = "fan-main", name = "Fan", type = DeviceType.FAN, roomId = "lroom", isOn = false, isActuator = true)
        )
    }

    init {
        // Mock Verileri Yükle
        _uiState.update { currentState ->
            currentState.copy(
                quickAccessDevices = getInitialDevices(),
                weather = WeatherState(), // Mock Hava Durumu
                isLoading = false,
                isMqttConnected = true // Varsayılan olarak başarılı bağlantı
            )
        }

        repository.startConnection()

        // MQTT Dinleme Akışı (Sadece LIGHT cihazını günceller)
        viewModelScope.launch {
            repository.getLightStatusUpdates().collect { lightUpdate ->
                _uiState.update { currentState ->
                    // Gelen lightUpdate'i quickAccessDevices listesinde bul ve durumunu değiştir
                    val updatedDevices = currentState.quickAccessDevices.map { device ->
                        if (device.id == lightUpdate.deviceId) {
                            device.copy(isOn = lightUpdate.status) // Light modelindeki status, Device modelindeki isOn'a eşlendi
                        } else {
                            device
                        }
                    }
                    currentState.copy(quickAccessDevices = updatedDevices)
                }
            }
        }
    }

    // YENİ GENEL TOGGLE FONKSİYONU
    fun toggleDevice(deviceId: String) {
        _uiState.update { currentState ->
            val updatedDevices = currentState.quickAccessDevices.map { device ->
                if (device.id == deviceId && device.isActuator) {
                    val newState = !device.isOn

                    // MQTT KOMUTU GÖNDERME (Sadece LIGHT cihazı için)
                    if (device.type == DeviceType.LIGHT) {
                        viewModelScope.launch {
                            // Light ID'sini ve yeni durumu gönder
                            repository.sendLightCommand(deviceId, newState)
                        }
                    }
                    // Diğer cihazlar (Fan, Termostat) şimdilik sadece yerelde güncellenecek

                    // UI'ı yerelde güncelleme
                    device.copy(isOn = newState)
                } else {
                    device
                }
            }
            currentState.copy(quickAccessDevices = updatedDevices)
        }
    }

    // Eski toggleLight fonksiyonu kaldırıldı.
    // addRoom fonksiyonu şimdilik aynı kalır.
    fun addRoom(roomName: String, devices: List<Device>) {
        println("MVVM Aksiyonu: Yeni Oda Kaydediliyor -> $roomName")
        // Gerçek implementasyon buraya gelecek
    }
}