# Konum: app/api/routers/logs.py

from fastapi import APIRouter, Depends, Query
from typing import List, Dict, Any, Optional
from datetime import datetime

from firebase_admin import firestore 
from firebase_admin.firestore import client as FirestoreClient
from app.db import schemas
from app.db.database import get_db

router = APIRouter(
    prefix="/logs",
    tags=["Logs"],
)

def doc_to_log(doc: Any) -> Dict[str, Any]:
    """Firestore dokümanını Log şemasına dönüştürür."""
    data = doc.to_dict()
    timestamp = data.get("timestamp")
    
    return {
        "id": doc.id,
        "device_id": data.get("device_id"),
        "sensor_type": data.get("sensor_type"),
        "value": data.get("value"),
        "raw_value": data.get("raw_value"),
        "timestamp": timestamp.isoformat() if timestamp else datetime.now().isoformat()
    }

# Not: Bu router'ın, DeviceLog ve SensorLog verilerini çekmek için farklı endpoint'lere ayrılması daha temiz olur.
# Şimdilik SensorLog verisini çeken ana endpoint'i tanımlayalım.

@router.get("/sensors", response_model=List[schemas.SensorLog])
def get_sensor_logs(
    db: FirestoreClient = Depends(get_db),
    device_id: Optional[str] = Query(None, description="Filtrelenecek cihaz ID'si"),
    limit: int = Query(100, ge=1, le=1000, description="Döndürülecek maksimum kayıt sayısı")
):
    """
    Yapay Zeka modellerinin veri çekmesi için sensör kayıtlarını listeler.
    """
    query = db.collection("sensors_log")

    if device_id is not None:
        query = query.where("device_id", "==", device_id)

    # Firebase'de orderBy() ve limit() kullanılır
    logs_ref = query.order_by("timestamp", direction=firestore.Query.DESCENDING).limit(limit).stream()
    
    logs = [doc_to_log(doc) for doc in logs_ref]
    
    return logs