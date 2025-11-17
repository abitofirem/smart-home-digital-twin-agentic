using System;

namespace SmartHome.Core.Data
{
    /*
     * COMMAND MESSAGE (GELEN KOMUT PAKETİ) - FİNAL VERSİYON
     * * Backend'in (client.py) gönderdiği JSON formatı ile tam uyumludur.
     * * Örnek JSON: {"deviceId":"salon-lamba-1", "command":"on", "payload":""}
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