package com.example.iothome.ui.screens.agent

import androidx.lifecycle.ViewModel
import com.example.iothome.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

class AgentViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AgentUiState(isLoading = true))
    val uiState: StateFlow<AgentUiState> = _uiState.asStateFlow()

    init {
        loadMockData()
    }

    // UI'ı geliştirebilmek için sahte veri yükler
    private fun loadMockData() {
        _uiState.update {
            it.copy(
                contextualSuggestion = getMockContextualSuggestion(),
                suggestions = getMockSuggestions(),
                routines = getMockRoutines(),
                isLoading = false
            )
        }
    }

    // "Uygula" butonuna basıldığında
    fun applySuggestion(suggestion: Suggestion) {
        println("AI Önerisi Uygulandı: ${suggestion.title}. Komut: ${suggestion.actionCommand}")
        // TODO: MqttRepository.publish(suggestion.actionCommand) çağrılacak
    }

    // Rutin kartına tıklandığında
    fun activateRoutine(routine: Routine) {
        println("Rutin Aktif Edildi: ${routine.name}")
        // TODO: MqttRepository.publish(routine.id) çağrılacak
    }

    // --- Mock Veri Fonksiyonları ---

    private fun getMockContextualSuggestion(): Suggestion {
        return Suggestion(
            id = "ai_ctx_1",
            title = "Konfor Optimizasyonu",
            description = "Dış hava sıcaklığı yükseliyor (28°C). 'Serinletme' modu (Fan+Klima) aktif edilsin mi?",
            icon = Icons.Default.Thermostat,
            actionCommand = "ROUTINE_COOLING_ON"
        )
    }

    private fun getMockSuggestions(): List<Suggestion> {
        return listOf(
            Suggestion(
                id = "ai_sug_1",
                title = "Enerji Tasarrufu Uyarısı",
                description = "Son 15 dakikadır Oturma Odası'nda hareket algılanmadı. Lambayı otomatik kapatmak ister misiniz?",
                icon = Icons.Default.Bolt,
                actionCommand = "light_1/OFF"
            )
        )
    }

    private fun getMockRoutines(): List<Routine> {
        return listOf(
            Routine(
                id = "scene_morning",
                name = "Sabah Modu",
                icon = Icons.Default.WbSunny,
                description = "3 cihaz aktif, kahve hazır.",
                triggerInfo = "Hafta içi 07:00"
            ),
            Routine(
                id = "scene_movie",
                name = "Film Gecesi",
                icon = Icons.Default.Movie,
                description = "Işıklar kapalı, TV açık.",
                triggerInfo = "Manuel"
            ),
            Routine(
                id = "scene_relax",
                name = "Relax Modu",
                icon = Icons.Default.Spa,
                description = "Loş sarı ışık ve ortam sesi.",
                triggerInfo = "Manuel"
            )
        )
    }
}