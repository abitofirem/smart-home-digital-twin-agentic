from fastapi import FastAPI, Depends
from pydantic import BaseModel
from sqlalchemy import create_engine, Column, Integer, String, DateTime, func
from sqlalchemy.orm import sessionmaker, declarative_base, Session
from dotenv import load_dotenv
import paho.mqtt.client as mqtt
import os

# --- ENV & FastAPI ---
load_dotenv()
app = FastAPI()

# --- DB CONFIG (Supabase PostgreSQL) ---
SQLALCHEMY_DATABASE_URL = os.getenv(
    "DATABASE_URL",
    "postgresql+psycopg2://postgres.jxjwcckjjmpuecwtrgyv:doTqgQ46MFIVF0@aws-1-us-east-2.pooler.supabase.com:6543/postgres" 
    )

engine = create_engine(SQLALCHEMY_DATABASE_URL)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()


# --- MODELLER ---
class Device(Base):
    __tablename__ = "devices"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, index=True)
    status = Column(String, default="off")
    last_updated = Column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now())

Base.metadata.create_all(bind=engine)


# --- Pydantic Şeması ---
class DeviceCreate(BaseModel):
    name: str
    status: str | None = "off"

    class Config:
        from_attributes = True


# --- DB Oturumu ---
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


# --- MQTT Ayarları ---
MQTT_BROKER = "mosquitto_broker"   # docker-compose içindeki service adı
MQTT_PORT = 1883
MQTT_TOPIC = "bmt400/devices"

mqtt_client = mqtt.Client()

def on_connect(client, userdata, flags, rc, properties=None):
    print("[+] MQTT connected:", rc)
    client.subscribe(MQTT_TOPIC + "/#")


def on_message(client, userdata, msg):
    print(f"[+] MQTT Received: {msg.topic} -> {msg.payload.decode()}")
    payload = msg.payload.decode().strip()
    parts = payload.split(":")

    if len(parts) != 2:
        print("[!] Invalid payload format. Expected 'id:status'")
        return

    try:
        device_id, status = parts
        device_id = int(device_id)

        db = SessionLocal()
        db_device = db.query(Device).filter(Device.id == device_id).first()

        if db_device:
            print(f"Found device {device_id}, updating status -> {status}")
            db_device.status = status
            db.commit()
            db.refresh(db_device)
            print("[+] DB updated successfully")
        else:
            print(f"[!] Device {device_id} not found in DB")

        db.close()
    except Exception as e:
        print("[!] MQTT DB update error:", e)


mqtt_client.on_connect = on_connect
mqtt_client.on_message = on_message
mqtt_client.connect(MQTT_BROKER, MQTT_PORT, 60)
mqtt_client.loop_start()


# --- API ENDPOINTLERİ ---
@app.post("/devices/", response_model=DeviceCreate)
def create_device(device: DeviceCreate, db: Session = Depends(get_db)):
    db_device = Device(name=device.name, status=device.status or "off")
    db.add(db_device)
    db.commit()
    db.refresh(db_device)
    return db_device


@app.get("/devices/")
def get_devices(db: Session = Depends(get_db)):
    return db.query(Device).all()


@app.put("/devices/{device_id}")
def update_device(device_id: int, new_status: str, db: Session = Depends(get_db)):
    db_device = db.query(Device).filter(Device.id == device_id).first()
    if not db_device:
        return {"error": "Device not found"}

    db_device.status = new_status
    db.commit()
    db.refresh(db_device)

    # MQTT Publish
    mqtt_message = f"{db_device.id}:{db_device.status}"
    mqtt_client.publish(f"{MQTT_TOPIC}/{db_device.id}", mqtt_message)
    print(f"[+] MQTT Publish -> {mqtt_message}")

    return db_device

@app.delete("/devices/{device_id}")
def delete_device(device_id: int, db: Session = Depends(get_db)):
    db_device = db.query(Device).filter(Device.id == device_id).first()
    if not db_device:
        raise HTTPException(status_code=404, detail="Device not found")
    
    db.delete(db_device)
    db.commit()
    return {"message": f"Device {device_id} deleted successfully"}
