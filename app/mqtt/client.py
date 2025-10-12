# Konum: app/mqtt/client.py

import paho.mqtt.client as mqtt
import os
import json
import ssl
from app.db.database import SessionLocal
from app.db.models import Device, Log

# Ortam değişkenlerinden MQTT ayarlarını oku
MQTT_BROKER = os.getenv("MQTT_BROKER_HOST")
MQTT_PORT = int(os.getenv("MQTT_BROKER_PORT", 8883))
MQTT_USERNAME = os.getenv("MQTT_USERNAME")
MQTT_PASSWORD = os.getenv("MQTT_PASSWORD")

TOPIC_ANDROID_COMMANDS = "commands/android/set-device-status"
TOPIC_UNITY_UPDATES = "updates/unity/device-status"
TOPIC_UNITY_SENSORS = "data/unity/sensor-readings" 


mqtt_client = mqtt.Client()

def on_connect(client, userdata, flags, rc, properties=None):
    if rc == 0:
        print(f"[+] HiveMQ connected successfully to {MQTT_BROKER}")

        #Android Komutları
        client.subscribe(TOPIC_ANDROID_COMMANDS)
        print(f"[+] Subscribed to: {TOPIC_ANDROID_COMMANDS}")

        #Unity Sensör Verileri
        client.subscribe(TOPIC_UNITY_SENSORS)
        print(f"[+] Subscribed to: {TOPIC_UNITY_SENSORS}")
    else:
        print(f"[!] Connection failed with code {rc}")

def on_message(client, userdata, msg):
    print(f"[+] MQTT Received: {msg.topic}")
    if msg.topic == TOPIC_ANDROID_COMMANDS:
        db = SessionLocal()
        try:
            payload_str = msg.payload.decode().strip()
            print(f"    Payload: {payload_str}")
            data = json.loads(payload_str)
            
            device_id = data.get("deviceId")
            new_status = data.get("status")

            if not device_id or new_status is None:
                print("[!] Eksik veri: deviceId veya status bulunamadı.")
                return

            db_device = db.query(Device).filter(Device.id == device_id).first()

            if db_device:
                print(f"    Updating Device {device_id} -> {new_status}")
                db_device.status = new_status
                db.commit()
                db.refresh(db_device)

                update_payload = json.dumps({
                    "deviceId": db_device.id,
                    "name": db_device.name,
                    "newStatus": db_device.status
                })
                client.publish(TOPIC_UNITY_UPDATES, update_payload)
                print(f"[+] Published to {TOPIC_UNITY_UPDATES}: {update_payload}")

            elif msg.topic == TOPIC_UNITY_SENSORS:
                device_id = data.get("deviceId")
                sensor_type = data.get("sensorType")
                value = data.get("value")

                if not device_id or not sensor_type or value is None:
                    print("[!] Eksik veri: deviceId, sensorType veya value bulunamadı.")
                    return
                
                log_message = f"Sensor {sensor_type} reported value {value}"

                db_log = Log(
                    devide_id=device_id, message=log_message, level=sensor_type.upper()
                )
                db.add(db_log)
                db.commit()
                print(f"[LOG] Log saved for Device {device_id}: {log_message}")
            else:
                print(f"[!] Device {device_id} not found in DB")
        except json.JSONDecodeError:
            print("[!] Hata: Gelen veri geçerli bir JSON değil.")
        except Exception as e:
            print(f"[!] MQTT işlem hatası: {e}")
        finally:
            db.close()

def start_mqtt_client():
    mqtt_client.username_pw_set(MQTT_USERNAME, MQTT_PASSWORD)
    mqtt_client.tls_set(tls_version=ssl.PROTOCOL_TLS)
    mqtt_client.on_connect = on_connect
    mqtt_client.on_message = on_message
    try:
        print(f"[*] Connecting to HiveMQ Cloud at {MQTT_BROKER}:{MQTT_PORT}...")
        mqtt_client.connect(MQTT_BROKER, MQTT_PORT, 60)
        mqtt_client.loop_start()
    except Exception as e:
        print(f"[!] HiveMQ Bağlantı Hatası: {e}")