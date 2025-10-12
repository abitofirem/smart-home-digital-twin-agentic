# Konum: app/api/routers/logs.py

from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session
from datetime import datetime, timezone
from typing import Optional, List

from app.db import models, schemas
from app.db.database import get_db

router = APIRouter(
    prefix="/logs",
    tags=["Logs"],
)

# Pydantic Şemasına Log için ihtiyacımız var, schemas.py'ye ekleyelim.
# Geçici olarak burada tanımlayabiliriz, ancak schemas.py'ye taşımak daha doğru olacaktır.

# --- Log Şemaları (schemas.py'ye eklenecek varsayımıyla) ---
class LogBase(schemas.BaseModel):
    message: str
    level: str
    timestamp: datetime
    device_id: Optional[int]

class Log(LogBase):
    id: int
    class Config:
        from_attributes = True
# ---

@router.get("/", response_model=List[Log])
def get_logs(
    db: Session = Depends(get_db),
    # AI Analizi için filtreleme parametreleri
    start_time: Optional[datetime] = Query(None, description="Başlangıç zamanı (ISO formatı)"),
    end_time: Optional[datetime] = Query(None, description="Bitiş zamanı (ISO formatı)"),
    device_id: Optional[int] = Query(None, description="Filtrelenecek cihaz ID'si"),
    limit: int = Query(100, ge=1, le=1000, description="Döndürülecek maksimum kayıt sayısı")
):
    query = db.query(models.Log)

    if device_id is not None:
        query = query.filter(models.Log.device_id == device_id)
    
    if start_time:
        # Zaman dilimi farkını yönetmek için timezone-aware sorgu
        query = query.filter(models.Log.timestamp >= start_time.replace(tzinfo=timezone.utc))
    
    if end_time:
        query = query.filter(models.Log.timestamp <= end_time.replace(tzinfo=timezone.utc))

    # En yeni loglar en başta olacak şekilde sırala ve limiti uygula
    logs = query.order_by(models.Log.timestamp.desc()).limit(limit).all()
    
    return logs