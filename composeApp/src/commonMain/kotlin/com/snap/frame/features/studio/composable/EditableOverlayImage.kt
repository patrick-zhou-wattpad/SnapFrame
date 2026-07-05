package com.snap.frame.features.studio.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt


// Movable and resizable overlay
@Composable
internal fun EditableOverlayImage(
    image: ImageBitmap,
    canvasSize: Size
) {
    val density = LocalDensity.current

    var overlayOffset by remember(image) {
        mutableStateOf(Offset.Zero)
    }

    var overlaySize by remember(image) {
        mutableStateOf(Size.Zero)
    }

    val currentOverlayOffset by rememberUpdatedState(
        overlayOffset
    )

    val currentOverlaySize by rememberUpdatedState(
        overlaySize
    )

    // Set initial overlay position
    LaunchedEffect(image, canvasSize) {
        val imageAspectRatio =
            image.width.toFloat() /
                image.height.toFloat()

        val initialScale = min(
            canvasSize.width * 0.4f /
                image.width.toFloat(),
            canvasSize.height * 0.4f /
                image.height.toFloat()
        )

        val initialWidth =
            image.width * initialScale

        val initialHeight =
            initialWidth / imageAspectRatio

        overlaySize = Size(
            width = initialWidth,
            height = initialHeight
        )

        overlayOffset = Offset(
            x = (
                canvasSize.width -
                    initialWidth
                ) / 2f,
            y = (
                canvasSize.height -
                    initialHeight
                ) / 2f
        )
    }

    if (
        overlaySize.width <= 0f ||
        overlaySize.height <= 0f
    ) {
        return
    }

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = overlayOffset.x.roundToInt(),
                    y = overlayOffset.y.roundToInt()
                )
            }
            .size(
                width = with(density) {
                    overlaySize.width.toDp()
                },
                height = with(density) {
                    overlaySize.height.toDp()
                }
            )
            .border(
                width = 1.dp,
                color = Color.White
            )
    ) {
        // Move body or resize corners
        Image(
            bitmap = image,
            contentDescription = "Overlay picture",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(
                    image,
                    canvasSize,
                    density
                ) {
                    var gestureMode =
                        OverlayGestureMode.MOVE

                    var dragStartOffset =
                        Offset.Zero

                    var dragStartSize =
                        Size.Zero

                    var totalDrag =
                        Offset.Zero

                    detectDragGestures(
                        onDragStart = { touchOffset ->
                            dragStartOffset =
                                currentOverlayOffset

                            dragStartSize =
                                currentOverlaySize

                            totalDrag =
                                Offset.Zero

                            val cornerTouchSize =
                                min(
                                    MAX_CORNER_TOUCH_SIZE.toPx(),
                                    min(
                                        dragStartSize.width,
                                        dragStartSize.height
                                    ) * CORNER_TOUCH_RATIO
                                )

                            gestureMode =
                                detectOverlayGestureMode(
                                    touchOffset = touchOffset,
                                    overlaySize = dragStartSize,
                                    cornerTouchSize = cornerTouchSize
                                )
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()

                            totalDrag += dragAmount

                            if (
                                gestureMode ==
                                OverlayGestureMode.MOVE
                            ) {
                                overlayOffset =
                                    calculateMoveOffset(
                                        startOffset = dragStartOffset,
                                        overlaySize = dragStartSize,
                                        canvasSize = canvasSize,
                                        dragAmount = totalDrag
                                    )

                                return@detectDragGestures
                            }

                            val imageAspectRatio =
                                image.width.toFloat() /
                                    image.height.toFloat()

                            val widthDelta =
                                calculateResizeWidthDelta(
                                    gestureMode = gestureMode,
                                    dragAmount = totalDrag,
                                    imageAspectRatio =
                                        imageAspectRatio
                                )

                            val maxWidth =
                                calculateMaxOverlayWidth(
                                    gestureMode = gestureMode,
                                    canvasSize = canvasSize,
                                    overlayOffset =
                                        dragStartOffset,
                                    overlaySize =
                                        dragStartSize,
                                    imageAspectRatio =
                                        imageAspectRatio
                                )

                            val minSidePx =
                                MIN_OVERLAY_SIZE.toPx()

                            val preferredMinWidth =
                                max(
                                    minSidePx,
                                    minSidePx *
                                        imageAspectRatio
                                )

                            val minWidth =
                                min(
                                    preferredMinWidth,
                                    maxWidth
                                )

                            val newWidth = (
                                dragStartSize.width +
                                    widthDelta
                                ).coerceIn(
                                    minimumValue = minWidth,
                                    maximumValue = maxWidth
                                )

                            val newHeight =
                                newWidth /
                                    imageAspectRatio

                            overlayOffset =
                                calculateResizeOffset(
                                    gestureMode = gestureMode,
                                    startOffset =
                                        dragStartOffset,
                                    startSize =
                                        dragStartSize,
                                    newWidth = newWidth,
                                    newHeight = newHeight
                                )

                            overlaySize = Size(
                                width = newWidth,
                                height = newHeight
                            )
                        }
                    )
                }
        )
    }
}
