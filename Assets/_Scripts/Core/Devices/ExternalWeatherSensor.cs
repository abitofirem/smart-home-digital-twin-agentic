using UnityEngine;
using System;
using SmartHome.Core;      
using SmartHome.Core.Devices; 
using SmartHome.Core.Data;    

namespace SmartHome.Devices
{
    public class ExternalWeatherSensor : MonoBehaviour, ISmartDevice 
    {
        [Header("Kimlik (Zorunlu!)")]
        [SerializeField] private string deviceId = "dis-hava-sensoru";

        [Header("Sensör Ayarları")]
        [SerializeField] private float publishInterval = 60.0f; 

        public string DeviceId => deviceId;
        public DeviceManager Manager { get; private set; }
        public event Action<TelemetryMessage> OnTelemetryDataPublished;

        private float _timeSinceLastPublish = 0f;
        private bool _isInitialized = false; // Başlatılıp başlatılmadığını takip edelim

        public void Initialize(DeviceManager manager)
        {
            this.Manager = manager;
            _timeSinceLastPublish = publishInterval; 
            _isInitialized = true; // Başlatıldı olarak işaretle
            Debug.Log($"[ExternalWeatherSensor] Cihaz başlatıldı: {DeviceId}");
        }

        public void ReceiveCommand(string command, string payload)
        {
            Debug.Log($"[ExternalWeatherSensor] ({DeviceId}) Komut alındı (yoksayıldı): {command}");
        }

        private void Update()
        {
            // Eğer Initialize henüz çağrılmadıysa veya Manager null ise bir şey yapma
            if (!_isInitialized || Manager == null) 
            {
                // Debug.Log("[ExternalWeatherSensor] Update çağrıldı ama henüz başlatılmadı."); // Bu log spam yapabilir
                return;
            }

            _timeSinceLastPublish += Time.deltaTime;
            
             // ---- YENİ DEBUG LOG 1 ----
             // Debug.Log($"[ExternalWeatherSensor] Timer: {_timeSinceLastPublish:F2} / {publishInterval:F2}"); // Zamanlayıcıyı görmek için (spam yapabilir)

            if (_timeSinceLastPublish >= publishInterval)
            {
                 // ---- YENİ DEBUG LOG 2 ----
                 Debug.Log($"[ExternalWeatherSensor] ({DeviceId}) Yayınlama zamanı geldi! Sıcaklık okunacak.");
                 
                _timeSinceLastPublish = 0f; 
                ReadAndPublishTemperature(); 
            }
        }

        private void ReadAndPublishTemperature()
        {
             // ---- YENİ DEBUG LOG 3 ----
             Debug.Log($"[ExternalWeatherSensor] ({DeviceId}) ReadAndPublishTemperature fonksiyonu çağrıldı.");
             
            SimulationManager simManager = SimulationManager.Instance;
            if (simManager == null)
            {
                Debug.LogWarning($"[ExternalWeatherSensor] ({DeviceId}) SimulationManager bulunamadı!");
                return;
            }

            float currentTemp = simManager.CurrentExternalTemperature;
            string tempValueString = currentTemp.ToString("F1"); 
            long timestamp = DateTimeOffset.UtcNow.ToUnixTimeSeconds();
            TelemetryMessage msg = new TelemetryMessage(DeviceId, "temperature", tempValueString, timestamp);

             // ---- YENİ DEBUG LOG 4 ----
             Debug.Log($"[ExternalWeatherSensor] ({DeviceId}) Telemetry paketi oluşturuldu: {tempValueString}°C. Event tetiklenecek.");

            // Olayı tetikle
            try 
            {
                OnTelemetryDataPublished?.Invoke(msg);
            }
            catch (Exception ex)
            {
                // Eğer event'i dinleyen (DeviceManager) bir hata fırlatırsa yakala
                Debug.LogError($"[ExternalWeatherSensor] ({DeviceId}) OnTelemetryDataPublished event'i tetiklenirken hata oluştu: {ex.Message}");
            }
        }
    }
}