package com.example.runinsync.model

data class RunState(
    val currentCadence: Float = 0f,
    val targetCadence: Int = 0,
    val currentSong: RunSong? = null,
    val currentScore: Float = 0f,
    val currentStreak: Int = 0,
    val isRunning: Boolean = false
)
