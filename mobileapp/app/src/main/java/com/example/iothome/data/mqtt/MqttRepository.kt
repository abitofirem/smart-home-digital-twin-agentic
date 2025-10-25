package com.example.iothome.data.mqtt

import android.content.Context
import com.example.iothome.data.MqttConstants
import com.example.iothome.data.model.Light
import com.example.iothome.domain.repository.MqttRepositoryInterface
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.map
import org.json.JSONObject

class MqttRepository(context: Context) : MqttRepositoryInterface {

    private val mqttService = MqttService(context)

    override fun startConnection() {
        mqttService.connect()
    }

    override fun getLightStatusUpdates(): Flow<Light> {
        // MqttService'ten gelen ham String mesajı al ve Light objesine dönüştür.
        return mqttService.messageFlow.map { payloadString ->
            try {
                // YENİ JSON FORMATI: {"deviceId": "salon-ayakli-lamba-1", "status": "ON"}
                val json = JSONObject(payloadString)
                val status = json.getString("status")

                Light(
                    deviceId = json.getString("deviceId"), // JSON'dan deviceId (String) oku
                    name = "Akıllı Lamba",
                    status = status.uppercase() == "ON"
                )
            } catch (e: Exception) {
                println("MQTT Payload dönüşüm hatası: ${e.message}")
                // Hata durumunda Light'ın tüm alanlarını içeren bir nesne döndürülmeli.
                // deviceId'yi varsayılan değerle dolduralım.
                Light(deviceId = "default_id", status = false)
            }
        }
    }

    override suspend fun sendLightCommand(lightId: String, newState: Boolean) {

        val statusString = if (newState) "ON" else "OFF"

        // KRİTİK DÜZELTME: Artık Int ID değil, tam string ID kullanılıyor ve JSON anahtarı 'status' oldu.
        val payload = JSONObject().apply {
            put("deviceId", lightId) // lightId zaten "salon-ayakli-lamba-1" gibi bir String
            put("status", statusString) // JSON'a Int değil, String ID gönderiyoruz ve 'status' anahtarını kullanıyoruz.
        }.toString()

        // MqttService üzerinden komutu yayınlar (Publish)
        mqttService.publish(MqttConstants.COMMAND_TOPIC, payload)
    }
}