# Konum: app/main.py

from fastapi import FastAPI
# Eski SQLAlchemy (engine, Base) importları KALDIRILDI.

# Tüm Firebase uyumlu router'ları import et
from app.api.routers import devices, users, logs, alerts 
from app.mqtt.client import start_mqtt_client
from dotenv import load_dotenv
load_dotenv()

# SQLALchemy Veritabanı tablolarını oluşturma satırı (Base.metadata.create_all) KALDIRILDI.

app = FastAPI(
    title="Akıllı Ev Dijital İkiz API",
    description="Firebase Firestore tabanlı, Android, Unity ve Backend arasındaki iletişimi sağlayan merkezi API.",
    version="2.0.0", # Versiyonu yükseltelim!
)

@app.on_event("startup")
def on_startup():
    """Uygulama başladığında Firebase bağlantısını kontrol eder ve MQTT istemcisini başlatır."""
    # Firebase bağlantısı (database.py'de initialize_firebase() ile) bu aşamada otomatik olarak yapılır.
    start_mqtt_client()
    print("[*] Akıllı Ev API başlatıldı. Tüm servisler Firebase'e bağlı.")

# Tüm router'ları ana uygulamaya dahil et
app.include_router(devices.router)
app.include_router(users.router)
app.include_router(logs.router)
app.include_router(alerts.router)


@app.get("/", tags=["Root"])
def read_root():
    return {"message": "Akıllı Ev API'sine hoş geldiniz! Tüm sistem Firebase Firestore'a taşındı."}