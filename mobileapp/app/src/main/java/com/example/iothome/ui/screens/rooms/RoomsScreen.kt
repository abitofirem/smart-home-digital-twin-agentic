package com.example.iothome.ui.screens.rooms

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

// Odalar Ekranı: 'All Rooms' listesi buraya gelecek.
@Composable
fun RoomsScreen(bottomNavController: NavHostController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "ODALAR (All Rooms) İçeriği")
    }
}