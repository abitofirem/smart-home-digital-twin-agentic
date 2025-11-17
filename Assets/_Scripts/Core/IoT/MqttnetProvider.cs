using UnityEngine;
using System;
using System.Collections.Generic; // Mesaj kuyruğu için
using System.Text;             // Mesajları UTF8'e çevirmek için
using System.Threading;        // CancellationToken için
using System.Threading.Tasks;    // Asenkron işlemler (Task) için
using MQTTnet;                 // MQTTnet kütüphanesi
using MQTTnet.Client;          // MQTTnet Client sınıfları
using MQTTnet.Client.Options;  // MQTTnet Options sınıfları
using SmartHome.Core.IoT;      // Kendi IMessageProvider ve InboundMessage sınıflarımız

// Tüm IoT kodlarımızı bu namespace altında topluyoruz
namespace SmartHome.Core.IoT
{
    /*
     * MQTTNET PROVIDER
     * * IMessageProvider arayüzünü uygulayan somut sınıftır.
     * * MQTTnet (v3.1.2) kütüphanesini kullanarak MQTT Broker'a bağlanır.
     * * Gelen mesajları alır, DeviceId'yi Topic'ten çıkarır ve DeviceManager'a iletir.
     * * DeviceManager'dan gelen Publish isteklerini Broker'a gönderir.
     * * Bu script, sahnedeki bir GameObject'e eklenmelidir.
     */
    public class MqttnetProvider : MonoBehaviour, IMessageProvider
    {
        [Header("HiveMQ Cloud Connection Details")]
        [SerializeField] private string brokerAddress = "69935d3a217e4cdb94eef5662762a511.s1.eu.hivemq.cloud"; 
        [SerializeField] private int brokerPort = 8883; 
        [SerializeField] private string username = "mtnkdr"; 
        [SerializeField] private string password = "3N772AHL0qSRp8";

        // Olay (Event): Dışarıdan bir mesaj geldiğinde tetiklenir. DeviceManager buna abone olacak.
        public event Action<InboundMessage> OnMessageReceived;

        private IMqttClient _mqttClient;
        private bool _isConnected = false;

        // Gelen mesajları ana thread'de işlemek için bir kuyruk (Thread safety için önemli)
        private readonly Queue<MqttApplicationMessageReceivedEventArgs> _incomingMessagesQueue = new Queue<MqttApplicationMessageReceivedEventArgs>();

        /// <summary>
        /// IMessageProvider arayüzünden gelen başlatma metodu.
        /// MQTT Broker'a asenkron olarak bağlanır.
        /// </summary>
        public async Task InitializeAsync()
        {
            if (_isConnected)
            {
                Debug.LogWarning("[MqttnetProvider] Zaten bağlı.");
                return;
            }

            var factory = new MqttFactory();
            _mqttClient = factory.CreateMqttClient();

            var options = new MqttClientOptionsBuilder()
                .WithClientId(Guid.NewGuid().ToString()) // Her seferinde benzersiz bir ID
                .WithTcpServer(brokerAddress, brokerPort)
                .WithCredentials(username, password)
                .WithTls(new MqttClientOptionsBuilderTlsParameters // TLS Ayarları (HiveMQ Cloud için gerekli)
                {
                    UseTls = true,
                    // ÖNEMLİ: Geliştirme ortamında sertifika sorunlarını es geçmek için
                    // AllowUntrustedCertificates = true, 
                    // IgnoreCertificateChainErrors = true,
                    // IgnoreCertificateRevocationErrors = true 
                    // Bunları gerçek uygulamada false yapmalısınız veya doğru sertifikaları ayarlamalısınız.
                    // Şimdilik test için true bırakabiliriz.
                     AllowUntrustedCertificates = true, 
                     IgnoreCertificateChainErrors = true,
                     IgnoreCertificateRevocationErrors = true 
                })
                .WithCleanSession()
                .Build();

            // Mesaj geldiğinde ne olacağını tanımla (Kuyruğa ekle)
            _mqttClient.UseApplicationMessageReceivedHandler(e =>
            {
                // MQTT kütüphanesi mesajları farklı bir thread'de alabilir.
                // Unity API'larına doğrudan erişmek tehlikelidir.
                // Bu yüzden mesajı bir kuyruğa alıp Update() içinde işleyeceğiz.
                lock (_incomingMessagesQueue)
                {
                    _incomingMessagesQueue.Enqueue(e);
                }
            });

            // Bağlantı kesildiğinde ne olacağını tanımla (Tekrar bağlanmayı deneyebiliriz)
            _mqttClient.UseDisconnectedHandler(async e =>
            {
                Debug.LogWarning("[MqttnetProvider] MQTT Broker bağlantısı kesildi. Tekrar bağlanmaya çalışılıyor...");
                _isConnected = false;
                await Task.Delay(TimeSpan.FromSeconds(5)); // 5 saniye bekle
                try
                {
                    await _mqttClient.ConnectAsync(options, CancellationToken.None);
                }
                catch (Exception ex)
                {
                    Debug.LogError($"[MqttnetProvider] Tekrar bağlanma hatası: {ex.Message}");
                }
            });

             // Bağlandığında ne olacağını tanımla
            _mqttClient.UseConnectedHandler(e => {
                 Debug.Log("[MqttnetProvider] MQTT Broker'a başarıyla bağlandı!");
                 _isConnected = true;
                 // TODO: Bağlantı kurulunca otomatik olarak abone olunacak konuları buraya ekleyebiliriz.
            });


            // Bağlanmayı dene
            try
            {
                await _mqttClient.ConnectAsync(options, CancellationToken.None);
            }
            catch (Exception e)
            {
                Debug.LogError($"[MqttnetProvider] MQTT Broker'a bağlanırken HATA oluştu: {e.Message}");
                // Bağlantı başarısız olursa InitializeAsync'ın hata fırlatmasını sağlayalım ki DeviceManager bunu bilsin.
                throw; 
            }
        }

