package com.example.runinsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.runinsync.ui.RunScreen
import com.example.runinsync.ui.RunViewModel
import com.example.runinsync.ui.SummaryScreen
import com.example.runinsync.ui.theme.RunInSyncTheme

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContent {
			RunInSyncTheme {
				RunApp()
			}
		}
	}
}

@Composable
private fun RunApp() {
	val viewModel: RunViewModel = viewModel()
	val completedSession by viewModel.completedSession.collectAsState()

	Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
		when (val session = completedSession) {
			null -> RunScreen(
				modifier = Modifier.padding(innerPadding),
				viewModel = viewModel
			)
			else -> SummaryScreen(
				runSession = session,
				onBack = { viewModel.clearCompletedSession() },
				modifier = Modifier.padding(innerPadding)
			)
		}
	}
}
