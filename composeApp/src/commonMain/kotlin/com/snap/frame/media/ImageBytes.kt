package com.snap.frame.media

import androidx.compose.ui.graphics.ImageBitmap
import org.jetbrains.compose.resources.decodeToImageBitmap

fun ByteArray.toImageBitmap(): ImageBitmap =
    decodeToImageBitmap()
