package com.example.iothome.data.model

// Room modeli, odalar ekranındaki kartları besler
data class Room(
    val id: String,
    val name: String,
    val totalDevices: Int,
    val devicesOn: Int,
    // Odadaki cihazların listesi (ileride detay ekranı için kullanılacak)
    val devices: List<Device>,
    // Oda fotoğrafının yolu (Görseldeki arka plan için)
    val imageUrl: String? = null,


    val isFavorite: Boolean = false // YENİ ALAN: Favori durumu
)