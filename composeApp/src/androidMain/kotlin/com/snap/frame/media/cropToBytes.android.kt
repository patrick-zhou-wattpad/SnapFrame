package com.snap.frame.media

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import java.io.ByteArrayOutputStream

actual fun cropToBytes(
    source: ImageBitmap,
    cropTopLeft: Offset,
    cropSize: Size,
    viewportSize: Size,
    imageScale: Float
): ByteArray {
    val bmp: Bitmap = source.asAndroidBitmap()

    if (viewportSize.width <= 0f || viewportSize.height <= 0f) {
        return ByteArray(0)
    }

    val vw = viewportSize.width
    val vh = viewportSize.height

    val srcW = bmp.width.toFloat()
    val srcH = bmp.height.toFloat()

    val imageAspect = srcW / srcH
    val viewAspect = vw / vh

    // 1) Fit-center displayed size at scale = 1
    val displayW: Float
    val displayH: Float
    if (imageAspect > viewAspect) {
        // fit width
        displayW = vw
        displayH = vw / imageAspect
    } else {
        // fit height
        displayH = vh
        displayW = vh * imageAspect
    }

    // 2) Apply zoom scale around viewport center (matches graphicsLayer default)
    val scaledW = displayW * imageScale
    val scaledH = displayH * imageScale

    val cx = vw / 2f
    val cy = vh / 2f

    val imageTLx = cx - scaledW / 2f
    val imageTLy = cy - scaledH / 2f

    // 3) Convert crop rect from viewport coords -> normalized coords in the SCALED displayed image rect
    val nx0 = (cropTopLeft.x - imageTLx) / scaledW
    val ny0 = (cropTopLeft.y - imageTLy) / scaledH
    val nx1 = (cropTopLeft.x + cropSize.width - imageTLx) / scaledW
    val ny1 = (cropTopLeft.y + cropSize.height - imageTLy) / scaledH

    // 4) Manual clamp to [0,1]
    var x0n = nx0
    var y0n = ny0
    var x1n = nx1
    var y1n = ny1

    if (x0n < 0f) x0n = 0f
    if (y0n < 0f) y0n = 0f
    if (x1n < 0f) x1n = 0f
    if (y1n < 0f) y1n = 0f

    if (x0n > 1f) x0n = 1f
    if (y0n > 1f) y0n = 1f
    if (x1n > 1f) x1n = 1f
    if (y1n > 1f) y1n = 1f

    // Ensure correct ordering (in case of weird drag states)
    if (x1n < x0n) {
        val t = x0n; x0n = x1n; x1n = t
    }
    if (y1n < y0n) {
        val t = y0n; y0n = y1n; y1n = t
    }

    // 5) Normalized -> bitmap pixel coords
    var left = (x0n * srcW).toInt()
    var top = (y0n * srcH).toInt()
    var right = (x1n * srcW).toInt()
    var bottom = (y1n * srcH).toInt()

    // Convert to width/height
    var width = right - left
    var height = bottom - top

    // manual clamp
    if (left < 0) left = 0
    if (top < 0) top = 0

    if (left >= bmp.width) left = bmp.width - 1
    if (top >= bmp.height) top = bmp.height - 1

    if (width < 1) width = 1
    if (height < 1) height = 1

    if (left + width > bmp.width) {
        width = bmp.width - left
        if (width < 1) width = 1
    }


    if (top + height > bmp.height) {
        height = bmp.height - top
        if (height < 1) height = 1
    }

    val cropped = Bitmap.createBitmap(bmp, left, top, width, height)

    val out = ByteArrayOutputStream()
    cropped.compress(Bitmap.CompressFormat.PNG, 100, out)
    return out.toByteArray()
}
