package com.example.runinsync.domain

import com.example.runinsync.model.RunSong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Dummy music player for testing. Iterates through a playlist,
 * emitting each song and holding it for its duration before advancing.
 */
class DummyMusicPlayer(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) : MusicPlayer {

    private val _currentSongFlow = MutableStateFlow<RunSong?>(null)
    override val currentSongFlow: StateFlow<RunSong?> = _currentSongFlow.asStateFlow()

    private var playlistJob: Job? = null

    override fun startPlaylist(songs: List<RunSong>) {
        stop()
        playlistJob = scope.launch {
            for (song in songs) {
                _currentSongFlow.value = song
                delay(song.durationSeconds * 1000L)
            }
            _currentSongFlow.value = null
        }
    }

    override fun stop() {
        playlistJob?.cancel()
        playlistJob = null
        _currentSongFlow.value = null
    }
}
