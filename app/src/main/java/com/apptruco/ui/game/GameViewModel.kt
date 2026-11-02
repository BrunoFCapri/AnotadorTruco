package com.apptruco.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.apptruco.TrucoApplication
import com.apptruco.domain.GameRepository
import com.apptruco.domain.Session
import com.apptruco.domain.model.GameAction
import com.apptruco.domain.model.GameConfig
import com.apptruco.domain.model.Team
import com.apptruco.ui.performance.toPerformanceUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameViewModel(private val repository: GameRepository) : ViewModel() {

    private enum class Destination { GAME, SETUP, PERFORMANCE }

    private val destination = MutableStateFlow(Destination.GAME)

    val uiState: StateFlow<GameScreenState> =
        combine(repository.session, destination) { session, current ->
            when {
                session == null -> GameScreenState.Setup(GameConfig(), Team.NOSOTROS, canCancel = false)
                current == Destination.SETUP ->
                    GameScreenState.Setup(session.game.config, session.game.hand.mano, canCancel = true)
                current == Destination.PERFORMANCE ->
                    GameScreenState.Performance(session.game.toPerformanceUiState())
                else -> GameScreenState.Playing(session.toUiState())
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GameScreenState.Loading)

    fun onAction(action: GameAction) = edit { it?.apply(action) }

    fun onUndo() = edit { it?.undo() }

    fun onRematch() {
        viewModelScope.launch {
            repository.update { it?.restart(it.game.config, it.game.hand.mano) }
            destination.value = Destination.GAME
        }
    }

    fun onOpenSetup() {
        destination.value = Destination.SETUP
    }

    fun onOpenPerformance() {
        destination.value = Destination.PERFORMANCE
    }

    fun onBackToGame() {
        destination.value = Destination.GAME
    }

    fun onStart(config: GameConfig, mano: Team) {
        viewModelScope.launch {
            repository.update { it?.restart(config, mano) ?: Session.start(config, mano) }
            destination.value = Destination.GAME
        }
    }

    private fun edit(transform: (Session?) -> Session?) {
        viewModelScope.launch { repository.update(transform) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TrucoApplication
                GameViewModel(app.gameRepository)
            }
        }
    }
}
