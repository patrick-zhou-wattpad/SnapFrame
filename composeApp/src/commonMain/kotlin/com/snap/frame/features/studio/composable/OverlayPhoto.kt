package com.snap.frame.features.studio.composable

import androidx.compose.ui.graphics.ImageBitmap
import kotlin.math.min

internal data class OverlayTransform(
    val leftFraction: Float,
    val topFraction: Float,
    val widthFraction: Float
)

internal data class OverlayPhoto(
    val id: Long,
    val photoBytes: ByteArray,
    val image: ImageBitmap,
    val transform: OverlayTransform
)

// Creates a centered overlay that fits within 40% (initial value) of the canvas
internal fun createInitialOverlayTransform(
    backgroundAspectRatio: Float,
    overlayImage: ImageBitmap
): OverlayTransform {
    // Get the overlay image shape
    val overlayAspectRatio =
        overlayImage.width.toFloat() / overlayImage.height.toFloat()

    // Fit the overlay within 40% of the canvas while keeping its aspect ratio
    val widthFraction = min(
        0.4f,
        0.4f * overlayAspectRatio / backgroundAspectRatio
    )

    // Calculate height from the final width to keep the image ratio
    val heightFraction =
        widthFraction * backgroundAspectRatio / overlayAspectRatio

    // Center the overlay on the canvas
    return OverlayTransform(
        leftFraction = (1f - widthFraction) / 2f,
        topFraction = (1f - heightFraction) / 2f,
        widthFraction = widthFraction
    )
}
