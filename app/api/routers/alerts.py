# Konum: app/api/routers/alerts.py

from datetime import datetime
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from typing import List, Optional

from app.db import models, schemas
from app.db.database import get_db
# Alert şemalarını kullanmak için schemas.py'ye ihtiyacımız var

router = APIRouter(
    prefix="/alerts",
    tags=["Alerts"],
)

class AlertBase(schemas.BaseModel):
    message: str
    severity: str
    device_id: Optional[int] = None

class Alert(AlertBase):
    id: int
    timestamp: datetime
    is_acknowledged: bool
    class Config:
        from_attributes = True

# -----------------------------------------------------
# 1. Yeni Alert Oluşturma (Unity veya AI tarafından kullanılacak)
# -----------------------------------------------------

@router.post("/", response_model=Alert)
def create_alert(alert: schemas.AlertCreate, db: Session = Depends(get_db)):
    """
    Simülasyon veya Yapay Zeka tarafından kritik bir durum algılandığında
    yeni bir uyarı kaydı oluşturur.
    """
    db_alert = models.Alert(
        message=alert.message,
        severity=alert.severity,
        device_id=alert.device_id
    )
    db.add(db_alert)
    db.commit()
    db.refresh(db_alert)
    return db_alert

# -----------------------------------------------------
# 2. Alert'leri Sorgulama (Mobil Uygulama tarafından kullanılacak)
# -----------------------------------------------------

@router.get("/", response_model=List[Alert])
def get_alerts(
    db: Session = Depends(get_db),
    is_acknowledged: Optional[bool] = Query(False, description="Onaylanmış (True) veya Onaylanmamış (False) uyarıları filtrele"),
    limit: int = Query(50, ge=1, le=500)
):
    """
    Uyarıları listeler. Özellikle onaylanmamış (is_acknowledged=False) uyarılar için kullanılır.
    """
    query = db.query(models.Alert).filter(models.Alert.is_acknowledged == is_acknowledged)
    alerts = query.order_by(models.Alert.timestamp.desc()).limit(limit).all()
    return alerts

# -----------------------------------------------------
# 3. Alert'i Onaylama (Mobil Uygulama tarafından kullanılacak)
# -----------------------------------------------------

@router.put("/{alert_id}/acknowledge", response_model=Alert)
def acknowledge_alert(alert_id: int, db: Session = Depends(get_db)):
    """
    Kullanıcı tarafından görülen bir uyarıyı onaylanmış (is_acknowledged=True) olarak işaretler.
    """
    db_alert = db.query(models.Alert).filter(models.Alert.id == alert_id).first()
    if not db_alert:
        raise HTTPException(status_code=404, detail="Alert not found")
    
    db_alert.is_acknowledged = True
    db.commit()
    db.refresh(db_alert)
    return db_alert