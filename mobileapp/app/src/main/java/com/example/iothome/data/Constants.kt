package com.example.iothome.data

object MqttConstants {
    // Broker Ayarları
    const val BROKER_HOST = "69935d3a217e4cdb94eef5662762a511.s1.eu.hivemq.cloud"
    const val BROKER_PORT = 8883 // Güvenli SSL Portu

    // Kullanıcı Bilgileri
    const val USERNAME = "mtnkdr"
    const val PASSWORD = "3N772AHL0qSRp8"

    // Konular (Topics)
    const val CLIENT_ID = "AndroidAppClient" // Her cihaz için benzersiz olmalı
    const val QOS = 1 // Quality of Service: Mesajın en az bir kez teslim garantisi

    // İletişim Konuları
    const val COMMAND_TOPIC = "commands/android/set-device-status" // Publish (Komut gönderme)
    const val UPDATE_TOPIC = "updates/unity/device-status"        // Subscribe (Durum dinleme)
}