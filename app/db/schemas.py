# Konum: app/db/schemas.py

from pydantic import BaseModel
from datetime import datetime
from typing import Optional

# --- Device Şemaları ---
class DeviceBase(BaseModel):
    name: str
    status: Optional[str] = "off"

class DeviceCreate(DeviceBase):
    pass

class Device(DeviceBase):
    id: int
    last_updated: datetime
    class Config:
        from_attributes = True

# --- User Şemaları ---
class UserBase(BaseModel):
    username: str
    email: str

class UserCreate(UserBase):
    password: str

class User(UserBase):
    id: int
    is_active: bool
    created_at: datetime
    class Config:
        from_attributes = True


# --- Log Şemaları ---
class LogBase(BaseModel):
    message: str
    level: str
    device_id: Optional[int] = None

class LogCreate(LogBase):
    pass

class Log(LogBase):
    id: int
    timestamp: datetime
    class Config:
        from_attributes = True

# --- Alert Şemaları ---
class AlertBase(BaseModel):
    message: str
    severity: str
    device_id: Optional[int] = None # Hangi cihazla ilgili olduğu

class AlertCreate(AlertBase):
    pass

class AlertUpdate(BaseModel):
    # Örneğin, mobil uygulama tarafından onaylanıp onaylanmadığını güncellemek için
    is_acknowledged: Optional[bool] = None

class Alert(AlertBase):
    id: int
    is_acknowledged: bool
    timestamp: datetime
    class Config:
        from_attributes = True

        