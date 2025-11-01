package com.example.iothome.data

object MqttConstants {
    // Broker Ayarları
    const val BROKER_HOST = ""
    const val BROKER_PORT = ""

    // Kullanıcı Bilgileri
    const val USERNAME = ""
    const val PASSWORD = ""

    // Konular (Topics)
    const val CLIENT_ID = "AndroidAppClient" // Her cihaz için benzersiz olmalı
    const val QOS = 1 // Quality of Service: Mesajın en az bir kez teslim garantisi

    // İletişim Konuları
    const val COMMAND_TOPIC = "commands/android/set-device-status" // Publish (Komut gönderme)
    const val UPDATE_TOPIC = "updates/unity/device-status"        // Subscribe (Durum dinleme)
}