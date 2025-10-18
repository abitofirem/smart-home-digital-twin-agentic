package com.example.iothome.domain.repository

import com.example.iothome.data.model.Light
import kotlinx.coroutines.flow.Flow

interface MqttRepositoryInterface {

    // MQTT bağlantısını başlatır.
    fun startConnection()

    // Unity'den gelen lamba durumu güncellemelerini yayınlar (Subscribe)
    fun getLightStatusUpdates(): Flow<Light>

    // Lamba açma/kapama komutunu Unity'ye gönderir (Publish)
    suspend fun sendLightCommand(lightId: String, newState: Boolean)
}