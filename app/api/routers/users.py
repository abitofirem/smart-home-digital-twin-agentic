# Konum: app/api/routers/users.py

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List

# Güvenlik için şifre hashing kütüphanesini import etmelisin
# Örneğin: from passlib.context import CryptContext
# pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

from app.db import models, schemas
from app.db.database import get_db

router = APIRouter(
    prefix="/users",
    tags=["Users"],
)

# -----------------------------------------------------
# 1. Yeni Kullanıcı Oluşturma (Kayıt)
# -----------------------------------------------------

@router.post("/", response_model=schemas.User)
def create_user(user: schemas.UserCreate, db: Session = Depends(get_db)):
    # Kullanıcı adının veya e-postanın zaten var olup olmadığını kontrol et
    db_user = db.query(models.User).filter(
        (models.User.username == user.username) | (models.User.email == user.email)
    ).first()
    
    if db_user:
        raise HTTPException(status_code=400, detail="Username or email already registered")

    # Şifreyi hash'le
    # hashed_password = pwd_context.hash(user.password) 
    
    # Şimdilik hash'lemeden (PoC amaçlı)
    db_user = models.User(
        username=user.username, 
        email=user.email, 
        hashed_password=user.password # DİKKAT: Gerçek uygulamada HASH'LENMELİDİR!
    )
    
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user

# -----------------------------------------------------
# 2. Kullanıcıları Listeleme (Yönetim Amaçlı)
# -----------------------------------------------------

@router.get("/", response_model=List[schemas.User])
def get_users(db: Session = Depends(get_db)):
    """
    Sistemdeki tüm kullanıcıları listeler. (Üretim ortamında kısıtlanmalıdır)
    """
    users = db.query(models.User).all()
    return users

# -----------------------------------------------------
# 3. Belirli Bir Kullanıcıyı Çekme
# -----------------------------------------------------

@router.get("/{user_id}", response_model=schemas.User)
def get_user(user_id: int, db: Session = Depends(get_db)):
    db_user = db.query(models.User).filter(models.User.id == user_id).first()
    if not db_user:
        raise HTTPException(status_code=404, detail="User not found")
    return db_user