package com.snap.frame

import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.ui.Modifier

actual fun Modifier.platformGestureExclusion(): Modifier =
    this.systemGestureExclusion()
