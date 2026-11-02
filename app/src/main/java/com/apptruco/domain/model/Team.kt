package com.apptruco.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class Team {
    NOSOTROS,
    ELLOS;

    val opponent: Team
        get() = when (this) {
            NOSOTROS -> ELLOS
            ELLOS -> NOSOTROS
        }
}
