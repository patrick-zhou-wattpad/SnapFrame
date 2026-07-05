package com.snap.frame.features.studio.composable

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.min


internal val MIN_OVERLAY_SIZE = 12.dp
internal val MAX_CORNER_TOUCH_SIZE = 20.dp

internal const val CORNER_TOUCH_RATIO = 0.35f


internal enum class OverlayGestureMode {
    MOVE,
    RESIZE_TOP_LEFT,
    RESIZE_TOP_RIGHT,
    RESIZE_BOTTOM_LEFT,
    RESIZE_BOTTOM_RIGHT
}


// Detect move or corner resize
internal fun detectOverlayGestureMode(
    touchOffset: Offset,
    overlaySize: Size,
    cornerTouchSize: Float
): OverlayGestureMode {
    val isLeft =
        touchOffset.x <= cornerTouchSize

    val isRight =
        touchOffset.x >=
            overlaySize.width - cornerTouchSize

    val isTop =
        touchOffset.y <= cornerTouchSize

    val isBottom =
        touchOffset.y >=
            overlaySize.height - cornerTouchSize

    return when {
        isLeft && isTop ->
            OverlayGestureMode.RESIZE_TOP_LEFT

        isRight && isTop ->
            OverlayGestureMode.RESIZE_TOP_RIGHT

        isLeft && isBottom ->
            OverlayGestureMode.RESIZE_BOTTOM_LEFT

        isRight && isBottom ->
            OverlayGestureMode.RESIZE_BOTTOM_RIGHT

        else ->
            OverlayGestureMode.MOVE
    }
}


// Move overlay inside canvas
internal fun calculateMoveOffset(
    startOffset: Offset,
    overlaySize: Size,
    canvasSize: Size,
    dragAmount: Offset
): Offset {
    val maxX = (
        canvasSize.width -
            overlaySize.width
        ).coerceAtLeast(0f)

    val maxY = (
        canvasSize.height -
            overlaySize.height
        ).coerceAtLeast(0f)

    return Offset(
        x = (
            startOffset.x +
                dragAmount.x
            ).coerceIn(
                minimumValue = 0f,
                maximumValue = maxX
            ),
        y = (
            startOffset.y +
                dragAmount.y
            ).coerceIn(
                minimumValue = 0f,
                maximumValue = maxY
            )
    )
}


// Calculate resize amount
internal fun calculateResizeWidthDelta(
    gestureMode: OverlayGestureMode,
    dragAmount: Offset,
    imageAspectRatio: Float
): Float {
    val horizontalDrag =
        abs(dragAmount.x) >=
            abs(dragAmount.y)

    return when (gestureMode) {
        OverlayGestureMode.RESIZE_TOP_LEFT -> {
            if (horizontalDrag) {
                -dragAmount.x
            } else {
                -dragAmount.y *
                    imageAspectRatio
            }
        }

        OverlayGestureMode.RESIZE_TOP_RIGHT -> {
            if (horizontalDrag) {
                dragAmount.x
            } else {
                -dragAmount.y *
                    imageAspectRatio
            }
        }

        OverlayGestureMode.RESIZE_BOTTOM_LEFT -> {
            if (horizontalDrag) {
                -dragAmount.x
            } else {
                dragAmount.y *
                    imageAspectRatio
            }
        }

        OverlayGestureMode.RESIZE_BOTTOM_RIGHT -> {
            if (horizontalDrag) {
                dragAmount.x
            } else {
                dragAmount.y *
                    imageAspectRatio
            }
        }

        OverlayGestureMode.MOVE ->
            0f
    }
}


// Keep resize inside canvas
internal fun calculateMaxOverlayWidth(
    gestureMode: OverlayGestureMode,
    canvasSize: Size,
    overlayOffset: Offset,
    overlaySize: Size,
    imageAspectRatio: Float
): Float {
    return when (gestureMode) {
        OverlayGestureMode.RESIZE_TOP_LEFT -> {
            val right =
                overlayOffset.x +
                    overlaySize.width

            val bottom =
                overlayOffset.y +
                    overlaySize.height

            min(
                right,
                bottom * imageAspectRatio
            )
        }

        OverlayGestureMode.RESIZE_TOP_RIGHT -> {
            val bottom =
                overlayOffset.y +
                    overlaySize.height

            min(
                canvasSize.width -
                    overlayOffset.x,
                bottom * imageAspectRatio
            )
        }

        OverlayGestureMode.RESIZE_BOTTOM_LEFT -> {
            val right =
                overlayOffset.x +
                    overlaySize.width

            min(
                right,
                (
                    canvasSize.height -
                        overlayOffset.y
                    ) * imageAspectRatio
            )
        }

        OverlayGestureMode.RESIZE_BOTTOM_RIGHT -> {
            min(
                canvasSize.width -
                    overlayOffset.x,
                (
                    canvasSize.height -
                        overlayOffset.y
                    ) * imageAspectRatio
            )
        }

        OverlayGestureMode.MOVE ->
            overlaySize.width
    }.coerceAtLeast(1f)
}


// Keep opposite corner fixed
internal fun calculateResizeOffset(
    gestureMode: OverlayGestureMode,
    startOffset: Offset,
    startSize: Size,
    newWidth: Float,
    newHeight: Float
): Offset {
    return when (gestureMode) {
        OverlayGestureMode.RESIZE_TOP_LEFT -> {
            Offset(
                x = startOffset.x +
                    startSize.width -
                    newWidth,
                y = startOffset.y +
                    startSize.height -
                    newHeight
            )
        }

        OverlayGestureMode.RESIZE_TOP_RIGHT -> {
            Offset(
                x = startOffset.x,
                y = startOffset.y +
                    startSize.height -
                    newHeight
            )
        }

        OverlayGestureMode.RESIZE_BOTTOM_LEFT -> {
            Offset(
                x = startOffset.x +
                    startSize.width -
                    newWidth,
                y = startOffset.y
            )
        }

        OverlayGestureMode.RESIZE_BOTTOM_RIGHT,
        OverlayGestureMode.MOVE -> {
            startOffset
        }
    }
}
