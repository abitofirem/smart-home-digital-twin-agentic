package com.example.iothome.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.iothome.ui.navigation.AppBottomNavHost
import com.example.iothome.ui.theme.BackgroundDark // Koyu tema arka planı
import kotlinx.coroutines.launch

// Ana ekran, Bottom Nav Bar'ı ve Yan Menüyü barındıran Scaffolding'dir.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    // Navigasyon ve Drawer yönetimi için gerekli state ve scope'lar
    val bottomNavController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Yan Menü (Drawer) Kontainer'ı
    ModalNavigationDrawer(
        drawerState = drawerState,
        // AppDrawer içeriği
        drawerContent = {
            AppDrawer(
                navController = bottomNavController,
                closeDrawer = {
                    // Drawer içeriği tıklandığında menüyü kapatma fonksiyonu
                    scope.launch { drawerState.close() }
                }
            )
        },
        // Ana içerik
        content = {
            // Scaffold (Top Bar ve Bottom Bar'ı içeren ana iskelet)
            Scaffold(
                // Top Bar: Yan Menüyü açma ikonu buraya yerleştirilir
                topBar ={
                    TopAppBar(
                        title = {  }, // Genel proje adı
                        navigationIcon = {
                            IconButton(onClick = {
                                // Menü ikonuna basıldığında menüyü aç
                                scope.launch { drawerState.open() }
                            }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Menü")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
                    )
                },
                // Bottom Bar: Animasyonlu navigasyon çubuğu
                bottomBar = { AppBottomBar(bottomNavController) },
                containerColor = BackgroundDark // Koyu tema arka planı
            ) { innerPadding ->

                // Bottom Nav Bar'ın içeriğini (sekme sayfalarını) yöneten NavHost
                AppBottomNavHost(
                    navController = bottomNavController,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    )
}
// AppBottomBar.kt dosyasında bulunan diğer Composable'lar (AppBottomBar, CustomBottomNavItem) buraya dahil edilmiştir.