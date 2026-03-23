package com.example.runinsync.domain

import kotlinx.coroutines.flow.Flow

interface CadenceTracker {
    val cadenceFlow: Flow<Float>
}
