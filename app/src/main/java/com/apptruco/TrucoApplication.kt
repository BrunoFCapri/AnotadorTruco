package com.apptruco

import android.app.Application
import com.apptruco.data.DataStoreGameRepository
import com.apptruco.domain.GameRepository

class TrucoApplication : Application() {
    val gameRepository: GameRepository by lazy { DataStoreGameRepository.create(this) }
}
