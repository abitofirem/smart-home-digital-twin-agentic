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

// Diğer model sınıflarınız (Device, Room, Suggestion) bu dosyada yer alabilir
// Veya ayrı dosyalarda olsalar bile DeviceType enum'unu import edebilirler.