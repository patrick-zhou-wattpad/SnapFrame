package com.snap.frame.features.studio.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged


// Photo editing canvas
@Composable
internal fun PhotoEditorCanvas(
    backgroundImage: ImageBitmap,
    overlayImage: ImageBitmap?,
    modifier: Modifier = Modifier
) {
    var canvasSize by remember {
        mutableStateOf(Size.Zero)
    }

    Box(
        modifier = modifier
            .clipToBounds()
            .onSizeChanged { size ->
                canvasSize = Size(
                    width = size.width.toFloat(),
                    height = size.height.toFloat()
                )
            }
    ) {
        Image(
            bitmap = backgroundImage,
            contentDescription = "Background photo",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        if (
            overlayImage != null &&
            canvasSize.width > 0f &&
            canvasSize.height > 0f
        ) {
            EditableOverlayImage(
                image = overlayImage,
                canvasSize = canvasSize
            )
        }
    }
}
