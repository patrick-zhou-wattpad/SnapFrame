package com.snap.frame.media

import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface

class IOSPhotoComposer : PhotoComposer {

    override fun compose(
        backgroundPhotoBytes: ByteArray,
        overlays: List<OverlayComposition>
    ): ByteArray? = runCatching {
        val background = Image.makeFromEncoded(
            backgroundPhotoBytes
        )

        try {
            val surface = Surface.makeRasterN32Premul(
                width = background.width,
                height = background.height
            )

            try {
                val canvas = surface.canvas

                canvas.drawImageRect(
                    background,
                    Rect(
                        left = 0f,
                        top = 0f,
                        right = background.width.toFloat(),
                        bottom = background.height.toFloat()
                    )
                )

                overlays.forEach { overlay ->
                    val overlayImage = Image.makeFromEncoded(
                        overlay.photoBytes
                    )

                    try {
                        val overlayAspectRatio =
                            overlayImage.width.toFloat() /
                                overlayImage.height.toFloat()

                        val left =
                            background.width * overlay.leftFraction

                        val top =
                            background.height * overlay.topFraction

                        val width =
                            background.width * overlay.widthFraction

                        val height =
                            width / overlayAspectRatio

                        canvas.drawImageRect(
                            overlayImage,
                            Rect(
                                left = left,
                                top = top,
                                right = left + width,
                                bottom = top + height
                            )
                        )
                    } finally {
                        overlayImage.close()
                    }
                }

                val snapshot = surface.makeImageSnapshot()

                try {
                    val encodedData = snapshot.encodeToData(
                        EncodedImageFormat.PNG
                    ) ?: error("Failed to encode composed photo")

                    try {
                        encodedData.bytes
                    } finally {
                        encodedData.close()
                    }
                } finally {
                    snapshot.close()
                }
            } finally {
                surface.close()
            }
        } finally {
            background.close()
        }
    }.getOrNull()
}
