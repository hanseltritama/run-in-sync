package com.example.runinsync.domain

import com.example.runinsync.model.RunSession
import com.example.runinsync.model.RunSong
import com.example.runinsync.model.RunState
import com.example.runinsync.model.SongScore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.ceil

/**
 * Orchestrates a run session: builds playlists, combines cadence and music,
 * computes scores and streaks, and emits RunState and RunSession.
 */
class RunEngine(
    private val cadenceTracker: CadenceTracker,
    private val musicPlayer: MusicPlayer,
    private val scope: CoroutineScope,
    private val isQuickTestEnabled: Boolean = false
) {
    private val _runStateFlow = MutableStateFlow(RunState())
    val runStateFlow: StateFlow<RunState> = _runStateFlow.asStateFlow()

    private var runJob: Job? = null

    /**
     * Starts a run with the given target cadence and duration.
     * Uses Option A playlist logic (~3.5 min per song). In debug builds, runs cap at 2 min for quick-test.
     * When the run completes after all songs, invokes [onComplete] with the RunSession.
     */
    fun startRun(targetCadence: Int, durationMinutes: Int, onComplete: (RunSession) -> Unit) {
        stopRun()
        val effectiveMinutes = if (isQuickTestEnabled) minOf(durationMinutes, 2) else durationMinutes
        val targetSeconds = (effectiveMinutes * 60).toInt()
        val songCount = ceil(effectiveMinutes / 3.5f).toInt().coerceAtLeast(2)
        val durationPerSong = targetSeconds / songCount
        val warmUpCount = maxOf(1, songCount / 5)
        val mainCount = songCount - warmUpCount
        val warmUpBpm = (targetCadence - 20).coerceAtLeast(60)
        val warmUpSongs = (1..warmUpCount).map { i ->
            RunSong(
                id = "warm-up-$i",
                title = "Warm-up $i",
                bpm = warmUpBpm,
                durationSeconds = durationPerSong
            )
        }
        val mainSongs = (1..mainCount).map { i ->
            RunSong(
                id = "main-$i",
                title = "Main $i",
                bpm = targetCadence,
                durationSeconds = durationPerSong
            )
        }
        val playlist = warmUpSongs + mainSongs

        musicPlayer.startPlaylist(playlist)

        val songScores = mutableListOf<SongScore>()
        var currentScoreSum = 0f
        var currentScoreCount = 0
        var currentStreak = 0
        var longestStreakThisSong = 0
        var longestStreakOverall = 0
        var previousSong: RunSong? = null
        val targetCadenceFloat = targetCadence.toFloat()

        runJob = scope.launch {
            combine(
                cadenceTracker.cadenceFlow,
                musicPlayer.currentSongFlow
            ) { cadence, song ->
                Pair(cadence, song)
            }.collect { (cadence, currentSong) ->
                // Playlist ended (currentSong becomes null after last song) — check before updating state
                if (previousSong != null && currentSong == null) {
                    // Flush last song's score before creating session
                    val avgScore = if (currentScoreCount > 0) {
                        currentScoreSum / currentScoreCount
                    } else {
                        0f
                    }
                    previousSong?.let { prev ->
                        songScores.add(
                            SongScore(
                                songTitle = prev.title,
                                score = avgScore,
                                longestStreakSeconds = longestStreakThisSong
                            )
                        )
                    }
                    musicPlayer.stop()
                    val finalSongScores = songScores.toList()
                    val averageScore = if (finalSongScores.isNotEmpty()) {
                        finalSongScores.map { it.score }.average().toFloat()
                    } else {
                        0f
                    }
                    val session = RunSession(
                        date = System.currentTimeMillis(),
                        targetCadence = targetCadence,
                        durationMinutes = effectiveMinutes,
                        songScores = finalSongScores,
                        averageScore = averageScore,
                        longestStreak = longestStreakOverall
                    )
                    _runStateFlow.value = RunState(isRunning = false)
                    onComplete(session)
                    runJob?.cancel()
                    return@collect
                }

                val targetBpm = currentSong?.bpm?.toFloat() ?: targetCadenceFloat

                // Song changed: store previous SongScore and reset accumulators
                if (currentSong != previousSong) {
                    previousSong?.let { prev ->
                        val avgScore = if (currentScoreCount > 0) {
                            currentScoreSum / currentScoreCount
                        } else {
                            0f
                        }
                        songScores.add(
                            SongScore(
                                songTitle = prev.title,
                                score = avgScore,
                                longestStreakSeconds = longestStreakThisSong
                            )
                        )
                    }
                    previousSong = currentSong
                    currentScoreSum = 0f
                    currentScoreCount = 0
                    currentStreak = 0
                    longestStreakThisSong = 0
                }

                // Compute instantaneous score: max(0, 100 - abs(cadence - targetBpm) * 5)
                val score = max(0f, 100f - abs(cadence - targetBpm) * 5f)
                currentScoreSum += score
                currentScoreCount++

                // Streak: within ±3 BPM
                if (abs(cadence - targetBpm) <= 3f) {
                    currentStreak++
                    longestStreakThisSong = max(longestStreakThisSong, currentStreak)
                    longestStreakOverall = max(longestStreakOverall, currentStreak)
                } else {
                    currentStreak = 0
                }

                val avgScoreSoFar = if (currentScoreCount > 0) {
                    currentScoreSum / currentScoreCount
                } else {
                    0f
                }

                _runStateFlow.value = RunState(
                    currentCadence = cadence,
                    targetCadence = targetCadence,
                    currentSong = currentSong,
                    currentScore = avgScoreSoFar,
                    currentStreak = currentStreak,
                    isRunning = true
                )
            }
        }
    }

    fun stopRun() {
        runJob?.cancel()
        runJob = null
        musicPlayer.stop()
        _runStateFlow.value = RunState()
    }
}
