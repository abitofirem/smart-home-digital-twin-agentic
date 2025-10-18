# Konum: app/mqtt/client.py

import paho.mqtt.client as mqtt
import os
import json
import ssl
from typing import Optional, Any
from datetime import datetime

# Firebase Modülleri
from firebase_admin import firestore
from firebase_admin.firestore import client as FirestoreClient
# Firebase istemcisini almak için database.py'den import et
from app.db.database import get_db, db_firestore as firebase_client 


# Ortam değişkenlerinden MQTT ayarlarını oku
MQTT_BROKER = os.getenv("MQTT_BROKER_HOST")
MQTT_PORT = int(os.getenv("MQTT_BROKER_PORT", 8883))
MQTT_USERNAME = os.getenv("MQTT_USERNAME")
MQTT_PASSWORD = os.getenv("MQTT_PASSWORD")

# MQTT Konuları
TOPIC_ANDROID_COMMANDS = "commands/android/set-device-status"
TOPIC_UNITY_UPDATES = "updates/unity/device-status"
TOPIC_UNITY_SENSORS = "data/unity/sensor-readings"

mqtt_client = mqtt.Client()

def on_connect(client, userdata, flags, rc, properties=None):
    if rc == 0:
        print(f"[+] MQTT Broker'a başarıyla bağlandı: {MQTT_BROKER}")
        client.subscribe(TOPIC_ANDROID_COMMANDS)
        print(f"[+] Konuya abone olundu: {TOPIC_ANDROID_COMMANDS}")
        client.subscribe(TOPIC_UNITY_SENSORS)
        print(f"[+] Konuya abone olundu: {TOPIC_UNITY_SENSORS}")
    else:
        print(f"[!] Bağlantı hatası, kod: {rc}")

def handle_android_command(db: FirestoreClient, data: dict, client: mqtt.Client):
    """Android komutlarını işler, Firebase'de cihazı günceller ve DeviceLog kaydı oluşturur."""
    device_id: Optional[str] = data.get("deviceId") # ID artık string
    new_status: Optional[str] = data.get("status")
    command_source: str = "ANDROID"

    if not device_id is not None:
        device_id = str(device_id)
        return

    # 1. Cihazın Mevcut Durumunu Kontrol Et ve Çek
    device_ref = db.collection("devices").document(device_id)
    doc = device_ref.get()

    if not doc.exists:
        print(f"[!] Cihaz {device_id} Firebase'de bulunamadı.")
        return
    
    device_data = doc.to_dict()
    old_status: str = device_data.get("status", "off")
    device_name: str = device_data.get("name", "Bilinmeyen Cihaz")

    try:
        # 2. DEVICES_LOG KAYDI OLUŞTUR (Firebase'de Transaction kullanmıyoruz)
        db.collection("devices_log").add({
            "device_id": device_id,
            "command_source": command_source,
            "old_status": old_status,
            "new_status": new_status,
            "timestamp": firestore.SERVER_TIMESTAMP
        })
        print(f"    [LOG] DeviceLog kaydı oluşturuldu (Cihaz {device_id}): {old_status} -> {new_status}")
    except Exception as e:
        print(f"[!] DeviceLog Oluşturma Hatası: {e}")
        return
    
    # 3. CİHAZ DURUMUNU GÜNCELLE (Firebase'de "devices" koleksiyonu)
    try:
        device_ref.update({
            "status": new_status,
            "last_updated": firestore.SERVER_TIMESTAMP
        })
        print(f"    [COMMAND] Device '{device_name}' updated in Firebase: {old_status} -> {new_status}")

        # 4. Unity'ye YAYINLA
        update_payload = json.dumps({
            "deviceId": device_id,
            "name": device_name,
            "newStatus": new_status
        })
        client.publish(TOPIC_UNITY_UPDATES, update_payload)
        print(f"    [PUBLISH] Unity'ye cihaz durumu güncellendi ({TOPIC_UNITY_UPDATES}).")
        
    except Exception as e:
        print(f"[!] Firebase Update Hatası: {e}")


def handle_unity_sensor(db: FirestoreClient, data: dict):
    """Unity'den gelen sensör verilerini işler ve SensorLog'a kaydeder."""
    device_id: Optional[str] = data.get("deviceId")
    sensor_type: Optional[str] = data.get("sensorType")
    value: Optional[Any] = data.get("value") 

    if not device_id or not sensor_type or value is None:
        print("[!] Eksik sensör verisi: deviceId, sensorType veya value bulunamadı.")
        return

    # Değer tipi kontrolü ve kaydı
    float_value: Optional[float] = None
    raw_value_str: Optional[str] = None
    
    try:
        # Sayısal değerleri float olarak kaydetmeye çalış
        float_value = float(value)
    except (TypeError, ValueError):
        # Sayısal değilse, string olarak kaydet (örn: "detected")
        raw_value_str = str(value)
        
    # SENSORS_LOG KAYDI OLUŞTUR
    db.collection("sensors_log").add({
        "device_id": device_id,
        "sensor_type": sensor_type.upper(),
        "value": float_value,
        "raw_value": raw_value_str,
        "timestamp": firestore.SERVER_TIMESTAMP
    })
    
    print(f"    [SENSOR LOG] {sensor_type.upper()} verisi Firebase'e kaydedildi (Cihaz {device_id}).")

def on_message(client, userdata, msg):
    print(f"[+] MQTT Mesajı Alındı: {msg.topic}")
    
    # Firestore istemcisini al (database.py'den)
    db = firebase_client 

    if db is None:
        print("[!] Kritik Hata: Firebase bağlantısı kurulamadığı için MQTT mesajı işlenemedi.")
        return

    try:
        payload_str = msg.payload.decode().strip()
        data = json.loads(payload_str)
        
        if msg.topic == TOPIC_ANDROID_COMMANDS:
            handle_android_command(db, data, client)
            
        elif msg.topic == TOPIC_UNITY_SENSORS:
            handle_unity_sensor(db, data)
            
        else:
            print(f"[!] Bilinmeyen konu: {msg.topic}")

    except json.JSONDecodeError:
        print("[!] Hata: Gelen veri geçerli bir JSON değil.")
    except Exception as e:
        print(f"[!] MQTT İşlem Hatası: {e}")
        import traceback
        traceback.print_exc() 
    finally:
        # NoSQL'de Session kapatma (db.close()) gerekmez.
        pass

def start_mqtt_client():
    # Firebase'i başlat (Zaten database.py'de yapılıyor)
    mqtt_client = mqtt.Client(client_id="FastAPI_Backend_" + os.uname()[1])
    mqtt_client.username_pw_set(MQTT_USERNAME, MQTT_PASSWORD)
    mqtt_client.tls_set(tls_version=ssl.PROTOCOL_TLSv1_2) 
    mqtt_client.on_connect = on_connect
    mqtt_client.on_message = on_message
    
    try:
        print(f"[*] MQTT Broker'a bağlanılıyor: {MQTT_BROKER}:{MQTT_PORT}...")
        mqtt_client.connect(MQTT_BROKER, MQTT_PORT, 60)
        mqtt_client.loop_start()
    except Exception as e:
        print(f"[!] MQTT Bağlantı Hatası: {e}")