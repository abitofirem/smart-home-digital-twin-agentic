package com.example.iothome.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iothome.data.model.Light
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    // ViewModel'ın dışarıya sunduğu ve UI'ın izlediği State
    private val _lightState = MutableStateFlow(Light())
    val lightState: StateFlow<Light> = _lightState.asStateFlow()

    /**
     * Lamba durumunu değiştirme (toggle) işlemini yapar.
     * GELECEKTE: Bu fonksiyon, Use Case'i çağıracaktır.
     */
    fun toggleLight() {
        viewModelScope.launch {
            _lightState.update { currentLight ->
                // Mevcut durumun tersini alarak lambayı açar/kapatır
                currentLight.copy(isOn = !currentLight.isOn)
            }
        }
    }
}