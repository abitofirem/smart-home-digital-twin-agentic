using UnityEngine;
using System;
using SmartHome.Core;      // DeviceManager için
using SmartHome.Core.Devices; // ISmartDevice arayüzü için
using SmartHome.Core.Data;    // TelemetryMessage için

namespace SmartHome.Devices
{
    /*
     * AKILLI KLİMA KONTROLCÜSÜ
     * * ISmartDevice arayüzünü uygular.
     * * DeviceManager tarafından yönetilir.
     * * "on", "off", "set_temp", "set_mode" (cool/heat) komutlarını alır.
     * * O anki çalışma durumuna göre bir ısıtma/soğutma etkisi sağlar.
     */
    public class SmartAirConditioner : MonoBehaviour, ISmartDevice //
    {
        // Enum: Klimanın çalışabileceği modlar
        public enum ACMode { Off, Cool, Heat }

        [Header("Kimlik (Zorunlu!)")]
        [Tooltip("Bu klimanın benzersiz kimliği (örn: 'salon-klima').")]
        [SerializeField] private string deviceId = "klima-cihazi";

        [Header("Klima Ayarları")]
        [Tooltip("Klimanın ısıtma/soğutma gücü. İç sıcaklık değişimini etkiler.")]
        [SerializeField] private float powerFactor = 1.0f; // Ne kadar güçlü ısıtıp soğuttuğu
        [Tooltip("Hedef sıcaklığa ulaşıldığında çalışmayı durdurmak için tolerans payı (°C).")]
        [SerializeField] private float temperatureThreshold = 0.5f; // Hedefin +/- 0.5 derece yakınına gelince dursun

        // ISmartDevice arayüzünden gelen özellikler
        public string DeviceId => deviceId;
        public DeviceManager Manager { get; private set; }
        public event Action<TelemetryMessage> OnTelemetryDataPublished;

        // Klimanın mevcut durumu
        private bool _isOn = false;
        private ACMode _currentMode = ACMode.Cool; // Varsayılan mod: Soğutma
        private float _targetTemperature = 22.0f; // Varsayılan hedef sıcaklık

        // Dışarıdan okunabilir özellikler
        public bool IsOn => _isOn;
        public ACMode CurrentMode => _currentMode;
        public float TargetTemperature => _targetTemperature;

        /// <summary>
        /// Klimanın o anki ısıtma/soğutma etkisini döndürür.
        /// InternalTemperatureSensor bu değeri kullanacak.
        /// </summary>
        /// <returns>Pozitif: Isıtıyor, Negatif: Soğutuyor, 0: Kapalı veya hedefte.</returns>
        public float GetCurrentHeatingCoolingEffect(float currentRoomTemperature)
        {
            if (!_isOn) return 0f; // Klima kapalıysa etki yok

            float effect = 0f;
            float tempDifference = currentRoomTemperature - _targetTemperature;

            if (_currentMode == ACMode.Cool)
            {
                // Oda hedef sıcaklıktan DAHA SICAKSA ve fark toleransın DIŞINDAYSA soğut
                if (tempDifference > temperatureThreshold)
                {
                    effect = -powerFactor; // Soğutma etkisi (negatif)
                }
            }
            else if (_currentMode == ACMode.Heat)
            {
                // Oda hedef sıcaklıktan DAHA SOĞUKSA ve fark toleransın DIŞINDAYSA ısıt
                if (tempDifference < -temperatureThreshold)
                {
                    effect = powerFactor; // Isıtma etkisi (pozitif)
                }
            }
            // Debug.Log($"[SmartAC] ({DeviceId}) Effect: {effect} (Current: {currentRoomTemperature:F1}, Target: {_targetTemperature:F1}, Mode: {_currentMode})");
            return effect;
        }


        /// <summary>
        /// ISmartDevice arayüzünden gelen başlatma metodu.
        /// </summary>
        public void Initialize(DeviceManager manager)
        {
            this.Manager = manager;
            // Başlangıç durumunu telemetri olarak gönderelim
            PublishAllStatusTelemetry();
            Debug.Log($"[SmartAirConditioner] Cihaz başlatıldı: {DeviceId}");
        }

