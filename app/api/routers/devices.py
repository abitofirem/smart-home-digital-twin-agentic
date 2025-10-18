# Konum: app/api/routers/devices.py

from fastapi import APIRouter, Depends, HTTPException
# SQLAlchemy importları kaldırıldı
from typing import List, Dict, Any
import json
import datetime
# Firebase ve Firestore importları
from firebase_admin import firestore 
from firebase_admin.firestore import client as FirestoreClient # get_db'den gelen tip

from app.db import schemas
from app.db.database import get_db
from app.mqtt.client import mqtt_client, TOPIC_UNITY_UPDATES # MQTT yayınlamak için hala lazım

router = APIRouter(
    prefix="/devices",
    tags=["Devices"],
)

# Yardımcı fonksiyon: Firestore dokümanını Pydantic şemasına uyan bir dict'e dönüştürür.
def doc_to_device(doc: Any) -> Dict[str, Any]:
    """Firestore dokümanını cihaz şemasına dönüştürür."""
    data = doc.to_dict()
    timestamp = data.get("last_updated")
    
    return {
        # doc.id, Firestore'un benzersiz string ID'sidir.
        "id": doc.id, 
        "name": data.get("name"),
        "status": data.get("status", "off"),
        # Firestore Timestamp objesini ISO 8601 formatına dönüştürme
        "last_updated": timestamp.isoformat() if timestamp else datetime.now().isoformat()
    }

# ------------------------------------------------------------------
# 1. Cihaz Oluşturma (POST /devices/)
# ------------------------------------------------------------------
@router.post("/", response_model=schemas.Device)
def create_device(device: schemas.DeviceCreate, db: FirestoreClient = Depends(get_db)):
    
    new_device_data = {
        "name": device.name,
        "status": device.status or "off",
        "last_updated": firestore.SERVER_TIMESTAMP # Sunucuda otomatik zaman damgası
    }
    
    # Firestore'a ekle ve eklenen dokümanın referansını al
    # [1] ile DocRef objesini alırız.
    doc_ref = db.collection("devices").add(new_device_data)[1] 
    
    # Eklenen dokümanı tekrar oku (id ve sunucu zaman damgasını almak için)
    doc = doc_ref.get()

    return doc_to_device(doc)

# ------------------------------------------------------------------
# 2. Cihazları Listeleme (GET /devices/)
# ------------------------------------------------------------------
@router.get("/", response_model=List[schemas.Device])
def get_devices(db: FirestoreClient = Depends(get_db)):
    
    devices_ref = db.collection("devices").stream() # Tüm dokümanları çeker
    
    # Her dokümanı Pydantic şemasına dönüştür
    devices = [doc_to_device(doc) for doc in devices_ref]
    return devices

# ------------------------------------------------------------------
# 3. Cihaz Durumunu Güncelleme (PUT /devices/{device_id})
# ------------------------------------------------------------------
@router.put("/{device_id}", response_model=schemas.Device)
def update_device(device_id: str, new_status: str, db: FirestoreClient = Depends(get_db)):
    # device_id artık bir string (Firestore Document ID)
    
    device_ref = db.collection("devices").document(device_id)
    doc = device_ref.get()

    if not doc.exists:
        raise HTTPException(status_code=404, detail="Device not found")
        
    # Firestore dokümanını güncelle
    device_ref.update({
        "status": new_status,
        "last_updated": firestore.SERVER_TIMESTAMP
    })

    # Güncel veriyi tekrar çek
    updated_doc = device_ref.get()
    
    # MQTT Yayınlama (Unity'ye sinyal gönderme)
    update_payload = json.dumps({
        "deviceId": updated_doc.id, 
        "name": updated_doc.to_dict().get("name"),
        "newStatus": new_status
    })
    mqtt_client.publish(TOPIC_UNITY_UPDATES, update_payload)
    print(f"[+] Published to {TOPIC_UNITY_UPDATES} via API: {update_payload}")
    
    return doc_to_device(updated_doc)

@router.delete("/{device_id}")
def delete_device(device_id: str, db: FirestoreClient = Depends(get_db)):
    device_ref = db.collection("devices").document(device_id)
    if not device_ref.get().exists:
        raise HTTPException(status_code=404, detail="Device not found")
    
    device_ref.delete()
    return {"message": f"Device {device_id} deleted successfully"}