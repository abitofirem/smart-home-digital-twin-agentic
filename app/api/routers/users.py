# Konum: app/api/routers/users.py

from fastapi import APIRouter, Depends, HTTPException
from typing import List, Dict, Any
from datetime import datetime

from firebase_admin import firestore 
from firebase_admin.firestore import client as FirestoreClient
from app.db import schemas
from app.db.database import get_db

router = APIRouter(
    prefix="/users",
    tags=["Users"],
)

def doc_to_user(doc: Any) -> Dict[str, Any]:
    """Firestore dokümanını User şemasına dönüştürür."""
    data = doc.to_dict()
    timestamp = data.get("created_at")
    
    return {
        "id": doc.id, 
        "username": data.get("username"),
        "email": data.get("email"),
        "is_active": data.get("is_active", True),
        "created_at": timestamp.isoformat() if timestamp else datetime.now().isoformat()
    }

@router.post("/", response_model=schemas.User)
def create_user(user: schemas.UserCreate, db: FirestoreClient = Depends(get_db)):
    
    # Firebase'de eşsizlik kontrolü (username/email)
    existing_user_ref = db.collection("users").where("username", "==", user.username).limit(1).stream()
    if list(existing_user_ref):
        raise HTTPException(status_code=400, detail="Username already registered")
        
    existing_email_ref = db.collection("users").where("email", "==", user.email).limit(1).stream()
    if list(existing_email_ref):
        raise HTTPException(status_code=400, detail="Email already registered")

    new_user_data = {
        "username": user.username, 
        "email": user.email, 
        "hashed_password": user.password, # DİKKAT: Üretimde mutlaka HASH'lenmeli!
        "created_at": firestore.SERVER_TIMESTAMP,
        "is_active": True
    }
    
    doc_ref = db.collection("users").add(new_user_data)[1] 
    doc = doc_ref.get()
    return doc_to_user(doc)

@router.get("/", response_model=List[schemas.User])
def get_users(db: FirestoreClient = Depends(get_db)):
    users_ref = db.collection("users").stream()
    users = [doc_to_user(doc) for doc in users_ref]
    return users

@router.get("/{user_id}", response_model=schemas.User)
def get_user(user_id: str, db: FirestoreClient = Depends(get_db)):
    user_ref = db.collection("users").document(user_id).get()
    if not user_ref.exists:
        raise HTTPException(status_code=404, detail="User not found")
    return doc_to_user(user_ref)