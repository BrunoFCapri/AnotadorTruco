package com.apptruco

import android.app.Application
import com.apptruco.data.DataStoreGameRepository
import com.apptruco.domain.GameRepository
import com.apptruco.ui.theme.precalentarLetras
import kotlin.concurrent.thread

class TrucoApplication : Application() {
    val gameRepository: GameRepository by lazy { DataStoreGameRepository.create(this) }

    override fun onCreate() {
        super.onCreate()
        thread(name = "precalentar-letras") { precalentarLetras(this) }
    }
}
