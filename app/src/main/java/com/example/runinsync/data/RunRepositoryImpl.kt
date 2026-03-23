package com.example.runinsync.data

import com.example.runinsync.model.RunSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RunRepositoryImpl : RunRepository {
    private val sessions = mutableListOf<RunSession>()

    override suspend fun saveRunSession(session: RunSession) {
        sessions.add(session)
    }

    override fun getPastRunSessions(): Flow<List<RunSession>> = flow {
        emit(sessions.toList())
    }
}
