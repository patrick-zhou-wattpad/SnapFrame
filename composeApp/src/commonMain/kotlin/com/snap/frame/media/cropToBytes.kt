package com.snap.frame.media

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap

expect fun cropToBytes(
    source: ImageBitmap,
    cropTopLeft: Offset,
    cropSize: Size,
    viewportSize: Size,
    imageScale: Float
): ByteArray
