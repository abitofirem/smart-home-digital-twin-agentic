using UnityEngine;
using System.Collections.Generic; // Dictionary ve List için
using System.Linq;                // FindObjectsOfType().OfType<T>() için
using SmartHome.Core.IoT;         // IMessageProvider ve InboundMessage için
using SmartHome.Core.Devices;    // ISmartDevice için
using SmartHome.Core.Data;       // CommandMessage ve TelemetryMessage için
using Newtonsoft.Json;             // JSON parse etmek için
using System;                      // Exception ve Action için
using System.Threading.Tasks;    // Task (await InitializeAsync) için

// Tüm çekirdek sistemlerimizi bu namespace altında topluyoruz.
namespace SmartHome.Core 
{
    /*
     * DEVICE MANAGER (GENEL MÜDÜR)
     * * Sahnedeki tüm akıllı cihazları (ISmartDevice) yönetir.
     * * Dış dünyadan (IMessageProvider aracılığıyla) komutları alır ve doğru cihaza iletir.
     * * Cihazlardan (ISmartDevice) gelen telemetri verilerini alır ve dış dünyaya (IMessageProvider aracılığıyla) yayınlar.
     * * Singleton deseni ile tasarlanmıştır, yani sahnede sadece 1 tane olabilir.
     */
    public class DeviceManager : MonoBehaviour
    {
        #region Singleton
        // --- Singleton Deseni Başlangıcı ---
        public static DeviceManager Instance { get; private set; }

        private void Awake()
        {
            if (Instance != null && Instance != this) 
            { 
                Debug.LogWarning("[DeviceManager] Sahnede birden fazla DeviceManager bulundu! Bu örnek yok ediliyor.");
                Destroy(gameObject); 
                return; 
            }
            Instance = this;
            // Opsiyonel: DontDestroyOnLoad(gameObject); // Eğer sahneler arası geçişte yöneticinin kalmasını isterseniz.

            // IMessageProvider'ı Awake'de bulup atıyoruz ki Start'ta kullanıma hazır olsun.
            messageProvider = messageProviderObject as IMessageProvider;
            if (messageProvider == null && messageProviderObject != null) 
            {
                Debug.LogError($"[DeviceManager] HATA: Inspector'dan atanan 'Message Provider Object' ({messageProviderObject.name}), " +
                               $"IMessageProvider arayüzünü uygulamıyor!", this);
            }
            else if (messageProviderObject == null)
            {
                 Debug.LogError($"[DeviceManager] HATA: 'Message Provider Object' atanmamış! Lütfen Inspector'dan atayın.", this);
            }
        }
        // --- Singleton Deseni Bitişi ---
        #endregion

        [Header("Sistem Bağlantıları")]
        [Tooltip("IMessageProvider arayüzünü uygulayan MonoBehaviour objesi (örn: MqttnetProvider). Sahnedeki bu objeyi buraya sürükleyin.")]
        [SerializeField] private MonoBehaviour messageProviderObject; // Inspector'dan atanacak olan asıl obje
        private IMessageProvider messageProvider; // Kod içinde kullanacağımız arayüz referansı

        [Header("Cihaz Kayıt Defteri")]
        [Tooltip("Sistemdeki tüm kayıtlı akıllı cihazlar (Hata ayıklama için görünür)")]
        [SerializeField] private List<string> _registeredDeviceIDs = new List<string>(); // Inspector'da sadece ID'leri görmek için
        
        // Cihazları ID'lerine göre hızlıca bulmak için Sözlük (Dictionary)
        private Dictionary<string, ISmartDevice> _deviceRegistry = new Dictionary<string, ISmartDevice>();
        
