# Konum: app/api/routers/devices.py

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
import json

from app.db import models, schemas
from app.db.database import get_db
from app.mqtt.client import mqtt_client, TOPIC_UNITY_UPDATES

router = APIRouter(
    prefix="/devices",
    tags=["Devices"],
)

@router.post("/", response_model=schemas.Device)
def create_device(device: schemas.DeviceCreate, db: Session = Depends(get_db)):
    db_device = models.Device(name=device.name, status=device.status or "off")
    db.add(db_device)
    db.commit()
    db.refresh(db_device)
    return db_device

@router.get("/", response_model=list[schemas.Device])
def get_devices(db: Session = Depends(get_db)):
    devices = db.query(models.Device).all()
    return devices

@router.put("/{device_id}", response_model=schemas.Device)
def update_device(device_id: int, new_status: str, db: Session = Depends(get_db)):
    db_device = db.query(models.Device).filter(models.Device.id == device_id).first()
    if not db_device:
        raise HTTPException(status_code=404, detail="Device not found")

    db_device.status = new_status
    db.commit()
    db.refresh(db_device)

    update_payload = json.dumps({
        "deviceId": db_device.id,
        "name": db_device.name,
        "newStatus": db_device.status
    })
    mqtt_client.publish(TOPIC_UNITY_UPDATES, update_payload)
    print(f"[+] Published to {TOPIC_UNITY_UPDATES} via API: {update_payload}")
    
    return db_device

@router.delete("/{device_id}")
def delete_device(device_id: int, db: Session = Depends(get_db)):
    db_device = db.query(models.Device).filter(models.Device.id == device_id).first()
    if not db_device:
        raise HTTPException(status_code=404, detail="Device not found")
    
    db.delete(db_device)
    db.commit()
    return {"message": f"Device {device_id} deleted successfully"}