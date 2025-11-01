package com.example.iothome.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.iothome.ui.theme.GraphCold // Soğuk Renk (Mavi)
import com.example.iothome.ui.theme.GraphHot  // Sıcak Renk (Kırmızı)
import com.example.iothome.ui.theme.MobileappTheme
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CircularGauge(
    modifier: Modifier = Modifier,
    value: Double, // Gösterilecek değer (örn: 25.0)
    minValue: Double = 0.0, // Minimum değer (örn: 0)
    maxValue: Double = 100.0, // Maksimum değer (örn: 100)
    primaryColor: Color = GraphCold, // Ana renk (Mavi kısım)
    secondaryColor: Color = GraphHot, // İkincil renk (Kırmızı kısım)
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), // Arka plan halkası
    strokeWidth: Float = 30f,
    title: String = "Sıcaklık", // Ortadaki büyük metin başlığı
    subtitle: String = "Nem:" // Ortadaki küçük metin
) {
    // Değeri 0 ile 360 derece arasına ölçeklendir
    val sweepAngle = ((value - minValue) / (maxValue - minValue) * 360f).toFloat().coerceIn(0f, 360f)
    val startAngle = -90f // Halkanın başlangıç açısı (Üst Orta)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(200.dp) // Göstergenin boyutu
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val arcSize = size.width - strokeWidth // Halka çapı
            val arcRadius = arcSize / 2f

            // 1. Arka Plan Halkası (Tam Daire)
            drawArc(
                color = backgroundColor,
                startAngle = startAngle,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 2. Birincil Renk Halkası (Mavi Kısım)
            drawArc(
                color = primaryColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle, // Değere göre ilerleme
                useCenter = false,
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round) // Yuvarlak uçlar
            )

            // 3. İkincil Renk Halkası (Kırmızı Kısım - Mavi'nin bittiği yerden başlar)
            // Görseldeki gibi yarım daire efekti için basit bir mantık:
            // Eğer değer %50'yi geçerse, kırmızı başlar.
            val secondaryStartAngle = startAngle + sweepAngle
            val secondarySweepAngle = 360f - sweepAngle // Kalan kısım

            // DÜZELTME 2: Değeri (value) KIRMIZI ile çiziyoruz
            // Not: İki rengin üst üste binmemesi için bu bloğu Sona aldık (Z-sırası)
            drawArc(
                color = secondaryColor, // KIRMIZI (GraphHot)
                startAngle = startAngle,
                sweepAngle = sweepAngle, // Değere göre ilerleme
                useCenter = false,
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }


        // Ortadaki Metinler
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                text = "${value.toInt()}°C", // Değeri göster
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 48.sp // Büyük font boyutu
            )
            Text(
                text = "$subtitle",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}



@Preview(showBackground = true)
@Composable
fun CircularGaugePreview() {
    // Projenizin koyu temasını çağırıyoruz
    MobileappTheme(darkTheme = true) {

        // CircularGauge'ı örnek bir değerle görüntülüyoruz
        CircularGauge(
            value = 25.0, // Görseldeki gibi 25°C
            title = "Sıcaklık",
            subtitle = "Nem:"
        )
    }
}

// Farklı bir değerle ikinci bir önizleme (opsiyonel)
@Preview(showBackground = true)
@Composable
fun CircularGaugePreviewHighValue() {
    MobileappTheme(darkTheme = true) {
        CircularGauge(
            value = 75.0,
            minValue = 0.0,
            maxValue = 100.0,
            title = "Nem",
            subtitle = "Yüzde:"
        )
    }
}