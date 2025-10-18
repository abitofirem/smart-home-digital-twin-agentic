package com.example.iothome.data.mqtt

import android.content.Context
import com.example.iothome.data.MqttConstants
import com.example.iothome.data.model.Light
import com.example.iothome.domain.repository.MqttRepositoryInterface
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.map
import org.json.JSONObject

/**
 * MqttRepository, MqttService'ten gelen ham veriyi (String) alıp,
 * Domain katmanının anlayacağı Light modeline çevirir.
 */
class MqttRepository(context: Context) : MqttRepositoryInterface {

    private val mqttService = MqttService(context)

    override fun startConnection() {
        mqttService.connect()
    }

    override fun getLightStatusUpdates(): Flow<Light> {
        // MqttService'ten gelen ham String mesajı al ve Light objesine dönüştür.
        return mqttService.messageFlow.map { payloadString ->
            try {
                // payloadString'in şöyle bir JSON olduğunu varsayıyoruz:
                // {"id": "light_1", "status": "ON/OFF"}
                val json = JSONObject(payloadString)
                val status = json.getString("status")

                Light(
                    id = json.getString("id"),
                    name = "Akıllı Lamba", // Bu bilgiyi modelde tutmak şimdilik basit bir yaklaşımdır.
                    isOn = status.uppercase() == "ON"
                )
            } catch (e: Exception) {
                println("MQTT Payload dönüşüm hatası: ${e.message}")
                // Hata durumunda varsayılan bir durumu döndür
                Light(isOn = false)
            }
        } // <<< ZORLA DÖNÜŞÜM KALDIRILDI! Bu artık Flow<Light> döndürüyor.
    }

    override suspend fun sendLightCommand(lightId: String, newState: Boolean) {

        // 1. Durum string'ini Unity'nin beklediği 'newStatus' alanına çevirme
        val statusString = if (newState) "ON" else "OFF"

        // 2. KRİTİK DÜZELTME: lightId string'ini (örn. "light_1") alıp sadece sayısal kısmını (1) tamsayıya çevirme
        // Eğer lightId = "light_1" ise: split("_").last() -> "1" olur. toIntOrNull() -> 1 olur.
        val deviceIdInt = lightId.split("_").last().toIntOrNull() ?: 1
        // toIntOrNull() kullanarak güvenli dönüşüm yapıyoruz, hata olursa varsayılan 1 kullanır.

        // 3. Payload'ı Unity'nin beklediği int tipi 'deviceId' ve 'newStatus' alan adları ile oluşturun:
        val payload = JSONObject().apply {
            put("deviceId", deviceIdInt) // JSON'a Int (tamsayı) gönderiyoruz
            put("status", statusString) // newStatus kullanıyoruz
        }.toString()

        // MqttService üzerinden komutu yayınlar (Publish)
        mqttService.publish(MqttConstants.COMMAND_TOPIC, payload)
    }
}