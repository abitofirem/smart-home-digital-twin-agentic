package com.example.iothome.ui.screens.catalog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.iothome.data.model.DeviceType
import com.example.iothome.ui.components.DeviceCatalogCard
import com.example.iothome.ui.theme.BackgroundDark // Koyu Tema

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceCatalogScreen(navController: NavHostController) {

    // Simülasyon verisi: Cihaz tiplerini ve sayısını gösteren mock liste
    val deviceTypeList = listOf(
        Pair(DeviceType.LIGHT, 3),
        Pair(DeviceType.THERMOSTAT, 2),
        Pair(DeviceType.FAN, 3),
        Pair(DeviceType.TV, 1),
        Pair(DeviceType.PURIFIER, 2),
        Pair(DeviceType.WASHING_MACHINE, 1),
        Pair(DeviceType.GARAGE_DOOR, 1),
        Pair(DeviceType.COFFEE_MACHINE, 1),
        Pair(DeviceType.SENSOR, 4)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tüm Cihazlar") }, // Görseldeki Başlık
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { // Geri butonu
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Yeni Cihaz Ekleme Ekranına Navigasyon */ }) {
                        Icon(Icons.Filled.Add, contentDescription = "Yeni Cihaz Ekle")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        },
        containerColor = BackgroundDark // Koyu tema arka planı
    ) { padding ->

        // LazyVerticalGrid: Görseldeki gibi 3 sütunlu ızgara düzeni sağlar
        LazyVerticalGrid(
            columns = GridCells.Fixed(3), // 3 Cihaz Kartı
            contentPadding = PaddingValues(top = padding.calculateTopPadding() + 8.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(deviceTypeList) { (type, count) ->
                DeviceCatalogCard(
                    deviceType = type,
                    deviceCount = count,
                    onClick = {
                        // Tıklanınca o cihaz tipinin detay ekranına gidilebilir
                        // Örn: navController.navigate("device_type_detail/${type.name}")
                    }
                )
            }
        }
    }
}