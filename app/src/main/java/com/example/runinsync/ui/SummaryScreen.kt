package com.example.runinsync.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.runinsync.model.RunSession
import com.example.runinsync.model.SongScore
import com.example.runinsync.ui.theme.RunInSyncTheme

@Composable
fun SummaryScreen(
    runSession: RunSession,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    SummaryScreenContent(
        runSession = runSession,
        onBack = onBack,
        modifier = modifier
    )
}

@Composable
private fun SummaryScreenContent(
    runSession: RunSession,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Summary",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "${runSession.durationMinutes} min run",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Target cadence: ${runSession.targetCadence} BPM",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Overall",
                    style = MaterialTheme.typography.titleSmall
                )
                SummaryStatRow(
                    label = "Average score",
                    value = "%.1f".format(runSession.averageScore)
                )
                SummaryStatRow(
                    label = "Longest streak",
                    value = "${runSession.longestStreak} s"
                )
            }
        }

        Text(
            text = "Songs",
            style = MaterialTheme.typography.titleSmall
        )

        if (runSession.songScores.isEmpty()) {
            Text(
                text = "No song scores recorded.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                runSession.songScores.forEach { songScore ->
                    SongScoreRow(songScore = songScore)
                }
            }
        }

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}

@Composable
private fun SummaryStatRow(label: String, value: String) {
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

@Composable
private fun SongScoreRow(songScore: SongScore) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = songScore.songTitle,
                style = MaterialTheme.typography.titleSmall
            )
            SummaryStatRow(
                label = "Score",
                value = "%.1f".format(songScore.score)
            )
            SummaryStatRow(
                label = "Longest streak",
                value = "${songScore.longestStreakSeconds} s"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SummaryScreenPreview() {
    RunInSyncTheme {
        SummaryScreenContent(
            runSession = RunSession(
                date = 0L,
                targetCadence = 180,
                durationMinutes = 20,
                songScores = listOf(
                    SongScore("Warm-up 1", 82.5f, 12),
                    SongScore("Main 1", 91.0f, 45)
                ),
                averageScore = 86.75f,
                longestStreak = 45
            ),
            onBack = {}
        )
    }
}