        /// <summary>
        /// Gelen mesaj kuyruğunu Unity'nin ana thread'inde (Update fonksiyonu) işler.
        /// FİNAL VERSİYON: DeviceId çıkarma mantığı kaldırıldı.
        /// </summary>
        private void Update()
        {
            // Kuyrukta işlenmeyi bekleyen mesaj var mı?
            while (_incomingMessagesQueue.Count > 0)
            {
                MqttApplicationMessageReceivedEventArgs e;
                lock (_incomingMessagesQueue)
                {
                    e = _incomingMessagesQueue.Dequeue();
                }

                // Mesajı işle
                try
                {
                    string topic = e.ApplicationMessage.Topic;
                    string payload = Encoding.UTF8.GetString(e.ApplicationMessage.Payload);

                    Debug.Log($"[MqttnetProvider] Mesaj alındı - Topic: {topic} | Payload: {payload}");

                    // Gelen mesaj için InboundMessage paketi oluştur.
                    // DeviceId'yi artık burada AYIKLAMIYORUZ. DeviceManager JSON'dan alacak.
                    InboundMessage inboundMsg = new InboundMessage(topic, payload);
                    
                    // Mesajı DeviceManager'a iletmek için olayı (event) tetikle
                    OnMessageReceived?.Invoke(inboundMsg);
                }
                catch (Exception ex)
                {
                    Debug.LogError($"[MqttnetProvider] Gelen MQTT mesajı işlenirken hata: {ex.Message}");
                }
            }
        }

        /// <summary>
        /// IMessageProvider arayüzünden gelen metot.
        /// Belirtilen konuya (topic) bir mesaj yayınlar (Publish).
        /// </summary>
        public async Task PublishAsync(string topic, string payload)
        {
            if (!_isConnected || _mqttClient == null)
            {
                Debug.LogWarning($"[MqttnetProvider] MQTT bağlı değilken mesaj yayınlanamaz! Topic: {topic}");
                return; // Veya hata fırlatabiliriz: throw new InvalidOperationException("MQTT client not connected.");
            }

            var message = new MqttApplicationMessageBuilder()
                .WithTopic(topic)
                .WithPayload(payload)
                .WithExactlyOnceQoS() // Veya AtLeastOnceQoS() / AtMostOnceQoS()
                .WithRetainFlag(false) // Mesajın broker'da kalıcı olmamasını sağla
                .Build();

            try
            {
                await _mqttClient.PublishAsync(message, CancellationToken.None);
                // Başarılı publish log'u genellikle spam yaratır, sadece hata olursa loglamak daha iyidir.
            }
            catch (Exception e)
            {
                 Debug.LogError($"[MqttnetProvider] Mesaj yayınlanırken HATA oluştu (Topic: {topic}): {e.Message}");
                 // Hata oluşursa bunu yukarıya (DeviceManager'a) bildirebiliriz.
                 throw;
            }
        }

        /// <summary>
        /// IMessageProvider arayüzünden gelen metot.
        /// Belirtilen konuya (topic) abone olur (Subscribe).
        /// </summary>
        public async Task SubscribeAsync(string topic)
        {
             if (!_isConnected || _mqttClient == null)
            {
                Debug.LogWarning($"[MqttnetProvider] MQTT bağlı değilken konuya abone olunamaz! Topic: {topic}");
                return; // Veya hata fırlatabiliriz
            }

            var topicFilter = new MqttTopicFilterBuilder()
                .WithTopic(topic)
                .WithExactlyOnceQoS() // Veya diğer QoS seviyeleri
                .Build();

            try
            {
                await _mqttClient.SubscribeAsync(topicFilter);
                 Debug.Log($"[MqttnetProvider] Konuya başarıyla abone olundu: {topic}");
            }
            catch (Exception e)
            {
                 Debug.LogError($"[MqttnetProvider] Konuya abone olurken HATA oluştu ({topic}): {e.Message}");
                 throw;
            }
        }

        /// <summary>
        /// Uygulama kapanırken MQTT bağlantısını düzgünce kapatır.
        /// </summary>
        private async void OnApplicationQuit()
        {
            if (_mqttClient != null && _mqttClient.IsConnected)
            {
                // Önce abonelikleri iptal etmeyi düşünebiliriz (genellikle gerekli değil)
                // await _mqttClient.UnsubscribeAsync(...); 
                
                // Bağlantıyı kapat
                await _mqttClient.DisconnectAsync();
                _isConnected = false;
                Debug.Log("[MqttnetProvider] MQTT bağlantısı kapatıldı.");
            }
        }

        // Opsiyonel: Bağlantıyı manuel olarak kesmek için bir metot
        // public async Task DisconnectAsync() { ... }
    }
}