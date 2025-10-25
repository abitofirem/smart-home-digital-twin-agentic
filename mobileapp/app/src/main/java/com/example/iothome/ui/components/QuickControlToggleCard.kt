package com.example.iothome.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.iothome.data.model.Device
import com.example.iothome.data.model.DeviceType
import com.example.iothome.ui.theme.ActiveStatusYellow

@Composable
fun QuickControlToggleCard(
    device: Device,
    onToggle: (String) -> Unit
) {
    val cardBackgroundColor = if (device.isOn) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val iconColor = when (device.type) {
        DeviceType.LIGHT -> if (device.isOn) ActiveStatusYellow else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        else -> if (device.isOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    }

    Card(
        modifier = Modifier
            // DÜZELTME: Genişlik artırıldı, yükseklik kaldırıldı
            .width(140.dp), // Daha klasik ve okunabilir bir genişlik
        // .height(130.dp), // Sabit yükseklik kaldırıldı
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
    ) {
        Column(
            modifier = Modifier
                // .fillMaxSize() // Artık fillMaxSize değil, padding kullanıyoruz
                .padding(12.dp),
            // DÜZELTME: Arrangement.SpaceBetween yerine aralıklı (spacedBy)
            verticalArrangement = Arrangement.spacedBy(16.dp) // Öğeler arasına boşluk koyar
        ) {
            // Üst Kısım: İkon ve Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = getSmallDeviceIcon(device.type),
                    contentDescription = device.name,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
                if (device.isActuator) {
                    Switch(
                        checked = device.isOn,
                        onCheckedChange = { onToggle(device.id) },
                        // KRİTİK DÜZELTME: Switch'i küçültmek için .size() yerine .scale() kullanın
                        modifier = Modifier
                            .scale(0.7f) // Switch'i %70 oranında küçültür
                            // Ölçeklendirme sonrası konumu (offset) yeniden ayarlamanız gerekebilir
                            .offset(x = 8.dp, y = (-8).dp),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                        )
                    )
                }
            }

            // Alt Kısım: Cihaz Adı ve Oda Adı
            // (verticalArrangement.spacedBy sayesinde bu kısım otomatik olarak aşağı itilir)
            Column {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Oda: ${device.roomId}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// Hızlı kontrol kartları için daha küçük ikon seti (Gerekirse genişletilebilir)
fun getSmallDeviceIcon(type: DeviceType): ImageVector {
    return when (type) {
        DeviceType.LIGHT -> Icons.Default.Lightbulb
        DeviceType.THERMOSTAT -> Icons.Default.Thermostat
        DeviceType.FAN -> Icons.Default.AcUnit
        else -> Icons.Default.Settings // Diğer tipler için varsayılan
    }
}