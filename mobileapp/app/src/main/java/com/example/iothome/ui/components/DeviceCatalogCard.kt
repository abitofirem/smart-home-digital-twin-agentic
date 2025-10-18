package com.example.iothome.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
// !!! GEREKLİ MODEL VE İKON IMPORTLARI BURAYA EKLENMELİ !!!
import com.example.iothome.ui.theme.SurfaceDark
import androidx.compose.material.icons.filled.Lightbulb // Zaten var
import androidx.compose.material.icons.filled.* // <-- TÜM DİĞER İKONLAR İÇİN BU GEREKLİ!
import androidx.compose.ui.tooling.preview.Preview
// Kendi modelinizi import edin:
import com.example.iothome.data.model.DeviceType
import com.example.iothome.ui.theme.MobileappTheme
// Metin dönüşümü için:
import java.util.Locale




// Yukarıdaki satır (.*) Thermostat, LocalCafe, Tv, Air, vb. ikon hatalarını çözecektir.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceCatalogCard(
    deviceType: DeviceType,
    deviceCount: Int,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().aspectRatio(1f), // Kare görünüm
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface) // Koyu Kart Arka Planı
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Üst Kısım: İkon
            Icon(
                imageVector = getDeviceIcon(deviceType),
                contentDescription = deviceType.name,
                tint = MaterialTheme.colorScheme.onSurface, // Koyu Kart Üzerindeki Metin/İkon Rengi
                modifier = Modifier.size(36.dp)
            )

            // Alt Kısım: Başlık ve Sayı
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = deviceType.name.replace("_", " ") // Örn: "GARAGE DOOR" -> "GARAGE DOOR"
                        .lowercase(Locale.getDefault())       // -> "garage door"
                        .replaceFirstChar { it.titlecase(Locale.getDefault()) }, // -> "Garage door"
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "x$deviceCount Cihaz",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// Cihaz Tipine Göre İkon Döndüren Yardımcı Fonksiyon (İleride genişletilecek)
// Cihaz Tipine Göre İkon Döndüren Yardımcı Fonksiyon
fun getDeviceIcon(type: DeviceType): ImageVector {
    return when (type) {
        DeviceType.LIGHT -> Icons.Default.Lightbulb
        DeviceType.FAN -> Icons.Default.AcUnit
        DeviceType.THERMOSTAT -> Icons.Default.Thermostat
        DeviceType.COFFEE_MACHINE -> Icons.Default.LocalCafe
        DeviceType.TV -> Icons.Default.Tv
        DeviceType.PURIFIER -> Icons.Default.Air
        DeviceType.WASHING_MACHINE -> Icons.Default.LocalLaundryService
        DeviceType.GARAGE_DOOR -> Icons.Default.Garage
        DeviceType.SENSOR -> Icons.Default.Sensors
        // Burası tüm DeviceType'ları kapsadığı için artık hata vermeyecektir.
    }
}

