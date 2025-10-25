package com.example.iothome.data.model

// Cihaz ve Room modelleri bu dosyada veya ayrı dosyalarda olmalı

data class HomeUiState(
    val weather: WeatherState = WeatherState(),
    val quickAccessDevices: List<Device> = emptyList(), // Sık Kullanılan Cihazlar
    val isMqttConnected: Boolean = false,
    val isLoading: Boolean = true
    // Diğer listeler (Rooms, Suggestions) buraya eklenecektir.
)