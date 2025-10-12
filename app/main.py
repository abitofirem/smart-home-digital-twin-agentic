# Konum: app/main.py

from fastapi import FastAPI
from app.db.database import engine
from app.db.models import Base
from app.api.routers import devices, logs, alerts, users
from app.mqtt.client import start_mqtt_client

# Veritabanı tablolarını oluştur (eğer yoksa)
Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="Akıllı Ev API",
    description="Android, Unity ve Backend arasındaki iletişimi sağlayan merkezi API.",
    version="1.0.0",
)

@app.on_event("startup")
def on_startup():
    """Uygulama başladığında MQTT istemcisini başlat."""
    start_mqtt_client()

# Router'ları ana uygulamaya dahil et
app.include_router(devices.router)
app.include_router(logs.router)
app.include_router(alerts.router)
app.include_router(users.router)

@app.get("/", tags=["Root"])
def read_root():
    return {"message": "Akıllı Ev API'sine hoş geldiniz!"}