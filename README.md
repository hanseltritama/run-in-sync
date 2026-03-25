# RunInSync (CadenceRun MVP)

An Android app for rhythm-based running that matches your stride cadence to music tempo. Run in sync with the beat, track your scores, and build consistency with streak tracking.

## Tech Stack

- **Kotlin** + **Jetpack Compose** + **Material 3**
- Coroutines & StateFlow
- **lifecycle-viewmodel-compose** for `viewModel()` in Compose
- Clean architecture (UI, Domain, Data, Model layers)

## Features

- **Duration-based runs**: Dropdown picker for 5, 10, 15, 20, 30, 45, or 60 minutes (default 20)
- **Target cadence**: Editable target BPM (validated 60–240; default 180)
- **Adaptive playlist**: Warm-up segment(s) at target−20 BPM (minimum 60), then main segment(s) at target; song lengths follow Option A in `RunEngine` (total run time split across a song count derived from duration)
- **Simulated cadence**: `SimulatedCadenceTracker` varies BPM around the current song’s tempo
- **Dummy music**: `DummyMusicPlayer` advances through the playlist by each song’s `durationSeconds`
- **Real-time scoring**: Per sample, `max(0, 100 − |cadence − target| × 5)`; song score is the average over the segment
- **Streak tracking**: Consecutive seconds within ±3 BPM of the current target; tracked per song and for the whole run
- **Post-run summary**: Overall average score, longest streak, and per-song breakdown
- **Quick-test (debug only)**: `RunViewModel` passes `isQuickTestEnabled = BuildConfig.DEBUG` into `RunEngine`, which caps the **effective** run length at **2 minutes** while keeping the same scoring and navigation flow (release builds use the full selected duration)

## Navigation

- **Run** → start a run from `RunScreen`; when the playlist finishes, `RunViewModel` saves the session and exposes `completedSession`
- **Summary** → `MainActivity` shows `SummaryScreen` with that session; **Back** clears `completedSession` and returns to **Run**

## Project Structure

```
app/src/main/java/com/example/runinsync/
├── MainActivity.kt          # RunApp: single RunViewModel, Run ↔ Summary
├── model/
│   ├── RunSong.kt
│   ├── SongScore.kt
│   ├── RunSession.kt
│   └── RunState.kt
├── domain/
│   ├── CadenceTracker.kt
│   ├── SimulatedCadenceTracker.kt
│   ├── MusicPlayer.kt
│   ├── DummyMusicPlayer.kt
│   └── RunEngine.kt
├── data/
│   ├── RunRepository.kt
│   └── RunRepositoryImpl.kt
└── ui/
    ├── RunViewModel.kt
    ├── RunScreen.kt
    ├── SummaryScreen.kt
    └── theme/
```

## Architecture

- **Model**: Run state, songs, scores, sessions
- **Domain**: `RunEngine` (playlist build, scoring, streaks); cadence and music are simulated for the MVP
- **Data**: `RunRepository` with in-memory session list
- **UI**: `RunScreen`, `SummaryScreen`, `RunViewModel`; `MainActivity` hosts navigation

## Build & Run

```bash
./gradlew installDebug
```

Or open the project in Android Studio and run on a device or emulator.

**Requirements**: minSdk 28, targetSdk 36

## Status

- Model, domain, data, and UI layers implemented
- `RunEngine` with duration-based playlists, scoring, and streaks
- `RunViewModel`, `RunScreen`, `SummaryScreen`, and `MainActivity` navigation
