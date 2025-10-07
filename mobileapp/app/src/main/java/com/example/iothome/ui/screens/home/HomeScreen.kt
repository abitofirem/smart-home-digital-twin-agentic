package com.example.iothome.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // ViewModel'ı almak için

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    // ViewModel'ı Compose yapısı içinde alıyoruz
    viewModel: HomeViewModel = viewModel()
) {
    // ViewModel'dan gelen durumu izle
    val light by viewModel.lightState.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text(light.name + " Kontrol") }) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Işık Simgesi: Duruma göre renk değiştirir
            Icon(
                imageVector = Icons.Filled.Lightbulb,
                contentDescription = light.name,
                modifier = Modifier.size(120.dp),
                tint = if (light.isOn) Color.Yellow else Color.Gray
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Durum Yazısı
            Text(
                text = if (light.isOn) "AÇIK" else "KAPALI",
                style = MaterialTheme.typography.headlineMedium,
                color = if (light.isOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Toggle Butonu (Switch)
            Switch(
                checked = light.isOn,
                onCheckedChange = {
                    // Kullanıcı Switch'i değiştirdiğinde ViewModel'a bildir
                    viewModel.toggleLight()
                },
                modifier = Modifier.scale(1.5f) // Butonu biraz büyütmek için
            )
        }
    }
}