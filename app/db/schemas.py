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
    id: str
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
class UserLogBase(BaseModel):
    user_id: Optional[int]
    action_type: str
    message: str

class UserLog(UserLogBase):
    id: int
    timestamp: datetime
    class Config:
        from_attributes = True

# --- DeviceLog Şemaları ---
class DeviceLogBase(BaseModel):
    device_id: int
    command_source: str
    old_status: str
    new_status: str

class DeviceLog(DeviceLogBase):
    id: int
    timestamp: datetime
    class Config:
        from_attributes = True

# --- SensorLog Şemaları (AI için kritik) ---
class SensorLogBase(BaseModel):
    device_id: int
    sensor_type: str
    value: Optional[float] = None # Float değer
    raw_value: Optional[str] = None # String değer (örn. "detected")

class SensorLog(SensorLogBase):
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

