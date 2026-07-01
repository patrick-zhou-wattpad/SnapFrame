package com.snap.frame.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberPhotoPicker(): PhotoPicker {
    return remember {
        object : PhotoPicker {
            override fun pick(onResult: (ByteArray?) -> Unit) {
                // TODO: implement web picker. For now compile.
                onResult(null)
            }
        }
    }
}

