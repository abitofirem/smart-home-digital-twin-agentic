package com.example.iothome.ui.screens.agent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.iothome.ui.components.AISuggestionCard
import com.example.iothome.ui.components.RoutineCard
import com.example.iothome.ui.screens.home.SectionTitle // HomeScreen'deki başlık bileşeni

@Composable
fun AgentScreen(
    bottomNavController: NavHostController,
    paddingValues: PaddingValues, // Ana Scaffold'dan gelen padding
    viewModel: AgentViewModel = viewModel() // AgentViewModel'ı çağır
) {
    // ViewModel'dan gelen AgentUiState'i dinle
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .padding(paddingValues) // Ana Scaffold'dan gelen padding'i uygula
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // --- 1. BAĞLAMSAL KART (En Önemli Öneri) ---
        if (uiState.contextualSuggestion != null) {
            item {
                SectionTitle(title = "Sizin İçin Öneri")
                AISuggestionCard(
                    suggestion = uiState.contextualSuggestion!!,
                    onApply = { viewModel.applySuggestion(uiState.contextualSuggestion!!) }
                )
            }
        }

        // --- 2. DİĞER AI ÖNERİLERİ LİSTESİ ---
        item { SectionTitle(title = "Akıllı Öneriler") }

        if (uiState.suggestions.isEmpty() && !uiState.isLoading) {
            item { Text("Aktif öneri bulunamadı.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            items(uiState.suggestions) { suggestion ->
                AISuggestionCard(
                    suggestion = suggestion,
                    onApply = { viewModel.applySuggestion(suggestion) }
                )
            }
        }

        // --- 3. KULLANICI RUTİNLERİ (SCENES) LİSTESİ ---
        item { SectionTitle(title = "Rutinler") }

        if (uiState.routines.isEmpty() && !uiState.isLoading) {
            item { Text("Tanımlı rutin bulunamadı.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            items(uiState.routines) { routine ->
                RoutineCard(
                    routine = routine,
                    onClick = { viewModel.activateRoutine(routine) }
                )
            }
        }
    }
}