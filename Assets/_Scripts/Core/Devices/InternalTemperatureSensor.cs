using UnityEngine;
using System;
using SmartHome.Core;      
using SmartHome.Core.Devices; 
using SmartHome.Core.Data;    

namespace SmartHome.Devices
{
    /*
     * İÇ MEKAN SICAKLIK SENSÖRÜ - GÜNCELLENMİŞ VERSİYON
     * * Dış sıcaklık etkisi azaltıldı (yalıtım artırıldı).
     * * Başlangıç sıcaklığı 24 dereceye sabitlendi.
     */
    public class InternalTemperatureSensor : MonoBehaviour, ISmartDevice 
    {
        [Header("Kimlik (Zorunlu!)")]
        [Tooltip("Bu sensörün benzersiz kimliği (örn: 'salon-sicaklik-sensoru').")]
        [SerializeField] private string deviceId = "oda-sicaklik-sensoru";

        [Header("Bağlı Klima (Opsiyonel)")]
        [Tooltip("Bu sensörün bulunduğu odadaki klima cihazı. Inspector'dan atanmalı.")]
        [SerializeField] private SmartAirConditioner linkedAirConditioner; 

        [Header("Oda Ayarları")]
        [Tooltip("Odanın hedeflenen sıcaklığı (°C). Bu, klimanın hedefidir.")]
        [SerializeField] private float targetTemperature = 22.0f; // Bu, klimanın varsayılan hedefi olmaya devam edebilir
        
        // --- DEĞİŞİKLİK 1: YALITIM ARTIRILDI ---
        [Tooltip("Odanın yalıtım faktörü (0.0 = Etki çok, 1.0 = Etki yok).")]
        [SerializeField] [Range(0.0f, 1.0f)] private float insulationFactor = 0.95f; // 0.8'den 0.98'e yükseltildi (Dışarısı çok az etkilesin)
        
        // --- DEĞİŞİKLİK 2: DEĞİŞİM YAVAŞLATILDI ---
        [Tooltip("İç sıcaklığın dış sıcaklığa ve klima etkisine ne kadar hızlı tepki verdiği.")]
        [SerializeField] private float temperatureChangeRate = 0.001f; // 0.005'ten 0.001'e düşürüldü (Çok yavaş etkilensin)

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
            
            // --- DEĞİŞİKLİK 3: BAŞLANGIÇ SICAKLIĞI 24 DERECEYE SABİTLENDİ ---
            _currentInternalTemperature = 24.0f; // Başlangıç sıcaklığı 24 derece olarak ayarlandı.
            
            _timeSinceLastPublish = publishInterval; 
            _isInitialized = true;
            Debug.Log($"[InternalTemperatureSensor] Cihaz başlatıldı: {DeviceId} | Başlangıç İç Sıcaklık: {_currentInternalTemperature:F1}°C");
             // Başlangıç sıcaklığını hemen yayınla
            PublishCurrentTemperature(); 
        }

        public void ReceiveCommand(string command, string payload)
        {
             // Bu sensör doğrudan komut almaz
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
            // (1.0f - 0.98f) = 0.02f -> Dış sıcaklık artık 50 kat daha az etkili.
            float externalInfluence = (externalTemp - _currentInternalTemperature) * (1.0f - insulationFactor);

            // 2. Klima etkisi
            float acEffect = 0f;
            if (linkedAirConditioner != null && linkedAirConditioner.Manager != null) 
            {
                acEffect = linkedAirConditioner.GetCurrentHeatingCoolingEffect(_currentInternalTemperature);
            }

            // Toplam sıcaklık değişimi = (Dış Etki + Klima Etkisi) * Değişim Hızı * Zaman Farkı
            // temperatureChangeRate'i de 0.001'e düşürdük, bu yüzden değişim ÇOK yavaş olacak.
            float deltaTemperature = (externalInfluence + acEffect) * temperatureChangeRate * Time.deltaTime * 100f; 

            _currentInternalTemperature += deltaTemperature;
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