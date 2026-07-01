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


    // Screen -> bitmap scaling
    val scaleX = srcW.toFloat() / viewportSize.width
    val scaleY = srcH.toFloat() / viewportSize.height


    var left = (cropTopLeft.x * scaleX / imageScale).toInt()
    var top = (cropTopLeft.y * scaleY / imageScale).toInt()
    var width = (cropSize.width * scaleX / imageScale).toInt()
    var height = (cropSize.height * scaleY / imageScale).toInt()


    // ---- manual clamp (no coerceIn / no math / no until) ----
    if (left < 0) left = 0
    if (top < 0) top = 0


    if (srcW <= 0) return ByteArray(0)
    if (srcH <= 0) return ByteArray(0)


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

    // Convert ImageBitmap -> ByteArray (little-endian: B,G,R,A)
    val bytes = ByteArray(srcW * srcH * 4)
    var i = 0
    var y = 0
    while (y < srcH) {
        var x = 0
        while (x < srcW) {
            val argb = pixelMap[x, y].toArgb() // 0xAARRGGBB

            bytes[i] = (argb and 0xFF).toByte()                // B
            bytes[i + 1] = ((argb ushr 8) and 0xFF).toByte()   // G
            bytes[i + 2] = ((argb ushr 16) and 0xFF).toByte()  // R
            bytes[i + 3] = ((argb ushr 24) and 0xFF).toByte()  // A

            i += 4
            x += 1
        }
        y += 1
    }

    val info = ImageInfo.makeN32Premul(srcW, srcH)
    val rowBytes = srcW * 4
    val srcImage = Image.makeRaster(info, bytes, rowBytes)


    // Create output surface and draw cropped region via drawImageRect (NO subset)
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
