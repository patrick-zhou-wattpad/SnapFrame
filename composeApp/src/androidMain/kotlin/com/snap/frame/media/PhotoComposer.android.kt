package com.snap.frame.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import java.io.ByteArrayOutputStream
import androidx.core.graphics.createBitmap

class AndroidPhotoComposer : PhotoComposer {

    override fun compose(
        backgroundPhotoBytes: ByteArray,
        overlays: List<OverlayComposition>
    ): ByteArray? = runCatching {
        val background = BitmapFactory.decodeByteArray(
            backgroundPhotoBytes,
            0,
            backgroundPhotoBytes.size
        ) ?: error("Failed to decode background photo")

        try {
            val outputBitmap = createBitmap(background.width, background.height)

            try {
                val canvas = Canvas(outputBitmap)

                val paint = Paint(
                    Paint.ANTI_ALIAS_FLAG or
                        Paint.FILTER_BITMAP_FLAG
                )

                canvas.drawBitmap(
                    background,
                    0f,
                    0f,
                    paint
                )

                overlays.forEach { overlay ->
                    val overlayBitmap = BitmapFactory.decodeByteArray(
                        overlay.photoBytes,
                        0,
                        overlay.photoBytes.size
                    ) ?: return@forEach

                    try {
                        val overlayAspectRatio =
                            overlayBitmap.width.toFloat() /
                                overlayBitmap.height.toFloat()

                        val left =
                            outputBitmap.width * overlay.leftFraction

                        val top =
                            outputBitmap.height * overlay.topFraction

                        val width =
                            outputBitmap.width * overlay.widthFraction

                        val height =
                            width / overlayAspectRatio

                        canvas.drawBitmap(
                            overlayBitmap,
                            null,
                            RectF(
                                left,
                                top,
                                left + width,
                                top + height
                            ),
                            paint
                        )
                    } finally {
                        overlayBitmap.recycle()
                    }
                }

                ByteArrayOutputStream().use { outputStream ->
                    check(
                        outputBitmap.compress(
                            Bitmap.CompressFormat.PNG,
                            100,
                            outputStream
                        )
                    )

                    outputStream.toByteArray()
                }
            } finally {
                outputBitmap.recycle()
            }
        } finally {
            background.recycle()
        }
    }.getOrNull()
}
