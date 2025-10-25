package com.example.iothome.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.iothome.ui.components.CircularGauge
import com.example.iothome.ui.components.QuickControlToggleCard
// HomeUiState ve WeatherState importları
import com.example.iothome.data.model.HomeUiState
import com.example.iothome.data.model.WeatherState

@OptIn(ExperimentalMaterial3Api::class) // Bu hala gerekli olabilir
@Composable
// Scaffold ve TopAppBar kaldırıldı. Sadece içeriği döndürüyoruz.
// PaddingValues parametresi ana Scaffold'dan gelecek.
fun HomeScreen( // Fonksiyon adı aynı kalabilir, sadece içeriği değişti
    bottomNavController: NavHostController, // Bottom Nav için (İleride gerekebilir)
    viewModel: HomeViewModel = viewModel(),
    paddingValues: PaddingValues // ANA SCAFFOLD'DAN GELEN PADDING
) {
    // ViewModel'dan gelen GENEL DURUMU izle
    val uiState by viewModel.uiState.collectAsState()

    // Scaffold KALDIRILDI. Doğrudan LazyColumn ile başlıyoruz.

    LazyColumn(
        modifier = Modifier
            .padding(paddingValues) // ANA SCAFFOLD'DAN GELEN PADDING KULLANILIR
            .fillMaxSize()
            .padding(horizontal = 16.dp), // İçerik için ek yatay padding
        verticalArrangement = Arrangement.spacedBy(20.dp), // Öğeler arasındaki boşluk
        contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp) // İlk öğe için küçük üst boşluk
    ) {

        // --- 1. HAVA DURUMU VE KONUM KARTI ---
        item {
            WeatherInfoCard(weather = uiState.weather)
        }

        // --- 2. DAİRESEL NEM/SICAKLIK GÖSTERGESİ ---
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularGauge(
                    value = uiState.weather.humidityPercentage,
                    title = "Sıcaklık",
                    subtitle = "Nem:",
                )
            }
        }

        // --- 3. SIK KULLANILAN CİHAZLAR BAŞLIĞI ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle(title = "Sık Kullanılan Cıhazlar")
                TextButton(onClick = { /* Tüm Cihazlar Ekranına Git */ }) {
                    Text("Tümünü Göster")
                    Icon(Icons.Filled.ArrowForward, contentDescription = null)
                }
            }
        }

        // Hızlı Kontrol Kartlarının Yatay Listesi
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(uiState.quickAccessDevices) { device ->
                    QuickControlToggleCard(
                        device = device,
                        onToggle = { deviceId ->
                            viewModel.toggleDevice(deviceId)
                        }
                    )
                }
            }
        }

        // --- 4. AGENT AI ÖNERİLERİ ---
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle(title = "Akıllı Öneriler")
        }

        // En alta boşluk
        item { Spacer(Modifier.height(20.dp)) }
    }
}

// ------------------------------------------------------------------
// YARDIMCI BİLEŞENLER (HomeScreen'e özel)
// ------------------------------------------------------------------

// Hava Durumu Kartı
@Composable
fun WeatherInfoCard(weather: com.example.iothome.data.model.WeatherState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sol Taraf: Tarih, Konum, Durum
            Column {
                Text(weather.date, style = MaterialTheme.typography.labelMedium)
                Text(weather.location, style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Hava Durumu İkonu (Şimdilik placeholder)
                    Icon(Icons.Filled.Cloud, contentDescription = null, Modifier.size(20.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(weather.condition, style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Sağ Taraf: Sıcaklık
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    weather.temperature,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary // Vurgu rengi
                )
                Text("En Yüksek: ${weather.temperatureHigh}", style = MaterialTheme.typography.labelSmall)
                Text("En Düşük: ${weather.temperatureLow}", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

// SectionTitle (Eğer ui/components altında yoksa buraya ekleyin)
@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}