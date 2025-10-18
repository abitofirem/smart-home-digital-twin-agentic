package com.example.iothome.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.iothome.ui.screens.agent.AgentScreen
import com.example.iothome.ui.screens.catalog.DeviceCatalogScreen
import com.example.iothome.ui.screens.home.HomeScreen
import com.example.iothome.ui.screens.rooms.RoomsScreen
import com.example.iothome.ui.screens.settings.SettingsScreen

/**
 * Bottom Navigation Bar'da tıklanan sekmeyi ekranda gösteren NavHost bileşenidir.
 */
@Composable
fun AppBottomNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Home.route,
        modifier = modifier
    )

    {// --- 1. BOTTOM NAV BAR ROTLARI  ---
        // 1. Ana Sayfa Sekmesi
        composable(BottomNavItem.Home.route) {
            HomeScreen(bottomNavController = navController)
        }

        // 2. Odalar Sekmesi
        composable(BottomNavItem.Rooms.route) {
            RoomsScreen(bottomNavController = navController)
        }

        // 3. Rutinler (Agent AI) Sekmesi
        composable(BottomNavItem.Routines.route) {
            AgentScreen(bottomNavController = navController)
        }

        // 4. Ayarlar Sekmesi
        composable(BottomNavItem.Settings.route) {
            SettingsScreen(bottomNavController = navController)
        }


        // --- 2. YAN MENÜ (DRAWER) ROTLARI ---

        // Cihaz Kataloğu Rotası
        composable(DrawerItem.DeviceCatalog.route) {
            // Yan menüden gelen tıklamalar bu ekranı gösterir.
            DeviceCatalogScreen(navController = navController)
        }

        // Diğer Drawer Rotaları (Tanılama, Hakkında vb.)
        composable(DrawerItem.Diagnostics.route) {
            Text("Tanılama Ekranı") // Placeholder
        }
        composable(DrawerItem.About.route) {
            Text("Hakkında Ekranı") // Placeholder
        }
        composable(DrawerItem.AppSettings.route) {
            Text("Uygulama Ayarları Ekranı") // Placeholder
        }

    }
}