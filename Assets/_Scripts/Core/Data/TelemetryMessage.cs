using System;

namespace SmartHome.Core.Data
{
    /*
     * TELEMETRY MESSAGE (GİDEN VERİ PAKETİ)
     * * Bu sınıf, bir sensörden (ISmartDevice) DeviceManager'a
     * ve oradan da dış dünyaya (MQTT/Firebase) gönderilecek
     * verinin standart formatıdır.
     * * Örnek JSON: {"deviceId":"banyo-termostat", "dataType":"temperature", "value":"21.8", "timestamp":1666087800}
     * * Bu bir MonoBehaviour DEĞİLDİR. Sadece veri taşır.
     */
    [Serializable] // Bu satır, Unity'nin bu sınıfı tanımasını sağlar.
    public class TelemetryMessage
    {
        /// <summary>
        /// Bu veriyi gönderen cihazın kimliği (örn: "banyo-termostat").
        /// </summary>
        public string deviceId;

        /// <summary>
        /// Verinin türü (örn: "temperature", "humidity", "doorStatus", "isOn").
        /// </summary>
        public string dataType;

        /// <summary>
        /// Verinin kendisi (örn: "22.5", "60", "open", "true").
        /// Esneklik için string olarak tutulur.
        /// </summary>
        public string value;

        /// <summary>
        /// Verinin ne zaman üretildiğini gösteren Unix zaman damgası (saniye veya milisaniye).
        /// DateTimeOffset.UtcNow.ToUnixTimeSeconds() veya ToUnixTimeMilliseconds() ile alınabilir.
        /// </summary>
        public long timestamp;

        /// <summary>
        /// Yeni bir TelemetryMessage nesnesi oluşturmak için yapıcı metot (Constructor).
        /// </summary>
        public TelemetryMessage(string deviceId, string dataType, string value, long timestamp)
        {
            this.deviceId = deviceId;
            this.dataType = dataType;
            this.value = value;
            this.timestamp = timestamp;
        }
    }
}