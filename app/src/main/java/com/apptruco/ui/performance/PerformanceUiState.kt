package com.apptruco.ui.performance

import com.apptruco.domain.model.Game
import com.apptruco.domain.model.GameConfig
import com.apptruco.domain.model.PointSource
import com.apptruco.domain.model.Score
import com.apptruco.domain.model.Team
import com.apptruco.domain.rules.Performance
import com.apptruco.domain.rules.performance

data class PerformanceUiState(
    val config: GameConfig,
    val winner: Team?,
    val score: Score,
    val performance: Performance,
    val sources: List<PointSource>,
)

fun Game.toPerformanceUiState(): PerformanceUiState {
    val performance = performance()
    return PerformanceUiState(
        config = config,
        winner = winner,
        score = score,
        performance = performance,
        sources = PointSource.entries.filter { source ->
            when (source) {
                PointSource.HAND, PointSource.ENVIDO -> true
                PointSource.FLOR -> config.withFlor
                PointSource.MANUAL -> performance.pointsBySource.values.any { it.getValue(source) != 0 }
            }
        },
    )
}
