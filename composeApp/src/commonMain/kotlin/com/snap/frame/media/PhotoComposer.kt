package com.snap.frame.media

// Stores overlay image data and its position/size relative to the background
data class OverlayComposition(
    val photoBytes: ByteArray,
    val leftFraction: Float,
    val topFraction: Float,
    val widthFraction: Float
)

// Keeps image composition platform-specific while sharing the same editor logic
interface PhotoComposer {

    fun compose(
        backgroundPhotoBytes: ByteArray,
        overlays: List<OverlayComposition>
    ): ByteArray?
}
