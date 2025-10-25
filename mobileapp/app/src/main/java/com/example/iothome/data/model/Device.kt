package com.example.iothome.data.model

// Cihaz Kataloğu ve Oda Kontrolü için kullanılan tipler
enum class DeviceType {
    LIGHT,
    FAN,
    THERMOSTAT,
    COFFEE_MACHINE,
    TV,
    PURIFIER,
    WASHING_MACHINE,
    GARAGE_DOOR,
    SENSOR          // Sensörleri de kataloğa dahil ediyoruz
    // EKLE: Diğer tipleri buraya ekleyebilirsiniz.
}

// Tüm cihazları temsil eden ana model
data class Device(
    val id: String,
    val name: String,
    val type: DeviceType,
    val roomId: String,
    val isActuator: Boolean = true,           // Kontrol edilebilir mi?
    val isOn: Boolean = false,                // Açık/Kapalı durumu (Toggle)
    val intensity: Double = 0.0,              // Parlaklık/Hız/Sıcaklık değeri
    val unit: String? = null,                 // Örn: "°C", "%"
    val isOnline: Boolean = true              // Simülasyon bağlantı durumu
)
// Diğer model sınıflarınız (Device, Room, Suggestion) bu dosyada yer alabilir
// Veya ayrı dosyalarda olsalar bile DeviceType enum'unu import edebilirler.