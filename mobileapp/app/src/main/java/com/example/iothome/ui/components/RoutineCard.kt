package com.example.iothome.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.iothome.data.model.Routine // Model importu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineCard(
    routine: Routine,
    onClick: () -> Unit // Kart tıklandığında ViewModel'ı tetikler
) {
    // Tıklama hatasını (clickable crash) önlemek için
    val interactionSource = remember { MutableInteractionSource() }
    val indication = rememberRipple()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            // DÜZELTME: Card'ın kendi onClick'i yerine modifier.clickable kullanıyoruz
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                onClick = onClick,
                interactionSource = interactionSource,
                indication = indication
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), // Normal kart rengi
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sol Taraf: İkon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    // İkon arka planı (Temaya uygun)
                    .background(MaterialTheme.colorScheme.surface), // Veya SurfaceDark (daha koyu)
                    contentAlignment = Alignment.Center//                contentAlignment = Alignment.Center
            ) {
                Icon(
                    routine.icon, // Modelden gelen ikon (Örn: Movie)
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary, // Vurgu rengi
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            // Sağ Taraf: Metinler
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = routine.name, // Örn: "Film Gecesi"
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = routine.description, // Örn: "Işıklar kapalı, TV açık."
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                // ÖNERİ: Tetikleyici bilgisini ekliyoruz
                Text(
                    text = "Tetikleyici: ${routine.triggerInfo}", // Örn: "Manuel"
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}