package com.example.runinsync.domain

import com.example.runinsync.model.RunSong
import kotlinx.coroutines.flow.StateFlow

interface MusicPlayer {
    val currentSongFlow: StateFlow<RunSong?>
    fun startPlaylist(songs: List<RunSong>)
    fun stop()
}
