using System;

namespace SmartHome.Core.Data
{
    /*
     * COMMAND MESSAGE (GELEN KOMUT PAKETİ) - FİNAL VERSİYON
        {"deviceId":"cihaz_id", "command":"", "payload":""}
     */
    [Serializable]
    public class CommandMessage
    {
        /// <summary>
        /// Komutun hedeflendiği cihazın benzersiz kimliği.
        /// Bu alan, gelen JSON mesajında ZORUNLUDUR.
        /// </summary>
        public string deviceId;

        /// <summary>
        /// Yapılacak işlemin adı (örn: "on", "off", "set_temp").
        /// </summary>
        public string command;

        /// <summary>
        /// Komutla birlikte gönderilen ek veri (isteğe bağlı).
        /// </summary>
        public string payload;
    }
}