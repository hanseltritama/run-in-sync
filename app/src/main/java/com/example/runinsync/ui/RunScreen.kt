package com.example.runinsync.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.runinsync.model.RunSong
import com.example.runinsync.model.RunState
import com.example.runinsync.ui.theme.RunInSyncTheme

private val DurationPresets = listOf(5, 10, 15, 20, 30, 45, 60)

@Composable
fun RunScreen(
    modifier: Modifier = Modifier,
    viewModel: RunViewModel = viewModel(),
    onRunComplete: () -> Unit = {}
) {
    val runState by viewModel.runStateFlow.collectAsState()
    val durationMinutes by viewModel.durationMinutes.collectAsState()
    val completedSession by viewModel.completedSession.collectAsState()

    var targetCadenceText by remember { mutableStateOf("180") }

    LaunchedEffect(completedSession) {
        if (completedSession != null) {
            onRunComplete()
        }
    }

    RunScreenContent(
        modifier = modifier,
        runState = runState,
        durationMinutes = durationMinutes,
        targetCadenceText = targetCadenceText,
        onTargetCadenceTextChange = { targetCadenceText = it.filter { ch -> ch.isDigit() } },
        onDurationSelected = { viewModel.setDurationMinutes(it) },
        onStartClick = {
            val bpm = targetCadenceText.toIntOrNull()?.coerceIn(60, 240) ?: 180
            targetCadenceText = bpm.toString()
            viewModel.startRun(bpm)
        },
        onStopClick = { viewModel.stopRun() }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RunScreenContent(
    modifier: Modifier = Modifier,
    runState: RunState,
    durationMinutes: Int,
    targetCadenceText: String,
    onTargetCadenceTextChange: (String) -> Unit,
    onDurationSelected: (Int) -> Unit,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Run",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Duration",
            style = MaterialTheme.typography.titleSmall
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DurationPresets.forEach { minutes ->
                FilterChip(
                    selected = durationMinutes == minutes,
                    onClick = {
                        if (!runState.isRunning) {
                            onDurationSelected(minutes)
                        }
                    },
                    label = { Text("${minutes} min") }
                )
            }
        }

        OutlinedTextField(
            value = targetCadenceText,
            onValueChange = onTargetCadenceTextChange,
            label = { Text("Target cadence (BPM)") },
            enabled = !runState.isRunning,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        if (runState.isRunning) {
            OutlinedButton(
                onClick = onStopClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Stop Run")
            }
        } else {
            Button(
                onClick = onStartClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start Run")
            }
        }

        if (runState.isRunning) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Live",
                        style = MaterialTheme.typography.titleSmall
                    )
                    StatLine(label = "Current cadence", value = "%.1f BPM".format(runState.currentCadence))
                    StatLine(label = "Target cadence", value = "${runState.targetCadence} BPM")
                    StatLine(
                        label = "Song",
                        value = runState.currentSong?.title ?: "—"
                    )
                    StatLine(label = "Score", value = "%.1f".format(runState.currentScore))
                    StatLine(label = "Streak", value = "${runState.currentStreak} s")
                }
            }
        }
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview(showBackground = true, name = "Idle")
@Composable
private fun RunScreenPreviewIdle() {
    RunInSyncTheme {
        RunScreenContent(
            runState = RunState(isRunning = false),
            durationMinutes = 20,
            targetCadenceText = "180",
            onTargetCadenceTextChange = {},
            onDurationSelected = {},
            onStartClick = {},
            onStopClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Running")
@Composable
private fun RunScreenPreviewRunning() {
    RunInSyncTheme {
        RunScreenContent(
            runState = RunState(
                currentCadence = 177.3f,
                targetCadence = 180,
                currentSong = RunSong("main-1", "Main 1", 180, 60),
                currentScore = 87.2f,
                currentStreak = 14,
                isRunning = true
            ),
            durationMinutes = 20,
            targetCadenceText = "180",
            onTargetCadenceTextChange = {},
            onDurationSelected = {},
            onStartClick = {},
            onStopClick = {}
        )
    }
}
