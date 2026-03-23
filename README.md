# RunInSync (CadenceRun MVP)

An Android app for rhythm-based running that matches your stride cadence to music tempo. Run in sync with the beat, track your scores, and build consistency with streak tracking.

## Tech Stack

- **Kotlin** + **Jetpack Compose** + **Material 3**
- Coroutines & StateFlow
- Clean architecture (UI, Domain, Data, Model layers)

## Features

- **Duration-based runs**: Select 5–60 minute runs with presets (5, 10, 15, 20, 30, 45, 60 min)
- **Target cadence**: Set your target BPM (e.g. 180 steps/min)
- **Adaptive playlist**: Warm-up songs (~20% at target-20 BPM) + main songs (~3.5 min each)
- **Real-time scoring**: Score = `max(0, 100 - |cadence - target| × 5)` per cadence sample
- **Streak tracking**: Consecutive seconds within ±3 BPM of target
- **Quick-test mode**: Pass `isQuickTestEnabled = true` to RunEngine to cap runs at 2 min (for debugging)

## Project Structure

```
app/src/main/java/com/example/runinsync/
├── MainActivity.kt
├── model/
│   ├── RunSong.kt          # id, title, bpm, durationSeconds
│   ├── SongScore.kt         # songTitle, score, longestStreakSeconds
│   ├── RunSession.kt        # date, targetCadence, durationMinutes, songScores, etc.
│   └── RunState.kt          # currentCadence, targetCadence, currentSong, score, streak, isRunning
├── domain/
│   ├── CadenceTracker.kt    # cadenceFlow: Flow<Float>
│   ├── SimulatedCadenceTracker.kt
│   ├── MusicPlayer.kt       # currentSongFlow, startPlaylist, stop
│   ├── DummyMusicPlayer.kt
│   └── RunEngine.kt         # Orchestrates run, scoring, streaks
├── data/
│   ├── RunRepository.kt
│   └── RunRepositoryImpl.kt
└── ui/
    ├── RunScreen.kt         # (pending)
    ├── SummaryScreen.kt     # (pending)
    └── theme/
```

## Architecture

- **Model**: Data classes for run state, songs, scores, sessions
- **Domain**: RunEngine (scoring, streaks), CadenceTracker, MusicPlayer (simulated implementations for MVP)
- **Data**: RunRepository with in-memory session storage
- **UI**: RunScreen, SummaryScreen, RunViewModel (UI layer in progress)

## Build & Run

```bash
./gradlew installDebug
```

Or open in Android Studio and run on a device/emulator.

**Requirements**: minSdk 28, targetSdk 36

## Status

- ✅ Model, Domain, Data layers implemented
- ✅ RunEngine with duration-based playlists, scoring, streaks
- ⏳ RunViewModel, RunScreen, SummaryScreen, MainActivity navigation (pending)
