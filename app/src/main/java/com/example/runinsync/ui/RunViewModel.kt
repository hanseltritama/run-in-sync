package com.example.runinsync.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runinsync.BuildConfig
import com.example.runinsync.data.RunRepository
import com.example.runinsync.data.RunRepositoryImpl
import com.example.runinsync.domain.DummyMusicPlayer
import com.example.runinsync.domain.RunEngine
import com.example.runinsync.domain.SimulatedCadenceTracker
import com.example.runinsync.model.RunSession
import com.example.runinsync.model.RunState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class RunViewModel(
    private val runRepository: RunRepository = RunRepositoryImpl()
) : ViewModel() {

    private val musicPlayer = DummyMusicPlayer(viewModelScope)

    private val trackerFallbackBpm = MutableStateFlow(180f)

    private val targetBpmFlow = musicPlayer.currentSongFlow.combine(trackerFallbackBpm) { song, fallback ->
        song?.bpm?.toFloat() ?: fallback
    }

    private val cadenceTracker = SimulatedCadenceTracker(targetBpmFlow)

    private val runEngine = RunEngine(
        cadenceTracker = cadenceTracker,
        musicPlayer = musicPlayer,
        scope = viewModelScope,
        isQuickTestEnabled = BuildConfig.DEBUG
    )

    val runStateFlow: StateFlow<RunState> = runEngine.runStateFlow

    private val _durationMinutes = MutableStateFlow(20)
    val durationMinutes: StateFlow<Int> = _durationMinutes.asStateFlow()

    private val _completedSession = MutableStateFlow<RunSession?>(null)
    val completedSession: StateFlow<RunSession?> = _completedSession.asStateFlow()

    fun setDurationMinutes(minutes: Int) {
        _durationMinutes.value = minutes
    }

    fun startRun(targetCadence: Int) {
        trackerFallbackBpm.value = targetCadence.toFloat()
        runEngine.startRun(targetCadence, _durationMinutes.value) { session ->
            viewModelScope.launch {
                runRepository.saveRunSession(session)
                _completedSession.value = session
            }
        }
    }

    fun stopRun() {
        runEngine.stopRun()
    }

    fun clearCompletedSession() {
        _completedSession.value = null
    }
}
