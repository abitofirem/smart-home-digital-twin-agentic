# Konum: app/api/routers/alerts.py

from fastapi import APIRouter, Depends, HTTPException, Query
from typing import List, Dict, Any, Optional
from datetime import datetime

from firebase_admin import firestore 
from firebase_admin.firestore import client as FirestoreClient
from app.db import schemas
from app.db.database import get_db

router = APIRouter(
    prefix="/alerts",
    tags=["Alerts"],
)

def doc_to_alert(doc: Any) -> Dict[str, Any]:
    """Firestore dokümanını Alert şemasına dönüştürür."""
    data = doc.to_dict()
    timestamp = data.get("timestamp")
    
    return {
        "id": doc.id,
        "message": data.get("message"),
        "severity": data.get("severity"),
        "is_acknowledged": data.get("is_acknowledged", False),
        "device_id": data.get("device_id"),
        "timestamp": timestamp.isoformat() if timestamp else datetime.now().isoformat()
    }

@router.post("/", response_model=schemas.Alert)
def create_alert(alert: schemas.AlertCreate, db: FirestoreClient = Depends(get_db)):
    
    new_alert_data = {
        "message": alert.message,
        "severity": alert.severity,
        "device_id": alert.device_id,
        "is_acknowledged": False,
        "timestamp": firestore.SERVER_TIMESTAMP
    }
    
    doc_ref = db.collection("alerts").add(new_alert_data)[1]
    doc = doc_ref.get()
    return doc_to_alert(doc)

@router.get("/", response_model=List[schemas.Alert])
def get_alerts(
    db: FirestoreClient = Depends(get_db),
    is_acknowledged: Optional[bool] = Query(False, description="Onaylanmış (True) veya Onaylanmamış (False) uyarıları filtrele"),
    limit: int = Query(50, ge=1, le=500)
):
    """
    Uyarıları listeler.
    """
    query = db.collection("alerts").where("is_acknowledged", "==", is_acknowledged)
    
    alerts_ref = query.order_by("timestamp", direction=firestore.Query.DESCENDING).limit(limit).stream()
    
    alerts = [doc_to_alert(doc) for doc in alerts_ref]
    return alerts

@router.put("/{alert_id}/acknowledge", response_model=schemas.Alert)
def acknowledge_alert(alert_id: str, db: FirestoreClient = Depends(get_db)):
    
    alert_ref = db.collection("alerts").document(alert_id)
    doc = alert_ref.get()
    
    if not doc.exists:
        raise HTTPException(status_code=404, detail="Alert not found")
    
    alert_ref.update({
        "is_acknowledged": True
    })
    
    updated_doc = alert_ref.get()
    return doc_to_alert(updated_doc)