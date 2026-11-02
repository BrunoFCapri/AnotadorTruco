package com.apptruco.domain.model

sealed interface GameAction {
    data class AddPoints(val team: Team, val points: Int) : GameAction
    data class Sing(val team: Team, val canto: Canto) : GameAction
    data class Answer(val accept: Boolean) : GameAction
    data object CancelCanto : GameAction
    data class WinTanto(val team: Team) : GameAction
    data class WinHand(val team: Team) : GameAction
    data object PassMano : GameAction
}
