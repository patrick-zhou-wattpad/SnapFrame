package com.snap.frame.features.studio.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged

// Displays the background and all editable overlay images
@Composable
internal fun PhotoEditorCanvas(
    backgroundImage: ImageBitmap,
    overlays: List<OverlayPhoto>,
    selectedOverlayId: Long?,
    overlayRemoveMenuId: Long?,
    onOverlaySelected: (Long) -> Unit,
    onOverlayShowRemove: (Long) -> Unit,
    onOverlayRemove: (Long) -> Unit,
    onOverlayTransformChange: (Long, OverlayTransform) -> Unit,
    modifier: Modifier = Modifier
) {
    // Track the actual canvas size for overlay positioning and resizing
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
        // Fill the canvas with the cropped background image
        Image(
            bitmap = backgroundImage,
            contentDescription = "Background photo",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        // Draw each overlay in list order so newer/selected overlays appear on top
        if (canvasSize.width > 0f && canvasSize.height > 0f) {
            overlays.forEach { overlay ->
                key(overlay.id) {
                    EditableOverlayImage(
                        overlay = overlay,
                        canvasSize = canvasSize,
                        isSelected = overlay.id == selectedOverlayId,
                        showRemoveButton = overlay.id == overlayRemoveMenuId,
                        onSelect = {
                            onOverlaySelected(overlay.id)
                        },
                        onShowRemove = {
                            onOverlayShowRemove(overlay.id)
                        },
                        onRemove = {
                            onOverlayRemove(overlay.id)
                        },
                        onTransformChange = { transform ->
                            onOverlayTransformChange(
                                overlay.id,
                                transform
                            )
                        }
                    )
                }
            }
        }
    }
}
