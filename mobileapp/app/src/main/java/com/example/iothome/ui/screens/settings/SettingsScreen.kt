package com.example.iothome.ui.screens.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

// Ayarlar Ekranı: MQTT, Profil ve Uygulama Ayarları buraya gelecek.
@Composable
fun SettingsScreen(bottomNavController: NavHostController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "AYARLAR İçeriği")
    }
}