using UnityEngine;
using System;
using SmartHome.Core;      
using SmartHome.Core.Devices; 
using SmartHome.Core.Data;    

namespace SmartHome.Devices
{
    /*
     * İÇ MEKAN SICAKLIK SENSÖRÜ - GÜNCELLENMİŞ VERSİYON
     * * Artık aynı odadaki SmartAirConditioner'ın etkisini hesaba katıyor.
     */
    public class InternalTemperatureSensor : MonoBehaviour, ISmartDevice //
    {
        [Header("Kimlik (Zorunlu!)")]
        [Tooltip("Bu sensörün benzersiz kimliği (örn: 'salon-sicaklik-sensoru').")]
        [SerializeField] private string deviceId = "oda-sicaklik-sensoru";

        // --- YENİ BÖLÜM: KLİMA BAĞLANTISI ---
        [Header("Bağlı Klima (Opsiyonel)")]
        [Tooltip("Bu sensörün bulunduğu odadaki klima cihazı. Inspector'dan atanmalı.")]
        [SerializeField] private SmartAirConditioner linkedAirConditioner; 
        // --- YENİ BÖLÜM BİTİŞİ ---

        [Header("Oda Ayarları")]
        [Tooltip("Odanın hedeflenen sıcaklığı (°C). Şimdilik sabit, klima bunu dolaylı yoldan etkiler.")]
        [SerializeField] private float targetTemperature = 22.0f; // Bu artık klimanın hedefiyle karışmamalı, belki kaldırılabilir? Şimdilik kalsın.
        [Tooltip("Odanın yalıtım faktörü (0.0 = Pencere açık, 1.0 = Mükemmel yalıtım).")]
        [SerializeField] [Range(0.0f, 1.0f)] private float insulationFactor = 0.8f; 
        [Tooltip("İç sıcaklığın dış sıcaklığa ve klima etkisine ne kadar hızlı tepki verdiği.")]
        [SerializeField] private float temperatureChangeRate = 0.005f; 

        [Header("Yayınlama Ayarları")]
        [Tooltip("Sıcaklık verisinin ne sıklıkla okunup yayınlanacağı (saniye cinsinden).")]
        [SerializeField] private float publishInterval = 60.0f; // 1 dakika

        public string DeviceId => deviceId;
        public DeviceManager Manager { get; private set; }
        public event Action<TelemetryMessage> OnTelemetryDataPublished;

        private float _currentInternalTemperature;
        private bool _isInitialized = false;
        private float _timeSinceLastPublish = 0f;

        public void Initialize(DeviceManager manager)
        {
            this.Manager = manager;
            // Başlangıç sıcaklığını dış sıcaklığa göre daha gerçekçi ayarlayalım
             SimulationManager simManager = SimulationManager.Instance;
             if (simManager != null) {
                 // Dış sıcaklık ile hedef sıcaklık arasında, yalıtıma göre bir başlangıç noktası
                 _currentInternalTemperature = Mathf.Lerp(simManager.CurrentExternalTemperature, targetTemperature, insulationFactor);
             } else {
                 _currentInternalTemperature = targetTemperature; // SimulationManager yoksa hedefle başla
             }
            
            _timeSinceLastPublish = publishInterval; 
            _isInitialized = true;
            Debug.Log($"[InternalTemperatureSensor] Cihaz başlatıldı: {DeviceId} | Başlangıç İç Sıcaklık: {_currentInternalTemperature:F1}°C");
             // Başlangıç sıcaklığını hemen yayınla
            PublishCurrentTemperature(); 
        }

        public void ReceiveCommand(string command, string payload)
        {
            // Bu sensör doğrudan komut almaz (belki kalibrasyon vb. eklenebilir)
             Debug.Log($"[InternalTemperatureSensor] ({DeviceId}) Komut alındı (yoksayıldı): {command}");
        }

        private void Update()
        {
            if (!_isInitialized || Manager == null) return;
            SimulateTemperatureChange();
            _timeSinceLastPublish += Time.deltaTime;
            if (_timeSinceLastPublish >= publishInterval)
            {
                _timeSinceLastPublish = 0f;
                PublishCurrentTemperature();
            }
        }

        /// <summary>
        /// Dış sıcaklığa, yalıtıma VE BAĞLI KLİMANIN ETKİSİNE göre iç sıcaklığın değişimini simüle eder.
        /// </summary>
        private void SimulateTemperatureChange()
        {
             SimulationManager simManager = SimulationManager.Instance;
            if (simManager == null) return; 

            float externalTemp = simManager.CurrentExternalTemperature;

            // 1. Dış sıcaklığın doğal etkisi (yalıtıma bağlı)
            float externalInfluence = (externalTemp - _currentInternalTemperature) * (1.0f - insulationFactor);

            // --- YENİ BÖLÜM: KLİMA ETKİSİ ---
            float acEffect = 0f;
            // Eğer bir klima bağlıysa VE klima başlatıldıysa (Manager'ı varsa yani DeviceManager onu bulduysa)
            if (linkedAirConditioner != null && linkedAirConditioner.Manager != null) 
            {
                // Klimanın o anki ısıtma/soğutma etkisini al
                acEffect = linkedAirConditioner.GetCurrentHeatingCoolingEffect(_currentInternalTemperature);
            }
            // --- YENİ BÖLÜM BİTİŞİ ---

            // Toplam sıcaklık değişimi = (Dış Etki + Klima Etkisi) * Değişim Hızı * Zaman Farkı
            // Klima etkisini daha belirgin yapmak için onu changeRate ile çarpmayabiliriz veya ayrı bir çarpan kullanabiliriz.
            // Şimdilik ikisini de aynı oranda etkilesin.
            float deltaTemperature = (externalInfluence + acEffect) * temperatureChangeRate * Time.deltaTime * 100f; // Etkiyi artırmak için çarpan ekledim (ayarlanabilir)

            _currentInternalTemperature += deltaTemperature;

            // Sıcaklığın mantıksız değerlere gitmesini engelle (opsiyonel)
            // _currentInternalTemperature = Mathf.Clamp(_currentInternalTemperature, -10f, 50f); 
        }


        /// <summary>
        /// O anki iç sıcaklık verisini TelemetryMessage olarak yayınlar.
        /// </summary>
        private void PublishCurrentTemperature()
        {
            string tempValueString = _currentInternalTemperature.ToString("F1");
            long timestamp = DateTimeOffset.UtcNow.ToUnixTimeSeconds();
            TelemetryMessage msg = new TelemetryMessage(DeviceId, "temperature", tempValueString, timestamp);
            OnTelemetryDataPublished?.Invoke(msg); //
        }
    }
}