using System;
using SmartHome.Core.Data; // TelemetryMessage sınıfının adresi
using SmartHome.Core;      // DeviceManager sınıfının adresi

// Cihazlarla ilgili tüm arayüzleri ve sınıfları bu namespace içine alacağız.
namespace SmartHome.Core.Devices
{
    /*
     * ISMARTDEVICE ARAYÜZÜ (SÖZLEŞMESİ)
     * * Bu arayüz, projemizdeki tüm "akıllı cihaz" scriptlerinin (Lamba, Termostat vb.)
     * uymak zorunda olduğu TEK KURAL SETİDİR.
     * * DeviceManager, bir cihazın Lamba mı Termostat mı olduğunu bilmeden,
     * bu arayüz üzerinden onunla konuşabilir.
     */
    public interface ISmartDevice
    {
        /// <summary>
        /// Cihazın veritabanındaki benzersiz kimliği (Örn: "salon-lamba-1").
        /// Bu ID, cihaz script'inin Inspector panelinden atanmalıdır.
        /// </summary>
        string DeviceId { get; }

        /// <summary>
        /// Cihazı, ana yönetici olan DeviceManager ile tanıştıran metot.
        /// Oyun başlarken DeviceManager tarafından her cihaz için bir kez çağrılır.
        /// </summary>
        /// <param name="manager">Ana DeviceManager'ın referansı.</param>
        void Initialize(DeviceManager manager);

        /// <summary>
        /// Dışarıdan gelen komutları işlemek için DeviceManager tarafından çağrılan metot.
        /// </summary>
        /// <param name="command">Gelen komutun adı (örn: "on", "set_temp").</param>
        /// <param name="payload">Komutla gelen ek veri (örn: "", "22.5").</param>
        void ReceiveCommand(string command, string payload);

        /// <summary>
        /// Cihaz (özellikle sensörler) yeni bir veri ürettiğinde veya önemli bir durum
        /// değişikliği olduğunda tetiklenen olay (event). DeviceManager bu olayı dinler.
        /// </summary>
        event Action<TelemetryMessage> OnTelemetryDataPublished;
    }
}