package com.example.iothome.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt // Örnek ikon
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.iothome.data.model.Suggestion // Model importu
import com.example.iothome.ui.theme.ActiveStatusYellow // Vurgu rengi

@Composable
fun AISuggestionCard(
    suggestion: Suggestion,
    onApply: () -> Unit // "Uygula" butonuna basıldığında ViewModel'ı tetikler
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        // AI önerisi olduğu için dikkat çekici bir arka plan rengi
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Sol Taraf: İkon ve Metinler
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = suggestion.icon, // Modelden gelen ikon
                        contentDescription = "AI Önerisi",
                        // Örn: Enerji için Sarı, Konfor için Mavi
                        tint = ActiveStatusYellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = suggestion.title, // Örn: "Enerji Tasarrufu Uyarısı"
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = suggestion.description, // Örn: "Son 15 dakikadır hareket yok..."
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }

            Spacer(Modifier.width(16.dp))

            // Sağ Taraf: Aksiyon Butonu
            Button(
                onClick = onApply,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Uygula")
            }
        }
    }
}