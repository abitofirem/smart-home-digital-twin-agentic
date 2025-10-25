package com.example.iothome.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow // Yatay kaydırma için
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.iothome.data.model.Room
import com.example.iothome.data.model.DeviceType
import com.example.iothome.ui.theme.PrimaryPurple
import com.example.iothome.ui.theme.ActiveStatusYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomCard(
    room: Room,
    onClick: () -> Unit,
    // KRİTİK: Dışarıdan gelen toggle fonksiyonu
    onQuickControl: (deviceId: String) -> Unit
) {
    // Coil ile görsel yükleme
    val painter = rememberAsyncImagePainter(model = room.imageUrl)

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // 1. ODA RESMİ (Arka Plan)
            Image(
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)) // Loş etki
            )

            // 2. ÖN PLAN ALANI - YAZILAR VE İKONLAR
            // Burası, Oda Adını ve İkonları içeren alt şeffaf bandı yönetir.
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
            ) {

                // YARI ŞEFFAF SİYAH KATMAN (İkonlar hariç)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.6f)) // Yarı şeffaf koyu renk
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {

                    // Üst Satır: Oda Adı ve Açık Cihaz Sayısı
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = room.name,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White
                        )
                        Text(
                            text = "${room.devicesOn}/${room.totalDevices} açık",
                            style = MaterialTheme.typography.labelLarge,
                            color = PrimaryPurple
                        )
                    }
                }

                // ALT SATIR: HIZLI KONTROL İKONLARI (Yatay Kaydırılabilir Liste)
                // Bu liste, yazıların bulunduğu şeffaf bandın hemen altına gelir.
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.6f)) // Aynı şeffaf bandı devam ettirir
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp), // Alt padding
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(room.devices.filter { it.isActuator }) { device -> // Sadece kontrol edilebilir cihazları listele
                        QuickControlIcon(
                            type = device.type,
                            deviceId = device.id,
                            isOn = device.isOn,
                            onControl = onQuickControl
                        )
                    }
                }
            }
        }
    }
}

/**
 * Yeniden kullanılabilir Hızlı Kontrol İkonu Bileşeni
 * Not: Bu bileşeni RoomCard dışında bir dosyada tutmak daha temizdir, ancak şimdilik burada kalsın.
 */
@Composable
fun QuickControlIcon(
    type: DeviceType,
    deviceId: String,
    isOn: Boolean,
    onControl: (deviceId: String) -> Unit
) {
    // İkon rengini ayarla (Lamba için Sarı, diğerleri için Beyaz)
    val iconColor = when (type) {
        DeviceType.LIGHT -> if (isOn) ActiveStatusYellow else Color.White.copy(alpha = 0.9f)
        else -> Color.White.copy(alpha = 0.9f) // Diğer cihazlar (Fan, Termostat) için sabit renk
    }

    // İkonun kendisini al
    val iconVector: ImageVector = when (type) {
        DeviceType.LIGHT -> Icons.Default.Lightbulb
        DeviceType.THERMOSTAT -> Icons.Default.Thermostat
        DeviceType.FAN -> Icons.Default.AcUnit
        DeviceType.COFFEE_MACHINE -> Icons.Default.LocalCafe
        DeviceType.TV -> Icons.Default.Tv
        DeviceType.PURIFIER -> Icons.Default.Air
        else -> Icons.Default.Settings
    }

    IconButton(
        onClick = { onControl(deviceId) }, // MVVM'e iletiliyor!
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.5f)) // Arka plan
            .size(40.dp) // Tıklama alanı
    ) {
        Icon(
            imageVector = iconVector,
            contentDescription = "${type.name} Kontrol",
            tint = iconColor,
        )
    }
}