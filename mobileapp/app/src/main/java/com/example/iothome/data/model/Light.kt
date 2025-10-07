package com.example.iothome.data.model

data class Light(
    val id: String = "light_1",
    val name: String = "Akıllı Lamba",
    val isOn: Boolean = false // Lamba Açık/Kapalı durumu
)