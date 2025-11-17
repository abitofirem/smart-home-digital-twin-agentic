using System;
using System.Threading.Tasks; // Asenkron işlemler (InitializeAsync, PublishAsync vb.) için gerekli

// Tüm IoT ile ilgili çekirdek sınıflarımızı ve arayüzlerimizi bu namespace içine alıyoruz.
namespace SmartHome.Core.IoT 
{
    /// <summary>
    /// Dışarıdan (örn: MQTT Broker'dan) gelen bir mesajı temsil eden standart veri paketidir.
    /// Mesajı alan Provider (örn: MqttnetProvider), bu paketi oluşturup DeviceManager'a iletir.
    /// </summary>
    public class InboundMessage
    {
        /// <summary>
        /// Mesajın geldiği orijinal konu (örn: "home/devices/salon-lamba-1/set").
        /// </summary>
        public string Topic { get; private set; }

        /// <summary>
        /// Mesajın ham içeriği (genellikle JSON formatında).
        /// </summary>
        public string Payload { get; private set; }

        /// <summary>
        /// Mesajın hangi cihaza ait olduğunu belirten ID (örn: "salon-lamba-1").
        /// Bu alan, mesajı işleyen Provider (örn: MqttnetProvider) tarafından,
        /// genellikle Topic'ten ayrıştırılarak doldurulur.
        /// </summary>
        public string DeviceId { get; set; } 

        /// <summary>
        /// Yeni bir InboundMessage nesnesi oluşturmak için yapıcı metot.
        /// </summary>
        public InboundMessage(string topic, string payload)
        {
            this.Topic = topic;
            this.Payload = payload;
            // DeviceId burada atanmaz, Provider tarafından atanır.
        }
    }

    /// <summary>
    /// Projenin dış dünya ile iletişim kuracak olan herhangi bir "Mesaj Sağlayıcı" 
    /// (MQTT, Firebase SDK, vb.) sınıfının uyması gereken zorunlu sözleşmedir.
    /// DeviceManager, sadece bu arayüzü tanır ve kullanır.
    /// </summary>
    public interface IMessageProvider
    {
        /// <summary>
        /// Dış dünyadan yeni bir mesaj alındığında tetiklenmesi gereken olay (event).
        /// DeviceManager bu olaya abone olarak gelen mesajlardan haberdar olur.
        /// </summary>
        event Action<InboundMessage> OnMessageReceived;

        /// <summary>
        /// Mesaj Sağlayıcı'yı (örn: MQTT Broker'a bağlanma) asenkron olarak başlatan metot.
        /// DeviceManager tarafından oyunun başında bir kez çağrılır.
        /// </summary>
        Task InitializeAsync();

        /// <summary>
        /// Dış dünyaya (örn: MQTT Broker'a) belirtilen konuya (topic) bir mesajı 
        /// asenkron olarak yayınlayan (Publish) metot.
        /// DeviceManager, telemetri verisi göndermek için bunu kullanır.
        /// </summary>
        Task PublishAsync(string topic, string payload);

        /// <summary>
        /// Dış dünyadaki belirtilen bir konuya (topic) abone (Subscribe) olan metot.
        /// DeviceManager, cihaz komutlarını almak için hangi konuları
        /// dinlemesi gerektiğini bu metotla Provider'a bildirir.
        /// </summary>
        Task SubscribeAsync(string topic);

        // Gelecekte eklenebilecek metotlar (Şimdilik Gerekli Değil):
        // Task UnsubscribeAsync(string topic); // Abonelikten çıkma
        // Task DisconnectAsync(); // Bağlantıyı kesme
    }
}