        /// <summary>
        /// Oyun başladığında çalışan ana başlatma fonksiyonu.
        /// </summary>
        private async void Start()
        {
            // Eğer Awake'de messageProvider bulunamadıysa Start'ta devam etme.
            if (messageProvider == null) 
            {
                Debug.LogError("[DeviceManager] Başlatılamadı: Geçerli bir Message Provider bulunamadı.", this);
                return; 
            }

            // 1. Dış dünyadan gelen mesajları dinlemek için abone ol.
            messageProvider.OnMessageReceived += HandleInboundMessage;
            
            // 2. Mesaj sağlayıcıyı başlat (örn: MQTT'ye bağlan). Hata olursa logla ve dur.
            try
            {
               await messageProvider.InitializeAsync(); 
               Debug.Log("[DeviceManager] Message Provider başarıyla başlatıldı.");

               // 3. Sadece bağlantı başarılı olursa cihazları bul ve kaydet.
               RegisterAllDevices(); 
               Debug.Log($"[DeviceManager] Başlatıldı. {_deviceRegistry.Count} adet cihaz bulundu ve kaydedildi.");

               // 4. Cihaz komutlarını almak için arkadaşının belirttiği MQTT konusuna abone ol
               await messageProvider.SubscribeAsync("updates/unity/device-status");
               Debug.Log("[DeviceManager] Cihaz komutları konusuna abone olundu: updates/unity/device-status");

            }
            catch(Exception ex)
            {
                Debug.LogError($"[DeviceManager] Message Provider başlatılırken/abone olurken hata oluştu: {ex.Message}", this);
                return; 
            }
        }

        /// <summary>
        /// Sahnedeki ISmartDevice arayüzünü uygulayan tüm aktif ve geçerli cihazları bulur,
        /// onları başlatır (Initialize) ve kayıt defterine (`_deviceRegistry`) ekler.
        /// </summary>
        private void RegisterAllDevices()
        {
            _deviceRegistry.Clear(); 
            _registeredDeviceIDs.Clear();
            
            // Sahnedeki ISmartDevice arayüzünü uygulayan TÜM MonoBehaviour'ları bulur (aktif olmasalar bile).
            ISmartDevice[] devicesInScene = FindObjectsByType<MonoBehaviour>(FindObjectsSortMode.None).OfType<ISmartDevice>().ToArray();

            Debug.Log($"[DeviceManager] Sahnede {devicesInScene.Length} adet potansiyel ISmartDevice bulundu.");

            foreach (ISmartDevice device in devicesInScene)
            {
                // Cihaz script'inin bağlı olduğu GameObject'e erişelim.
                MonoBehaviour deviceMonoBehaviour = device as MonoBehaviour;
                if (deviceMonoBehaviour == null) 
                {
                    Debug.LogWarning($"[DeviceManager] ISmartDevice arayüzünü uygulayan bir script bulundu ama MonoBehaviour değil? Bu beklenmedik bir durum.");
                    continue; // Bu cihazı atla
                }

                // Cihazın Inspector'dan atanmış ID'sini al
                string id = "";
                try {
                    id = device.DeviceId; 
                     if (string.IsNullOrEmpty(id)) {
                         Debug.LogWarning($"[DeviceManager] Cihazın ({deviceMonoBehaviour.gameObject.name}) DeviceId özelliği boş! Inspector'dan atanmamış olabilir. Kayıt edilemedi.", deviceMonoBehaviour);
                         continue;
                    }
                } catch (Exception ex) { 
                     Debug.LogWarning($"[DeviceManager] Cihazın ({deviceMonoBehaviour.gameObject.name}) DeviceId özelliğine erişirken hata: {ex.Message}. Kayıt edilemedi.", deviceMonoBehaviour);
                     continue;
                }

                // Aynı ID ile başka bir cihaz zaten kayıtlı mı?
                if (_deviceRegistry.ContainsKey(id))
                {
                    Debug.LogWarning($"[DeviceManager] '{id}' kimliğine sahip birden fazla cihaz bulundu! Sadece ilki kaydedildi ({_deviceRegistry[id].GetType().Name}). " +
                                     $"Diğeri ({deviceMonoBehaviour.gameObject.name}) yoksayıldı.", deviceMonoBehaviour);
                    continue;
                }

                // Her şey yolundaysa, cihazı başlat ve kaydet.
                try 
                {
                    device.Initialize(this); // Cihazı "patronu" ile tanıştır
                    _deviceRegistry.Add(id, device);
                    _registeredDeviceIDs.Add(id); // Inspector listesi için

                    // Cihazın veri yayınlama olayını dinle
                    device.OnTelemetryDataPublished -= HandleTelemetryPublished; // Önce eski aboneliği kaldır (güvenlik için)
                    device.OnTelemetryDataPublished += HandleTelemetryPublished;
                    Debug.Log($"[DeviceManager] Cihaz başarıyla kaydedildi: {id} ({device.GetType().Name})");
                } 
                catch (Exception initEx)
                {
                    Debug.LogError($"[DeviceManager] Cihaz ({id}) başlatılırken (Initialize) hata oluştu: {initEx.Message}", deviceMonoBehaviour);
                    // Hatalı cihazı kaydetme.
                }
            }
             Debug.Log($"[DeviceManager] Cihaz kayıt işlemi tamamlandı. Toplam {_deviceRegistry.Count} cihaz kaydedildi.");
        }

