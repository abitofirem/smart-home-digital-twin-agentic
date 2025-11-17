using UnityEngine;
using System;
using SmartHome.Core;      // DeviceManager için
using SmartHome.Core.Devices; // ISmartDevice arayüzü için
using SmartHome.Core.Data;    // TelemetryMessage için

namespace SmartHome.Devices
{
    /*
     * AKILLI KLİMA KONTROLCÜSÜ (HİBRİT VERSİYON)
     * * ISmartDevice arayüzünü uygular.
     * * "on", "off", "set_temp" VE "set_mode" komutlarını tanır.
     * * "set_temp" veya "set_mode" komutu geldiğinde otomatik olarak açılır.
     * * Isıtma/Soğutma kararını manuel olarak ayarlanan 'mode' üzerinden verir.
     */
    public class SmartAirConditioner : MonoBehaviour, ISmartDevice //
    {
        // Klimanın kullanıcı tarafından ayarlanan modları
        public enum ACMode { Off, Cool, Heat }

        [Header("Kimlik (Zorunlu!)")]
        [Tooltip("Bu klimanın benzersiz kimliği (örn: 'salon-klima').")]
        [SerializeField] private string deviceId = "klima-cihazi";

        [Header("Klima Ayarları")]
        [Tooltip("Klimanın ısıtma/soğutma gücü. İç sıcaklık değişimini etkiler.")]
        [SerializeField] private float powerFactor = 1.0f;
        [Tooltip("Hedef sıcaklığa ulaşıldığında çalışmayı durdurmak için tolerans payı (°C).")]
        [SerializeField] private float temperatureThreshold = 0.5f;

        // ISmartDevice arayüzünden gelen özellikler
        public string DeviceId => deviceId;
        public DeviceManager Manager { get; private set; }
        public event Action<TelemetryMessage> OnTelemetryDataPublished;

        // Klimanın mevcut durumu
        private bool _isOn = false;
        private ACMode _currentMode = ACMode.Cool; // Varsayılan mod: Soğutma
        private float _targetTemperature = 22.0f; // Varsayılan hedef sıcaklık

        // Dışarıdan okunabilir özellikler (Debugging için)
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
            if (!_isOn || _currentMode == ACMode.Off) 
            {
                return 0f; // Klima kapalıysa veya modu 'Off' ise etki yok
            }

            float effect = 0f;
            float tempDifference = currentRoomTemperature - _targetTemperature;

            if (_currentMode == ACMode.Cool)
            {
                // Oda hedef sıcaklıktan DAHA SICAKSA (ve tolerans dışındaysa) soğut
                if (tempDifference > temperatureThreshold)
                {
                    effect = -powerFactor; // Soğutma etkisi (negatif)
                }
            }
            else if (_currentMode == ACMode.Heat)
            {
                // Oda hedef sıcaklıktan DAHA SOĞUKSA (ve tolerans dışındaysa) ısıt
                if (tempDifference < -temperatureThreshold)
                {
                    effect = powerFactor; // Isıtma etkisi (pozitif)
                }
            }
            
            return effect;
        }

        /// <summary>
        /// ISmartDevice arayüzünden gelen başlatma metodu.
        /// </summary>
        public void Initialize(DeviceManager manager)
        {
            this.Manager = manager;
            PublishAllStatusTelemetry(); // Başlangıç durumunu yayınla
            Debug.Log($"[SmartAirConditioner] Cihaz başlatıldı: {DeviceId}");
        }

        /// <summary>
        /// ISmartDevice arayüzünden gelen komut işleme metodu.
        /// "set_mode" komutu GERİ EKLENDİ.
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
                    if (!_isOn) { _isOn = false; statusChanged = true; }
                    break;
                
                // --- "set_mode" KOMUTU GERİ EKLENDİ ---
                case "set_mode":
                    ACMode newMode;
                    // Gelen payload'u (örn: "HEAT") ACMode enum'una çevirmeye çalış
                    if (Enum.TryParse<ACMode>(payload, true, out newMode)) 
                    {
                         if (_currentMode != newMode) { _currentMode = newMode; statusChanged = true;}
                         // Eğer ayarlanan mod "Off" değilse, klimayı da otomatik olarak AÇ
                         if (newMode != ACMode.Off && !_isOn) { _isOn = true; statusChanged = true; }
                    }
                    else { Debug.LogWarning($"[SmartAC] ({DeviceId}) Bilinmeyen set_mode payload'u: {payload}"); }
                    break;

                case "set_temp":
                case "set_temperature":
                    if (float.TryParse(payload, System.Globalization.NumberStyles.Any, System.Globalization.CultureInfo.InvariantCulture, out float newTemp))
                    {
                        if (Mathf.Abs(_targetTemperature - newTemp) > 0.01f)
                        {
                             _targetTemperature = newTemp;
                             statusChanged = true;
                             _isOn = true; // --- OTOMATİK AÇMA --- Sıcaklık ayarı gelince klimayı aç
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
        /// Klimanın ana durum bilgilerini telemetri olarak yayınlar.
        /// </summary>
        private void PublishAllStatusTelemetry()
        {
            if (Manager == null) return; // Henüz başlatılmadıysa gönderme
            PublishTelemetry("isOn", _isOn.ToString().ToLower());
            PublishTelemetry("mode", _currentMode.ToString().ToLower()); // Artık "workingMode" değil, kullanıcının ayarladığı "mode"
            PublishTelemetry("targetTemperature", _targetTemperature.ToString("F1"));
        }

        /// <summary>
        /// Belirli bir veri tipini telemetri olarak yayınlar.
        /// </summary>
        private void PublishTelemetry(string dataType, string value)
        {
            if (Manager == null) return; 
            long timestamp = DateTimeOffset.UtcNow.ToUnixTimeSeconds();
            TelemetryMessage msg = new TelemetryMessage(DeviceId, dataType, value, timestamp);
            OnTelemetryDataPublished?.Invoke(msg);
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