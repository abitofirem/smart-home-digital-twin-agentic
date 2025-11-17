using UnityEngine;
using System;                   // Action (event) için gerekli
using SmartHome.Core;         // DeviceManager için
using SmartHome.Core.Devices; // ISmartDevice arayüzü için
using SmartHome.Core.Data;    // TelemetryMessage için
using UnityEngine.InputSystem; 

// Cihaz script'lerimizi bu namespace altında topluyoruz
namespace SmartHome.Devices
{
    /*
     * AKILLI LAMBA KONTROLCÜSÜ (YENİ MİMARİ)
     * * ISmartDevice arayüzünü uygular.
     * * DeviceManager tarafından yönetilir.
     * * "on" ve "off" komutlarını alır.
     * * Durum değişikliğini Telemetry olarak yayınlar (isteğe bağlı).
     */
    public class SmartLamp : MonoBehaviour, ISmartDevice // MonoBehaviour'dan türet ve ISmartDevice sözleşmesini uygula
    {
        [Header("Kimlik (Zorunlu!)")]
        [Tooltip("Bu lambanın veritabanındaki benzersiz kimliği (örn: salon-lamba-1). DeviceManager bu ID ile cihazı bulur.")]
        [SerializeField] private string deviceId = "Lütfen_Bir_ID_Atayın";

        [Header("Bağlantılar (Inspector'dan Atanacak)")]
        [Tooltip("Kontrol edilecek ana ışık bileşeni.")]
        [SerializeField] private Light lampLight;
        [Tooltip("Işık açıldığında parlaması istenen materyallere sahip Renderer'lar (örn: lambanın cam kısımları).")]
        [SerializeField] private Renderer[] emissionRenderers;

        [Header("Ayarlar")]
        [Tooltip("Lamba açıkken emisyon rengi ve yoğunluğu.")]
        [SerializeField] private Color emissionColor = Color.yellow;
        [SerializeField] [Range(0f, 5f)] private float emissionIntensity = 1.2f;

        // ISmartDevice arayüzünden gelen özellik (Property)
        public string DeviceId => deviceId; // Inspector'dan atanan ID'yi döndürür

        // ISmartDevice arayüzünden gelen özellik
        public DeviceManager Manager { get; private set; } // Initialize metodunda atanacak

        // ISmartDevice arayüzünden gelen olay (Event)
        public event Action<TelemetryMessage> OnTelemetryDataPublished;

        // Cihazın mevcut durumu
        private bool _isOn = false;

        /// <summary>
        /// ISmartDevice arayüzünden gelen başlatma metodu.
        /// DeviceManager tarafından oyun başında bir kez çağrılır.
        /// </summary>
        public void Initialize(DeviceManager manager)
        {
            this.Manager = manager; // Patronumuzu kaydedelim

            // Başlangıç durumunu uygula (Inspector'da ışık kapalıysa kapalı başlasın)
            if (lampLight != null)
            {
                _isOn = lampLight.enabled; // Başlangıç durumunu Inspector'daki değere göre ayarla
            }
            ApplyVisualState(_isOn); // Görsel durumu güncelle

            Debug.Log($"[SmartLamp] Cihaz başlatıldı: {DeviceId}");
        }

        /// <summary>
        /// ISmartDevice arayüzünden gelen komut işleme metodu.
        /// DeviceManager tarafından çağrılır.
        /// </summary>
        public void ReceiveCommand(string command, string payload)
        {
            switch (command.ToLower()) // Komutu küçük harfe çevirerek kontrol et
            {
                case "on":
                    SetState(true);
                    break;
                case "off":
                    SetState(false);
                    break;
                case "toggle":
                    SetState(!_isOn);
                    break;
                default:
                    Debug.LogWarning($"[SmartLamp] ({DeviceId}) Bilinmeyen komut alındı: {command}");
                    break;
            }
        }

        /// <summary>
        /// Lambanın durumunu (açık/kapalı) ayarlar ve görsel durumu günceller.
        /// </summary>
        private void SetState(bool newState)
        {
            if (_isOn == newState) return; // Zaten aynı durumdaysa bir şey yapma

            _isOn = newState;
            ApplyVisualState(_isOn);
            Debug.Log($"[SmartLamp] ({DeviceId}) Durum değiştirildi -> {(_isOn ? "ON" : "OFF")}");

            // Durum değişikliğini Telemetry olarak yayınla (Opsiyonel ama önerilir)
            PublishTelemetry("isOn", _isOn.ToString().ToLower()); // "true" veya "false" olarak gönder
        }

        /// <summary>
        /// Lambanın ışığını ve materyal parlamasını (emisyon) ayarlar.
        /// </summary>
        private void ApplyVisualState(bool on)
        {
            // Ana ışığı aç/kapat
            if (lampLight != null)
            {
                lampLight.enabled = on;
            }

            // Emisyon materyallerini ayarla
            if (emissionRenderers != null && emissionRenderers.Length > 0)
            {
                Color targetColor = on ? emissionColor * emissionIntensity : Color.black;
                foreach (Renderer r in emissionRenderers)
                {
                    if (r != null && r.material != null)
                    {
                        // Materyalin emisyonunu (parlamasını) etkinleştir/devre dışı bırak
                        if (on)
                        {
                            r.material.EnableKeyword("_EMISSION");
                        }
                        else
                        {
                            r.material.DisableKeyword("_EMISSION");
                        }
                        // Emisyon rengini ayarla (HDR renkler için Color.black yeterli)
                        r.material.SetColor("_EmissionColor", targetColor);
                    }
                }
            }
        }

        /// <summary>
        /// Cihazın bir durumunu dış dünyaya (DeviceManager -> MQTT) bildirmek için kullanılır.
        /// </summary>
        /// <param name="dataType">Verinin türü (örn: "isOn", "temperature").</param>
        /// <param name="value">Verinin değeri (örn: "true", "22.5").</param>
        private void PublishTelemetry(string dataType, string value)
        {
            // Zaman damgasını al (Unix Epoch saniye cinsinden)
            long timestamp = DateTimeOffset.UtcNow.ToUnixTimeSeconds();

            // Veri paketini oluştur
            TelemetryMessage msg = new TelemetryMessage(DeviceId, dataType, value, timestamp);

            // OnTelemetryDataPublished olayını tetikle. DeviceManager bunu dinliyor olacak.
            OnTelemetryDataPublished?.Invoke(msg);
            // Debug.Log($"[SmartLamp] ({DeviceId}) Telemetry yayınlandı: {dataType} = {value}"); // Yayın logu genellikle spam olur, DeviceManager loglasın.
        }

        // --- Test Fonksiyonu (Opsiyonel) ---
        // Klavyeden 'L' tuşuna basıldığında lambayı açıp kapatır.
        void Update() 
        { 
            // InputSystem'in hazır olup olmadığını kontrol et
            if (Keyboard.current != null && Keyboard.current.lKey.wasPressedThisFrame) 
            {
                // Doğrudan SetState yerine ReceiveCommand'ı çağırmak daha doğru bir test olur
                ReceiveCommand("toggle", ""); 
            }
        }
    }
}