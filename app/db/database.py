import firebase_admin
from firebase_admin import credentials, firestore
import os
from typing import Generator
from dotenv import load_dotenv


# Ortam değişkeninden kimlik bilgisi yolunu oku
# Bu dosyanın (serviceAccountKey.json) .env dosyasında belirtilen yolda olması gerekir.

BASE_DIR = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

# .env dosyasının TAM YOLU
DOTENV_PATH = os.path.join(BASE_DIR, '.env')

# !!! .env yükleniyor !!!
load_dotenv(DOTENV_PATH) 

# KREDİTASYON DOSYASININ YOLUNU DÜZELT:
CRED_PATH = os.getenv("FIREBASE_CREDENTIALS_PATH")

if CRED_PATH:
    # Firebase JSON dosyasının da BMT400/ Kök Klasöründe olduğunu varsayarak 
    # mutlak yolunu oluşturuyoruz.
    FIREBASE_CREDENTIALS_PATH = os.path.join(BASE_DIR, CRED_PATH)
    
    # Hata ayıklama çıktısı için:
    print(f"[*] BASE_DIR: {BASE_DIR}")
    print(f"[*] JSON Dosya Yolu: {FIREBASE_CREDENTIALS_PATH}")
    
    # Şimdi initialize_firebase fonksiyonundaki CRED_PATH'i güncelleyelim.
    # initialize_firebase fonksiyonu içindeki "CRED_PATH = os.getenv("FIREBASE_CREDENTIALS_PATH")" satırını silip yerine 
    # FIREBASE_CREDENTIALS_PATH (mutlak yol) kullanmalıyız.
else:
    FIREBASE_CREDENTIALS_PATH = None
    print(f"[*] BASE_DIR: {BASE_DIR}")
    print(f"[*] JSON Dosya Yolu: None")

# Firestore istemcisi için global değişken
db_firestore = None


def initialize_firebase():
    """Firebase Admin SDK'yı başlatır ve Firestore istemcisini hazırlar."""
    global db_firestore
    
    # Sadece bir kere başlatıldığından emin ol
    if db_firestore is None and CRED_PATH and os.path.exists(CRED_PATH):
        try:
            # 1. Kimlik Bilgilerini Yükle
            cred = credentials.Certificate(CRED_PATH)
            
            # 2. Firebase Uygulamasını Başlat
            if not firebase_admin._apps:
                firebase_admin.initialize_app(cred, name='AkıllıEvAPI')

            print("[+] Firebase Admin SDK başarıyla başlatıldı.")
            
            # 3. Firestore İstemcisini Al
            db_firestore = firestore.client(app=firebase_admin.get_app('AkıllıEvAPI'))
            
        except Exception as e:
            print(f"[!] Firebase Başlatma Hatası: {e}")
            db_firestore = None
    elif db_firestore is None:
        print("[!] Hata: FIREBASE_CREDENTIALS_PATH ortam değişkeni tanımlı değil veya dosya bulunamadı.")


# Uygulama başlangıcında Firebase'i başlat (Dosya yüklendiğinde çalışır)
initialize_firebase()

# FastAPI Bağımlılığı (Dependency)
def get_db() -> Generator[firestore.client, None, None]:
    """FastAPI endpoint'leri için Firebase Firestore istemcisini sağlar."""
    if db_firestore is None:
        print("[!] Kritik Hata: get_db çağrıldı ancak Firebase bağlantısı kurulamadı.")
        raise Exception("Veritabanı bağlantısı kurulamadı.")
    
    # NoSQL (Firestore) istemcisi yield edilir.
    try:
        yield db_firestore
    finally:
        # Firestore istemcisi için kapatma (close) işlemi gerekmez.
        pass