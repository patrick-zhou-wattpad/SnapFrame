package com.snap.frame.media

import androidx.compose.runtime.Composable

interface PhotoPicker {
    fun pick(onResult: (ByteArray?) -> Unit)
}

@Composable
expect fun rememberPhotoPicker(): PhotoPicker
