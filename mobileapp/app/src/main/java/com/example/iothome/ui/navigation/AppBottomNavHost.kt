package com.example.iothome.ui.navigation

import androidx.compose.foundation.layout.PaddingValues // PaddingValues importu
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType // NavType importu KALDIRILABİLİR (RoomDetail ile gitti)
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument // navArgument importu KALDIRILABİLİR (RoomDetail ile gitti)
import com.example.iothome.ui.screens.agent.AgentScreen
import com.example.iothome.ui.screens.catalog.DeviceCatalogScreen
import com.example.iothome.ui.screens.home.HomeScreen
import com.example.iothome.ui.screens.rooms.NewRoomScreen
// import com.example.iothome.ui.screens.rooms.RoomDetailScreen // <-- BU IMPORT KALDIRILDI
import com.example.iothome.ui.screens.rooms.RoomsScreen
import com.example.iothome.ui.screens.settings.SettingsScreen

/**
 * Ana NavHost: Bottom Navigation ve diğer tüm ekran rotalarını yönetir.
 */
@Composable
fun AppBottomNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Home.route,
        modifier = modifier
    ) {
        // --- 1. BOTTOM NAV BAR ROTLARI ---
        composable(BottomNavItem.Home.route) {
            HomeScreen(bottomNavController = navController, paddingValues = paddingValues)
        }
        composable(BottomNavItem.Rooms.route) {
            RoomsScreen(bottomNavController = navController, paddingValues = paddingValues)
        }
        composable(BottomNavItem.Routines.route) {
      //      AgentScreen(bottomNavController = navController, paddingValues = paddingValues)
        }
        composable(BottomNavItem.Settings.route) {
      //      SettingsScreen(bottomNavController = navController, paddingValues = paddingValues)
        }

        // --- 2. DİĞER EKRAN ROTLARI ---

        // Yeni Oda Ekleme Rotası
        // KRİTİK DÜZELTME: Yeni Oda Ekleme Rotası artık aktif
        composable("new_room_route") {
            NewRoomScreen(navController = navController)
        }

       // }

        // --- ODA DETAY ROTASI BURADAN KALDIRILDI ---
        /*
        composable(
            route = "room_detail/{roomId}",
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) { backStackEntry ->
            // RoomDetailScreen çağrısı kaldırıldı
        }
        */



        // Cihaz Kataloğu Rotası
        composable(DrawerItem.DeviceCatalog.route) {
            DeviceCatalogScreen(navController = navController)
        }

        // Diğer Drawer Rotaları (Placeholder'lar)
        composable(DrawerItem.Diagnostics.route) { Text("Tanılama Ekranı") }
        composable(DrawerItem.About.route) { Text("Hakkında Ekranı") }
        composable(DrawerItem.AppSettings.route) { Text("Uygulama Ayarları Ekranı") }
    }
}