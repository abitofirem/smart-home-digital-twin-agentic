package com.example.iothome.ui.screens.rooms

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.iothome.data.model.Device
import com.example.iothome.data.model.DeviceType
import com.example.iothome.ui.screens.home.HomeViewModel
import com.example.iothome.ui.theme.BackgroundDark
import androidx.compose.material.icons.filled.* // Tüm ikonlar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewRoomScreen(navController: NavHostController, viewModel: HomeViewModel = viewModel()) {

    var roomName by remember { mutableStateOf("") }
    var selectedDevices by remember {
        mutableStateOf(
            listOf(
                Device(id="light_1", name="Ceiling Light", type = DeviceType.LIGHT, roomId = ""), // Mock cihaz
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yeni Oda Ekle") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        },
        containerColor = BackgroundDark
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- 1. ODA ADI ---
            item {
                Text("Ad", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = roomName,
                    onValueChange = { roomName = it },
                    label = { Text("Oda Adı", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // --- 2. DUVAR KAĞIDI SEÇİMİ ---
            item {
                Text("Duvar Kağıdı", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = { /* Galeri Açma Akışı */ },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.width(8.dp))
                    Text("Görsel Ekle", color = MaterialTheme.colorScheme.onSurface)
                }
            }

            // --- 3. CİHAZ EKLEME VE LİSTESİ ---
            item {
                Text("Cihazlar", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))

                // Seçili Cihazların Yatay Listesi
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // 1. Cihaz Ekleme Butonu
                    DeviceAddButton(onClick = { navController.navigate("device_catalog_route") })

                    // 2. Seçili Cihazlar Listesi (Çıkarma butonu için Box/Stack kullanıldı)
                    selectedDevices.forEach { device ->
                        Box(
                            // Bu dış Box, Z-sırasını yönetir.
                            contentAlignment = Alignment.TopStart
                        ) {
                            // a) Ana Cihaz Çipi
                            DeviceChipContent(name = device.name)

                            // b) KIRMIZI KALDIRMA BUTONU
                            DeviceRemoveButton(
                                onRemove = {
                                    selectedDevices = selectedDevices.filter { it.id != device.id }
                                },
                                // KRİTİK DÜZELTME: align modifier'ını buraya uyguluyoruz
                                modifier = Modifier.align(Alignment.TopEnd)
                            )
                        }
                    }
                }
            }

            // --- 4. ODA EKLE BUTONU ---
            item {
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = {
                        println("Yeni Oda Eklendi: $roomName. Cihaz Sayısı: ${selectedDevices.size}")
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = roomName.isNotEmpty() && selectedDevices.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Yeni Oda Ekle")
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

// ------------------------------------------------------------------
// BİLEŞENLER
// ------------------------------------------------------------------

// Cihaz Ekleme Butonu Bileşeni (Tıklama hatası çözüldü)
@Composable
fun DeviceAddButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val indication = rememberRipple(color = MaterialTheme.colorScheme.primary) // Güvenli Ripple

    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(
                onClick = onClick,
                interactionSource = interactionSource,
                indication = indication
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Filled.Add,
            contentDescription = "Cihaz Ekle",
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

// Ana Cihaz Çipi İçeriği (Sadece Görünüm)
@Composable
fun DeviceChipContent(name: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Lightbulb, contentDescription = null, modifier = Modifier.size(36.dp))
        }
        Text(name, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp), color = MaterialTheme.colorScheme.onSurface)
    }
}

// Kırmızı Çıkarma Butonu (Dışarı Taşarılmış)
@Composable
fun DeviceRemoveButton(onRemove: () -> Unit, modifier: Modifier = Modifier) { // Modifier parametresi ekledik
    val interactionSource = remember { MutableInteractionSource() }
    val indication = rememberRipple(color = Color.White)

    // Kırmızı butonu içeren Box
    Box(
        // KRİTİK DÜZELTME: .align(...) modifier'ı buradan KALDIRILDI!
        modifier = modifier // Artık modifier'ı dışarıdan alacak
            .offset(x = 6.dp, y = (-6).dp)
            .size(24.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.error)
            .clickable(
                onClick = onRemove,
                interactionSource = interactionSource,
                indication = indication
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Close, contentDescription = "Kaldır", tint = Color.White, modifier = Modifier.size(16.dp))
    }
}