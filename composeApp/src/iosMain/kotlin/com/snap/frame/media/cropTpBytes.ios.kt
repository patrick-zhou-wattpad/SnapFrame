package com.snap.frame.media

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface

actual fun cropToBytes(
    source: ImageBitmap,
    cropTopLeft: Offset,
    cropSize: Size,
    viewportSize: Size,
    imageScale: Float
): ByteArray {

    val pixelMap = source.toPixelMap()
    val srcW = pixelMap.width
    val srcH = pixelMap.height

    if (srcW <= 0 || srcH <= 0) return ByteArray(0)
    if (viewportSize.width <= 0f || viewportSize.height <= 0f) return ByteArray(0)

    val vw = viewportSize.width
    val vh = viewportSize.height

    val srcWF = srcW.toFloat()
    val srcHF = srcH.toFloat()

    val imageAspect = srcWF / srcHF
    val viewAspect = vw / vh

    // 1) Fit-center displayed size at scale = 1
    val displayW: Float
    val displayH: Float
    if (imageAspect > viewAspect) {
        displayW = vw
        displayH = vw / imageAspect
    } else {
        displayH = vh
        displayW = vh * imageAspect
    }

    // 2) Apply zoom scale around viewport center
    val scaledW = displayW * imageScale
    val scaledH = displayH * imageScale


    val cx = vw / 2f
    val cy = vh / 2f

    val imageTLx = cx - scaledW / 2f
    val imageTLy = cy - scaledH / 2f

    // 3) Convert crop rect (viewport coords) → normalized coords in scaled image
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

    // Ensure ordering
    if (x1n < x0n) {
        val t = x0n; x0n = x1n; x1n = t
    }
    if (y1n < y0n) {
        val t = y0n; y0n = y1n; y1n = t
    }

    // 5) Normalized → source pixel coords
    var left = (x0n * srcWF).toInt()
    var top = (y0n * srcHF).toInt()
    var right = (x1n * srcWF).toInt()
    var bottom = (y1n * srcHF).toInt()

    var width = right - left
    var height = bottom - top

    // manual clamp
    if (left < 0) left = 0
    if (top < 0) top = 0

    if (left >= srcW) left = srcW - 1
    if (top >= srcH) top = srcH - 1


    if (width < 1) width = 1
    if (height < 1) height = 1

    if (left + width > srcW) {
        width = srcW - left
        if (width < 1) width = 1
    }

    if (top + height > srcH) {
        height = srcH - top
        if (height < 1) height = 1
    }

    val bytes = ByteArray(srcW * srcH * 4)
    var i = 0
    var y = 0
    while (y < srcH) {
        var x = 0
        while (x < srcW) {
            val argb = pixelMap[x, y].toArgb()


            bytes[i]     = (argb and 0xFF).toByte()              // B
            bytes[i + 1] = ((argb ushr 8) and 0xFF).toByte()     // G
            bytes[i + 2] = ((argb ushr 16) and 0xFF).toByte()    // R
            bytes[i + 3] = ((argb ushr 24) and 0xFF).toByte()    // A


            i += 4
            x += 1
        }
        y += 1
    }

    val info = ImageInfo.makeN32Premul(srcW, srcH)
    val rowBytes = srcW * 4
    val srcImage = Image.makeRaster(info, bytes, rowBytes)

    val outSurface = Surface.makeRasterN32Premul(width, height)

    val srcRect = Rect.makeXYWH(
        left.toFloat(),
        top.toFloat(),
        width.toFloat(),
        height.toFloat()
    )
    val dstRect = Rect.makeXYWH(
        0f,
        0f,
        width.toFloat(),
        height.toFloat()
    )

    outSurface.canvas.drawImageRect(srcImage, srcRect, dstRect)

    return outSurface.makeImageSnapshot().encodeToData()!!.bytes
}