        /// <summary>
        /// IMessageProvider'dan bir mesaj geldiğinde bu fonksiyon tetiklenir.
        /// FİNAL VERSİYON: Backend'den gelen {"deviceId":"...", "command":"..."} formatını işler.
        /// </summary>
        private void HandleInboundMessage(InboundMessage message) //
        {
            try
            {
                // 1. Gelen ham JSON içeriğini CommandMessage sınıfımıza parse et.
                CommandMessage cmd = JsonConvert.DeserializeObject<CommandMessage>(message.Payload);

                // 2. JSON parse edildi mi ve GEREKLİ alanlar dolu mu kontrol et.
                if (cmd == null)
                {
                    Debug.LogWarning($"[DeviceManager] Gelen mesajın içeriği (Payload) geçerli bir JSON değil veya boş: {message.Payload}");
                    return;
                }
                if (string.IsNullOrEmpty(cmd.deviceId))
                {
                    Debug.LogWarning($"[DeviceManager] Gelen komut JSON'unda 'deviceId' alanı eksik veya boş! Mesaj yoksayıldı: {message.Payload}");
                    return;
                }
                if (string.IsNullOrEmpty(cmd.command))
                {
                    Debug.LogWarning($"[DeviceManager] '{cmd.deviceId}' için gelen komut JSON'unda 'command' alanı eksik veya boş! Mesaj yoksayıldı: {message.Payload}");
                    return;
                }

                // 3. JSON'dan okuduğumuz 'cmd.deviceId' ile kayıt defterimizde cihaz ara.
                if (_deviceRegistry.TryGetValue(cmd.deviceId, out ISmartDevice targetDevice))
                {
                    // Cihaz bulundu. Komutu doğrudan (çeviri yapmadan) cihaza ilet.
                    Debug.Log($"[DeviceManager] Komut '{cmd.deviceId}' cihazına iletiliyor: Command='{cmd.command}', Payload='{cmd.payload}'");
                    targetDevice.ReceiveCommand(cmd.command, cmd.payload); //
                }
                else
                {
                    // Kayıt defterimizde bu ID'ye sahip bir cihaz bulamadık.
                    Debug.LogWarning($"[DeviceManager] '{cmd.deviceId}' ID'sine sahip bir cihaz kayıtlı değil. Komut yoksayıldı. Payload: {message.Payload}");
                }
            }
            catch (JsonException jsonEx) // JSON formatı tamamen bozuksa
            {
                Debug.LogError($"[DeviceManager] Gelen mesajın içeriği (JSON) parse edilemedi: {jsonEx.Message} | Topic: {message.Topic} | Payload: {message.Payload}");
            }
            catch (Exception ex) // Komutu cihaza iletirken veya başka beklenmedik bir hata olursa
            {
                Debug.LogError($"[DeviceManager] Mesaj işlenirken beklenmedik hata: {ex.Message} | Payload: {message.Payload}");
            }
        }
        
