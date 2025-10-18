package com.example.iothome.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

// Alt Navigasyon Çubuğu Öğelerinin Tanımı
sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    // 1. Home
    object Home : BottomNavItem(
        route = "home_route",
        icon = Icons.Default.Home,
        label = "Anasayfa"
    )

    // 2. Rooms
    object Rooms : BottomNavItem(
        route = "rooms_route",
        icon = Icons.Default.LocationOn, // Oda/Konum ikonu uygun olabilir
        label = "Odalar"
    )

    // 3. Routines (Agent AI)
    object Routines : BottomNavItem(
        route = "routines_route",
        icon = Icons.Default.AutoAwesome, // AI/Rutinler için sihirli değnek
        label = "Rutinler"
    )

    // 4. Settings
    object Settings : BottomNavItem(
        route = "settings_route",
        icon = Icons.Default.Settings,
        label = "Ayarlar"
    )
}

// Navigasyon Çubuğunda listelenecek tüm öğeler
val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Rooms,
    BottomNavItem.Routines,
    BottomNavItem.Settings
)