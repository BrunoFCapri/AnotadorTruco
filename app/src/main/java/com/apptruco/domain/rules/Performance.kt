package com.apptruco.domain.rules

import com.apptruco.domain.model.Game
import com.apptruco.domain.model.PointSource
import com.apptruco.domain.model.Score
import com.apptruco.domain.model.Team

data class Performance(
    /** Puntaje después de cada jugada que sumó o restó, empezando en 0 a 0. */
    val progression: List<Score>,
    val pointsBySource: Map<Team, Map<PointSource, Int>>,
    val biggestLead: Map<Team, Int>,
)

fun Game.performance(): Performance {
    val progression = timeline.runningFold(Score()) { score, event ->
        score.plus(event.team, event.points, cap = config.target)
    }
    return Performance(
        progression = progression,
        pointsBySource = Team.entries.associateWith { team ->
            PointSource.entries.associateWith { source ->
                timeline.filter { it.team == team && it.source == source }.sumOf { it.points }
            }
        },
        biggestLead = Team.entries.associateWith { team ->
            progression.maxOf { it[team] - it[team.opponent] }.coerceAtLeast(0)
        },
    )
}