        /// <summary>
        /// Kayıtlı bir cihaz (ISmartDevice) veri yayınlamak istediğinde (OnTelemetryDataPublished event'ini tetiklediğinde) çağrılır.
        /// Hata ayıklama logları eklenmiştir.
        /// </summary>
        private async void HandleTelemetryPublished(TelemetryMessage telemetry) //
        {
            // --- YENİ DEBUG LOG 1: Fonksiyon çağrıldı mı? ---
            // Gelen telemetri nesnesinin null olup olmadığını kontrol et
            if (telemetry == null)
            {
                Debug.LogError("[DeviceManager] HandleTelemetryPublished çağrıldı ancak gelen telemetri verisi null!");
                return;
            }
            Debug.Log($"[DeviceManager] HandleTelemetryPublished fonksiyonu çağrıldı! Cihaz: {telemetry.deviceId}");

            // Message Provider atanmış ve hazır mı?
            if (messageProvider == null)
            {
                Debug.LogWarning($"[DeviceManager] Telemetri alındı ({telemetry.deviceId}) ama Message Provider atanmamış veya hazır değil. Yayınlanamadı.");
                return;
            }

            // Gönderilecek konuyu (topic) belirle
            string topic = $"home/telemetry/{telemetry.deviceId}/{telemetry.dataType}";

            // TelemetryMessage nesnesini JSON string'ine dönüştür
            string payload = "";
            try
            {
                payload = JsonConvert.SerializeObject(telemetry);
                // JSON'a dönüştürme başarılıysa loglamaya gerek yok.
            }
            catch (Exception ex)
            {
                Debug.LogError($"[DeviceManager] Telemetri verisi ({telemetry.deviceId}) JSON'a dönüştürülürken hata: {ex.Message}", this);
                return; // JSON'a çevrilemiyorsa gönderme işlemi yapma.
            }

            // Mesajı dış dünyaya yayınla (Publish)
            try
            {
                // Asenkron olarak yayınlama işlemini bekle
                await messageProvider.PublishAsync(topic, payload);

                // --- YENİ DEBUG LOG 2: Yayınlama başarılı mı? ---
                Debug.Log($"[DeviceManager] Telemetri yayınlandı: {topic} | {payload}");
            }
            catch (Exception e)
            {
                // Yayınlama sırasında bir hata oluşursa logla
                Debug.LogError($"[DeviceManager] Telemetri yayınlanırken HATA oluştu (Topic: {topic}): {e.Message}");
                // Bu hata, messageProvider.PublishAsync'tan fırlatılmış olabilir.
            }
        }
        
        /// <summary>
        /// Uygulama kapanırken veya bu obje yok edilirken çağrılır.
        /// Event aboneliklerini temizlemek için önemlidir.
        /// </summary>
        private void OnDestroy()
        {
            // Message Provider'ın olay aboneliğini kaldır.
            if (messageProvider != null)
            {
                messageProvider.OnMessageReceived -= HandleInboundMessage;
                // Opsiyonel: messageProvider.DisconnectAsync(); çağrılabilir.
            }
            
            // Kayıtlı tüm cihazların olay aboneliklerini kaldır.
            foreach (var kvp in _deviceRegistry) 
            {
               if(kvp.Value != null) // Cihaz hala varsa
                   kvp.Value.OnTelemetryDataPublished -= HandleTelemetryPublished;
            }
            _deviceRegistry.Clear(); // Kayıt defterini temizle
            _registeredDeviceIDs.Clear();

             // Singleton referansını temizle (önemli)
            if (Instance == this) {
                Instance = null;
            }
        }
    }
}