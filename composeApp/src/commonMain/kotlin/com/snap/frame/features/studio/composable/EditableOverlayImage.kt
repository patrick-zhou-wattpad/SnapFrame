package com.snap.frame.features.studio.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
    overlay: OverlayPhoto,
    canvasSize: Size,
    onTransformChange: (OverlayTransform) -> Unit
) {
    val density = LocalDensity.current

    val currentTransform by rememberUpdatedState(overlay.transform)

    val imageAspectRatio =
        overlay.image.width.toFloat() / overlay.image.height.toFloat()

    val overlayWidth =
        canvasSize.width * overlay.transform.widthFraction

    val overlayHeight =
        overlayWidth / imageAspectRatio

    val overlayOffset = Offset(
        x = canvasSize.width * overlay.transform.leftFraction,
        y = canvasSize.height * overlay.transform.topFraction
    )

    if (overlayWidth <= 0f || overlayHeight <= 0f) {
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
                width = with(density) { overlayWidth.toDp() },
                height = with(density) { overlayHeight.toDp() }
            )
            .border(
                width = 1.dp,
                color = Color.White
            )
    ) {
        Image(
            bitmap = overlay.image,
            contentDescription = "Overlay picture",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(
                    overlay.id,
                    canvasSize,
                    density
                ) {
                    var gestureMode = OverlayGestureMode.MOVE
                    var dragStartTransform = currentTransform
                    var dragStartOffset = Offset.Zero
                    var dragStartSize = Size.Zero
                    var totalDrag = Offset.Zero

                    detectDragGestures(
                        onDragStart = { touchOffset ->
                            dragStartTransform = currentTransform

                            val startWidth =
                                canvasSize.width * dragStartTransform.widthFraction

                            val startHeight =
                                startWidth / imageAspectRatio

                            dragStartSize = Size(
                                width = startWidth,
                                height = startHeight
                            )

                            dragStartOffset = Offset(
                                x = canvasSize.width *
                                    dragStartTransform.leftFraction,
                                y = canvasSize.height *
                                    dragStartTransform.topFraction
                            )

                            totalDrag = Offset.Zero

                            val cornerTouchSize = min(
                                MAX_CORNER_TOUCH_SIZE.toPx(),
                                min(
                                    dragStartSize.width,
                                    dragStartSize.height
                                ) * CORNER_TOUCH_RATIO
                            )

                            gestureMode = detectOverlayGestureMode(
                                touchOffset = touchOffset,
                                overlaySize = dragStartSize,
                                cornerTouchSize = cornerTouchSize
                            )
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()

                            totalDrag += dragAmount

                            if (gestureMode == OverlayGestureMode.MOVE) {
                                val newOffset = calculateMoveOffset(
                                    startOffset = dragStartOffset,
                                    overlaySize = dragStartSize,
                                    canvasSize = canvasSize,
                                    dragAmount = totalDrag
                                )

                                onTransformChange(
                                    dragStartTransform.copy(
                                        leftFraction =
                                            newOffset.x / canvasSize.width,
                                        topFraction =
                                            newOffset.y / canvasSize.height
                                    )
                                )

                                return@detectDragGestures
                            }

                            val widthDelta = calculateResizeWidthDelta(
                                gestureMode = gestureMode,
                                dragAmount = totalDrag,
                                imageAspectRatio = imageAspectRatio
                            )

                            val maxWidth = calculateMaxOverlayWidth(
                                gestureMode = gestureMode,
                                canvasSize = canvasSize,
                                overlayOffset = dragStartOffset,
                                overlaySize = dragStartSize,
                                imageAspectRatio = imageAspectRatio
                            )

                            val minSidePx = MIN_OVERLAY_SIZE.toPx()

                            val preferredMinWidth = max(
                                minSidePx,
                                minSidePx * imageAspectRatio
                            )

                            val minWidth = min(
                                preferredMinWidth,
                                maxWidth
                            )

                            val newWidth =
                                (dragStartSize.width + widthDelta).coerceIn(
                                    minimumValue = minWidth,
                                    maximumValue = maxWidth
                                )

                            val newHeight =
                                newWidth / imageAspectRatio

                            val newOffset = calculateResizeOffset(
                                gestureMode = gestureMode,
                                startOffset = dragStartOffset,
                                startSize = dragStartSize,
                                newWidth = newWidth,
                                newHeight = newHeight
                            )

                            onTransformChange(
                                OverlayTransform(
                                    leftFraction =
                                        newOffset.x / canvasSize.width,
                                    topFraction =
                                        newOffset.y / canvasSize.height,
                                    widthFraction =
                                        newWidth / canvasSize.width
                                )
                            )
                        }
                    )
                }
        )
    }
}