        /// <summary>
        /// ISmartDevice arayüzünden gelen komut işleme metodu.
        /// </summary>
        public void ReceiveCommand(string command, string payload)
        {
            bool statusChanged = false; // Telemetri göndermek gerekip gerekmediğini takip et

            switch (command.ToLower())
            {
                case "on":
                    if (!_isOn) { _isOn = true; statusChanged = true; }
                    break;
                case "off":
                    if (_isOn) { _isOn = false; statusChanged = true; }
                    break;
                case "set_state": // Hem on/off hem de modu ayarlayabilir (örn: payload="cool")
                    string targetState = payload.ToLower();
                    if (targetState == "on") { if (!_isOn) { _isOn = true; statusChanged = true; } }
                    else if (targetState == "off") { if (_isOn) { _isOn = false; statusChanged = true; } }
                    else if (Enum.TryParse<ACMode>(targetState, true, out ACMode newMode)) // Modu ayarla (cool/heat)
                    {
                         if (_currentMode != newMode) { _currentMode = newMode; statusChanged = true;}
                         if (!_isOn) { _isOn = true; statusChanged = true;} // Mod değişirse açılsın
                    }
                    else { Debug.LogWarning($"[SmartAC] ({DeviceId}) Bilinmeyen set_state payload'u: {payload}"); }
                    break;
                case "set_mode": // Sadece modu ayarla (cool/heat)
                    if (Enum.TryParse<ACMode>(payload, true, out ACMode modeToSet))
                    {
                         if (_currentMode != modeToSet) { _currentMode = modeToSet; statusChanged = true;}
                         if (!_isOn) { _isOn = true; statusChanged = true;} // Mod değişirse açılsın
                    }
                     else { Debug.LogWarning($"[SmartAC] ({DeviceId}) Bilinmeyen set_mode payload'u: {payload}"); }
                    break;
                case "set_temp":
                case "set_temperature":
                    if (float.TryParse(payload, System.Globalization.NumberStyles.Any, System.Globalization.CultureInfo.InvariantCulture, out float newTemp))
                    {
                        if (Mathf.Abs(_targetTemperature - newTemp) > 0.01f) // Küçük farkları önemseme
                        {
                             _targetTemperature = newTemp;
                             statusChanged = true;
                        }
                    }
                     else { Debug.LogWarning($"[SmartAC] ({DeviceId}) Geçersiz set_temp payload'u: {payload}"); }
                    break;
                default:
                    Debug.LogWarning($"[SmartAirConditioner] ({DeviceId}) Bilinmeyen komut alındı: {command}");
                    break;
            }

            // Eğer durum değiştiyse, yeni durumu logla ve telemetri gönder
            if (statusChanged)
            {
                LogCurrentStatus();
                PublishAllStatusTelemetry();
            }
        }

        /// <summary>
        /// Klimanın mevcut durumunu (On/Off, Mod, Hedef Sıcaklık) telemetri olarak yayınlar.
        /// </summary>
        private void PublishAllStatusTelemetry()
        {
            PublishTelemetry("isOn", _isOn.ToString().ToLower());
            PublishTelemetry("mode", _currentMode.ToString().ToLower());
            PublishTelemetry("targetTemperature", _targetTemperature.ToString("F1"));
        }

        /// <summary>
        /// Belirli bir veri tipini telemetri olarak yayınlar.
        /// </summary>
        private void PublishTelemetry(string dataType, string value)
        {
            long timestamp = DateTimeOffset.UtcNow.ToUnixTimeSeconds();
            TelemetryMessage msg = new TelemetryMessage(DeviceId, dataType, value, timestamp);
            OnTelemetryDataPublished?.Invoke(msg); //
        }
        
        /// <summary>
        /// Mevcut durumu konsola loglar.
        /// </summary>
        private void LogCurrentStatus()
        {
             Debug.Log($"[SmartAirConditioner] ({DeviceId}) Durum Güncellendi -> IsOn: {_isOn}, Mode: {_currentMode}, TargetTemp: {_targetTemperature:F1}°C");
        }
    }
}