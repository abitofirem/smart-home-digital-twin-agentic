using System;

namespace SmartHome.Core.Data
{
    /*
     * COMMAND MESSAGE (GELEN KOMUT PAKETİ) - newStatus FORMATI
     * * Arkadaşının gönderdiği JSON formatına ('deviceId', 'newStatus') uygun hale getirildi.
     * * Örnek JSON: {"deviceId":"salon-ayakli-lamba-1", "name":"...", "newStatus":"ON"}
     */
    [Serializable]
    public class CommandMessage
    {
        /// <summary>
        /// Komutun hedeflendiği cihazın benzersiz kimliği.
        /// </summary>
        public string deviceId;

        /// <summary>
        /// Cihazın yeni durumu (örn: "ON", "OFF").
        /// Arkadaşının gönderdiği JSON'daki alan adıyla eşleşmeli.
        /// </summary>
        public string newStatus; // 'status' yerine 'newStatus' kullanıyoruz

        // Opsiyonel: JSON'da 'name' gibi başka alanlar varsa ama kullanmayacaksak
        // buraya eklememize gerek yok, Newtonsoft.Json bunları görmezden gelir.
    }
}