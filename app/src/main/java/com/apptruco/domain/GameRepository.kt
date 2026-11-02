package com.apptruco.domain

import kotlinx.coroutines.flow.Flow

interface GameRepository {
    val session: Flow<Session?>

    suspend fun update(transform: (Session?) -> Session?)
}
