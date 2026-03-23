package com.example.runinsync.model

data class RunSession(
    val date: Long,
    val targetCadence: Int,
    val durationMinutes: Int,
    val songScores: List<SongScore>,
    val averageScore: Float,
    val longestStreak: Int
)
