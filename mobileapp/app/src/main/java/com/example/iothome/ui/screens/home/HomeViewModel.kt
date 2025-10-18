package com.example.iothome.ui.screens.home

import androidx.lifecycle.viewModelScope
import com.example.iothome.data.model.Light
import com.example.iothome.data.mqtt.MqttRepository // Repository import
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.update

// Context'e (uygulama context'i) ihtiyacımız olduğu için AndroidViewModel kullanıyoruz.
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    // DI kullanılmadığı için Repository'i manuel oluşturuyoruz.
    // Uygulama Context'ini Repository'e iletiyoruz.
    private val repository = MqttRepository(application)

    // UI State
    private val _lightState = MutableStateFlow(Light())
    val lightState: StateFlow<Light> = _lightState.asStateFlow()

    init {
        // 1. MQTT bağlantısını başlat ve güncel durumları dinle
        repository.startConnection()

        // 2. Repository'den gelen lamba durumlarını (subscribe) dinle
        viewModelScope.launch {
            repository.getLightStatusUpdates().collect { lightUpdate ->
                // Unity'den gelen yeni durumu UI State'ine yaz
                _lightState.value = lightUpdate
            }
        }
    }

    /**
     * Lamba aç/kapa komutunu MQTT üzerinden Unity'ye gönderir (Publish).
     */
// HomeViewModel.kt dosyanızdaki toggleLight() metodu
    fun toggleLight() {
        val currentLight = _lightState.value
        // newState değişkeni, mevcut durumun tam tersi olmalı!
        val newState = !currentLight.isOn // True ise False, False ise True olmalı.

        // 1. MQTT Komutunu gönder (Asıl iş)
        viewModelScope.launch {
            repository.sendLightCommand(currentLight.id, newState)
        }

        // 2. UI'ı YERELDE hemen güncelle (Kullanıcı Deneyimi için)
        _lightState.update { currentLight.copy(isOn = newState) }
    }


}