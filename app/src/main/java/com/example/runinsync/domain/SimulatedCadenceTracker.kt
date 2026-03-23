package com.example.runinsync.domain

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flatMapLatest

/**
 * Simulated cadence tracker that emits cadence values around the target BPM
 * every second, with variation of ±10 BPM for testing.
 */
class SimulatedCadenceTracker(
    private val targetBpmFlow: Flow<Float>
) : CadenceTracker {

    override val cadenceFlow: Flow<Float> = targetBpmFlow.flatMapLatest { target ->
        flow {
            while (true) {
                val variation = (-10..10).random()
                emit((target + variation).coerceIn(0f, Float.MAX_VALUE))
                delay(1000)
            }
        }
    }
}
