package com.example.iothome.data.mqtt

import android.content.Context
import com.example.iothome.data.MqttConstants
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.security.KeyStore
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory

/**
 * Paho MQTT Client kütüphanesi ile bağlantı, abonelik ve yayın işlemlerini yönetir.
 */
class MqttService(context: Context) {

    private val brokerUri = "ssl://${MqttConstants.BROKER_HOST}:${MqttConstants.BROKER_PORT}"
    private val client: MqttClient = MqttClient(brokerUri, MqttConstants.CLIENT_ID, MemoryPersistence())

    // Unity'den gelen güncel durumları yayınlamak için SharedFlow kullanıyoruz.
    private val _messageFlow = MutableSharedFlow<String>()
    val messageFlow: SharedFlow<String> = _messageFlow.asSharedFlow()

    init {
        setupMqttCallback()
    }

    private fun setupMqttCallback() {
        client.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String) {
                println("MQTT Bağlantısı Başarılı. Yeniden Bağlantı: $reconnect")
                // Bağlantı başarılı olduğunda, durum güncellemeleri için abone ol.
                subscribeToUpdates()
            }

            override fun connectionLost(cause: Throwable?) {
                println("MQTT Bağlantısı Kesildi: ${cause?.message}")
                // Yeniden bağlanma mekanizması burada tetiklenebilir.
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                // Unity'den gelen mesajları dinle ve akışa yayınla
                val payload = String(message.payload)
                println("MQTT Mesaj Geldi: Konu=$topic, İçerik=$payload")
                _messageFlow.tryEmit(payload)
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {
                // Publish işleminin tamamlandığını bildirir.
                println("MQTT Yayın Başarılı: ${token.message}")
            }
        })
    }

    /**
     * HiveMQ Cloud için SSL/TLS bağlantı seçeneklerini oluşturur.
     */
    private fun getConnectOptions(): MqttConnectOptions {
        val options = MqttConnectOptions()
        options.isCleanSession = true
        options.userName = MqttConstants.USERNAME
        options.password = MqttConstants.PASSWORD.toCharArray()
        options.socketFactory = getSSLSocketFactory() // SSL/TLS Güvenliği

        return options
    }

    /**
     * Güvenli MQTT (MQTTS) için varsayılan SSL Bağlantı Fabrikasını sağlar.
     */
    private fun getSSLSocketFactory(): SSLSocketFactory {
        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(null as KeyStore?)
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, tmf.trustManagers, null)
        return sslContext.socketFactory
    }

    fun connect() {
        try {
            client.connect(getConnectOptions())
        } catch (e: MqttException) {
            println("MQTT Bağlantı Hatası: ${e.message}")
        }
    }

    fun publish(topic: String, payload: String) {
        if (client.isConnected) {
            val message = MqttMessage(payload.toByteArray())
            message.qos = MqttConstants.QOS
            try {
                client.publish(topic, message)
                println("MQTT Yayınlandı: Konu=$topic, İçerik=$payload")
            } catch (e: MqttException) {
                println("MQTT Yayınlama Hatası: ${e.message}")
            }
        }
    }

    private fun subscribeToUpdates() {
        try {
            // Unity'den gelen güncellemeleri dinlemek için abone ol
            client.subscribe(MqttConstants.UPDATE_TOPIC, MqttConstants.QOS)
            println("MQTT Abone Olundu: ${MqttConstants.UPDATE_TOPIC}")
        } catch (e: MqttException) {
            println("MQTT Abonelik Hatası: ${e.message}")
        }
    }
}