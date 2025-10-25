package com.example.iothome.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add // (+) İkonu için
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications // Bildirim ikonu için
import androidx.compose.material3.*
import androidx.compose.runtime.* // remember, derivedStateOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState // NavController'ı dinlemek için
import androidx.navigation.compose.rememberNavController
import com.example.iothome.ui.navigation.AppBottomNavHost
import com.example.iothome.ui.navigation.BottomNavItem // Rota isimleri için
import com.example.iothome.ui.theme.BackgroundDark
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {

    val bottomNavController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // --- YENİ: NavController'ı dinleyerek mevcut rotayı alıyoruz ---
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Mevcut rotaya göre TopAppBar başlığını belirliyoruz
    val currentScreenTitle by remember(currentRoute) {
        derivedStateOf {
            when (currentRoute) {
                BottomNavItem.Home.route -> "Merhaba KullaniciAdi!" // Home ekranı başlığı
                BottomNavItem.Rooms.route -> "Tüm Odalar" // Rooms ekranı başlığı
                BottomNavItem.Routines.route -> "Agent AI & Rutinler"
                BottomNavItem.Settings.route -> "Ayarlar"
                // Diğer ekranlar (NewRoom, Catalog) için de başlık eklenebilir
              "new_room_route" -> "Yeni Oda Ekle"
                else -> "" // Varsayılan boş başlık
            }
        }
    }
    // -------------------------------------------------------------------

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                navController = bottomNavController,
                closeDrawer = { scope.launch { drawerState.close() } }
            )
        },
        content = {
            Scaffold(
                topBar = {
                    TopAppBar(
                        // DÜZELTME: Başlık artık dinamik
                        title = { Text(currentScreenTitle) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Menü")
                            }
                        },
                        // DÜZELTME: Aksiyonlar da dinamik olabilir
                        actions = {
                            // Sadece Rooms ekranında (+) butonunu göster
                            if (currentRoute == BottomNavItem.Rooms.route) {
                                IconButton(onClick = { bottomNavController.navigate("new_room_route") }) {
                                    Icon(Icons.Filled.Add, contentDescription = "Oda Ekle")
                                }
                            } else if (currentRoute == BottomNavItem.Home.route) {
                                // Home ekranında Bildirim ikonunu göster
                                IconButton(onClick = { /* Bildirimler */ }) {
                                    Icon(Icons.Filled.Notifications, contentDescription = "Bildirimler")
                                }
                            }
                            // Diğer ekranlar için başka aksiyonlar eklenebilir
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
                    )
                },
                bottomBar = { AppBottomBar(bottomNavController) },
                containerColor = BackgroundDark
            ) { innerPadding ->
                AppBottomNavHost(
                    navController = bottomNavController,
                    modifier = Modifier, // Padding burada uygulanmaz
                    paddingValues = innerPadding // PaddingValues NavHost'a iletilir
                )
            }
        }
    )
}