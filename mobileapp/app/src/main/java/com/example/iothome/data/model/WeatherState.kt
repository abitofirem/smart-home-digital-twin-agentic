package com.example.iothome.data.model

// Hava ve Konum mock verileri için
data class WeatherState(
    val greeting: String = "Merhaba KullaniciAdi!",
    val date: String = "Cumartesi, 20 Ekim 2025",
    val location: String = "Konum",
    val condition: String = "Bulutlu",
    val temperature: String = "-10°C",
    val humidityPercentage: Double = 25.0, // Nem yüzdesi (%)
    val temperatureHigh: String = "3°C",
    val temperatureLow: String = "-12°C"
)