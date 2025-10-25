package com.example.iothome.data.model

// NOT: Light modeli, MVVM'deki daha genel Device modeline entegre edilmelidir.
// Ancak bu aşamada sadece Light ile çalıştığınız için mevcut haliyle devam edelim.

data class Light(
    // Alan adlarını gelen/giden JSON ile uyumlu hale getirdik.
    val deviceId: String, // Yeni alan adı: deviceId (String)
    val name: String = "Akıllı Lamba",
    val status: Boolean = false // isOn yerine status kullanıyorsunuz.
)