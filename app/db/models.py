# Konum: app/db/models.py
"""from sqlalchemy import Boolean, Column, Integer, String, DateTime, func, ForeignKey
from sqlalchemy.orm import relationship
from .database import Base

class User(Base):
    __tablename__ = "users"
    id = Column(Integer, primary_key=True, index=True)
    username = Column(String, unique=True, index=True, nullable=False)
    email = Column(String, unique=True, index=True, nullable=False)
    hashed_password = Column(String, nullable=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    is_active = Column(Boolean, default=True)
    
    user_logs = relationship("UserLog", back_populates="user") 


class Device(Base):
    __tablename__ = "devices"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, index=True)
    status = Column(String, default="off")
    last_updated = Column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now())
    
    # İlişkiler: SADECE YENİLERİN KALDIĞINDAN EMİN OLUN
    
    # Eski: logs = relationship("Log", back_populates="device") # BU SATIR SİLİNMELİYDİ
    
    alerts = relationship("Alert", back_populates="device") # Alert modeli hala var
    device_logs = relationship("DeviceLog", back_populates="device") # Yeni Log
    sensor_logs = relationship("SensorLog", back_populates="device") # Yeni Log
class UserLog(Base):
    __tablename__ = "users_log"
    id = Column(Integer, primary_key=True, index=True)
    # Hangi kullanıcının aksiyonu
    user_id = Column(Integer, ForeignKey("users.id")) 
    action_type = Column(String, nullable=False) # Örn: LOGIN, LOGOUT, PROFILE_UPDATE
    message = Column(String, nullable=False) # Örn: 'User logged in successfully'
    timestamp = Column(DateTime(timezone=True), server_default=func.now())
    
    user = relationship("User", back_populates="user_logs") # User modelinde de ilişki tanımlanmalı
    
class DeviceLog(Base):
    __tablename__ = "devices_log"
    id = Column(Integer, primary_key=True, index=True)
    device_id = Column(Integer, ForeignKey("devices.id"))
    command_source = Column(String, default="ANDROID") # Komutun kaynağı: ANDROID, AI, API
    old_status = Column(String, nullable=False)
    new_status = Column(String, nullable=False)
    timestamp = Column(DateTime(timezone=True), server_default=func.now())

    device = relationship("Device", back_populates="device_logs") # Device modelinde de ilişki tanımlanmalı

class SensorLog(Base):
    __tablename__ = "sensors_log"
    id = Column(Integer, primary_key=True, index=True)
    device_id = Column(Integer, ForeignKey("devices.id"))
    sensor_type = Column(String, nullable=False) # Örn: TEMPERATURE, MOTION, SOUND
    # Float (ondalıklı) veya String (metinsel) değer tutabilir. Float daha iyidir.
    value = Column(String, nullable=True) 
    raw_value = Column(String, nullable=True) # "detected" gibi string değerler için
    timestamp = Column(DateTime(timezone=True), server_default=func.now())
    
    device = relationship("Device", back_populates="sensor_logs") # Device modelinde de ilişki tanımlanmalı


class Alert(Base):
    __tablename__ = "alerts"
    id = Column(Integer, primary_key=True, index=True)
    message = Column(String, nullable=False)
    severity = Column(String, default="LOW")
    is_acknowledged = Column(Boolean, default=False)
    timestamp = Column(DateTime(timezone=True), server_default=func.now())
    device_id = Column(Integer, ForeignKey("devices.id"))

    device = relationship("Device", back_populates="alerts")"""
# Bu dosya artık kullanılmıyor çünkü SQLAlchemy yerine Firebase Firestore kullanıyoruz.