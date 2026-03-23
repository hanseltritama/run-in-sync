package com.example.runinsync.data

import com.example.runinsync.model.RunSession
import kotlinx.coroutines.flow.Flow

interface RunRepository {
    suspend fun saveRunSession(session: RunSession)
    fun getPastRunSessions(): Flow<List<RunSession>>
}
