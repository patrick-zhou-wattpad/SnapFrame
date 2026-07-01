package com.snap.frame.features

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import snapframe.composeapp.generated.resources.arrow_left
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snap.frame.design.components.IconTitleRow
import com.snap.frame.media.cropToBytes
import com.snap.frame.media.toImageBitmap
import com.snap.frame.platformGestureExclusion
import org.jetbrains.compose.resources.painterResource
import snapframe.composeapp.generated.resources.Res
import snapframe.composeapp.generated.resources.p1

enum class DragMode {
    None,
    Move,
    Left, Right, Top, Bottom,
    TopLeft, TopRight, BottomLeft, BottomRight
}

@Composable
fun CropPhotoScreen(
    photoBytes: ByteArray,
    onBack: () -> Unit,
    onNext: (ByteArray) -> Unit
) {
    var imageScale by remember { mutableFloatStateOf(1f) }

    val imageBitmap = remember(photoBytes) { photoBytes.toImageBitmap() }
    val imageAspect = imageBitmap.width.toFloat() / imageBitmap.height.toFloat()

    var viewportSize by remember { mutableStateOf(Size.Zero) }
    var cropTopLeft by remember { mutableStateOf(Offset.Zero) }
    var cropSize by remember { mutableStateOf(Size.Zero) }
    var mode by remember { mutableStateOf(DragMode.None) }

    val minCrop = 80f
    val moveSpeed = 1.0f
    val resizeSpeed = 2.4f

    // axis-only radius
    val edgeHitDp: Dp = 18.dp
    // diagonal radius
    val cornerHitDp: Dp = 26.dp

    val edgeHit = with(LocalDensity.current) { edgeHitDp.toPx() }
    val cornerHit = with(LocalDensity.current) { cornerHitDp.toPx() }

    val maskColor = Color.Black.copy(alpha = 0.55f)
    val frameColor = Color.White.copy(alpha = 0.35f)
    val handleColor = Color.White


    fun clamp(v: Float, lo: Float, hi: Float): Float =
        when {
            v < lo -> lo
            v > hi -> hi
            else -> v
        }

    fun absf(x: Float): Float = if (x < 0f) -x else x

    // same math as resetCropToDisplayedImage, but returns the displayed rect
    fun computeDisplayedRect(vw: Float, vh: Float): Pair<Offset, Size> {
        val viewAspect = vw / vh
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


        val tl = Offset((vw - displayW) / 2f, (vh - displayH) / 2f)
        val sz = Size(displayW, displayH)
        return tl to sz
    }

    fun isFullImageSelection(): Boolean {
        if (viewportSize == Size.Zero) return false

        // IMPORTANT: your default selection is computed at scale=1 display rect.
        // So only bypass when zoom is effectively 1.0.
        if (absf(imageScale - 1f) > 0.001f) return false

        val (expTL, expSize) = computeDisplayedRect(viewportSize.width, viewportSize.height)

        // allow tiny float error
        val eps = 1.0f

        return absf(cropTopLeft.x - expTL.x) <= eps &&
            absf(cropTopLeft.y - expTL.y) <= eps &&
            absf(cropSize.width - expSize.width) <= eps &&
            absf(cropSize.height - expSize.height) <= eps
    }

    fun resetCropToDisplayedImage(vw: Float, vh: Float) {
        viewportSize = Size(vw, vh)

        val (tl, sz) = computeDisplayedRect(vw, vh)
        cropTopLeft = tl
        cropSize = sz
    }


    fun ensureViewportAndCrop(vw: Float, vh: Float) {
        // Re-init on first time, and re-init if orientation/size changes
        if (viewportSize == Size.Zero) {
            resetCropToDisplayedImage(vw, vh)
            return
        }

        // If size changed (rotation), reset crop to match new displayed image
        if (viewportSize.width != vw || viewportSize.height != vh) {
            resetCropToDisplayedImage(vw, vh)
        }
    }


    fun clampCrop(proposedTL: Offset, proposedSize: Size) {
        if (viewportSize == Size.Zero) return


        val w = clamp(proposedSize.width, minCrop, viewportSize.width)
        val h = clamp(proposedSize.height, minCrop, viewportSize.height)


        val maxX = viewportSize.width - w
        val maxY = viewportSize.height - h


        cropTopLeft = Offset(
            clamp(proposedTL.x, 0f, maxX),
            clamp(proposedTL.y, 0f, maxY)
        )
        cropSize = Size(w, h)
    }

    fun tl() = cropTopLeft
    fun br() = Offset(cropTopLeft.x + cropSize.width, cropTopLeft.y + cropSize.height)
    fun tr() = Offset(br().x, tl().y)
    fun bl() = Offset(tl().x, br().y)

    fun ct() = Offset(cropTopLeft.x + cropSize.width / 2f, cropTopLeft.y)
    fun cb() = Offset(cropTopLeft.x + cropSize.width / 2f, cropTopLeft.y + cropSize.height)
    fun cl() = Offset(cropTopLeft.x, cropTopLeft.y + cropSize.height / 2f)
    fun cr() = Offset(cropTopLeft.x + cropSize.width, cropTopLeft.y + cropSize.height / 2f)


    fun dist2(a: Offset, b: Offset): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return dx * dx + dy * dy
    }

    fun isInsideRect(p: Offset, tl: Offset, br: Offset): Boolean {
        return (p.x >= tl.x && p.x <= br.x && p.y >= tl.y && p.y <= br.y)
    }

    fun pickMode(down: Offset) {
        val _tl = tl()
        val _br = br()
        val inside = isInsideRect(down, _tl, _br)

        val cornerR2 = cornerHit * cornerHit
        val edgeR2 = edgeHit * edgeHit

        mode = when {
            dist2(down, tl()) <= cornerR2 -> DragMode.TopLeft
            dist2(down, tr()) <= cornerR2 -> DragMode.TopRight
            dist2(down, bl()) <= cornerR2 -> DragMode.BottomLeft
            dist2(down, br()) <= cornerR2 -> DragMode.BottomRight

            dist2(down, cl()) <= edgeR2 -> DragMode.Left
            dist2(down, cr()) <= edgeR2 -> DragMode.Right
            dist2(down, ct()) <= edgeR2 -> DragMode.Top
            dist2(down, cb()) <= edgeR2 -> DragMode.Bottom


            inside -> DragMode.Move
            else -> DragMode.None
        }
    }

    fun applyMove(d: Offset) {
        clampCrop(
            cropTopLeft + Offset(d.x * moveSpeed, d.y * moveSpeed),
            cropSize
        )
    }

    fun resizeLeft(dx: Float) {
        val right = cropTopLeft.x + cropSize.width
        var newLeft = cropTopLeft.x + dx * resizeSpeed
        newLeft = clamp(newLeft, 0f, right - minCrop)
        val newW = right - newLeft
        clampCrop(Offset(newLeft, cropTopLeft.y), Size(newW, cropSize.height))
    }

    fun resizeRight(dx: Float) {
        val left = cropTopLeft.x
        var newRight = left + cropSize.width + dx * resizeSpeed
        newRight = clamp(newRight, left + minCrop, viewportSize.width)
        val newW = newRight - left
        clampCrop(cropTopLeft, Size(newW, cropSize.height))
    }

    fun resizeTop(dy: Float) {
        val bottom = cropTopLeft.y + cropSize.height
        var newTop = cropTopLeft.y + dy * resizeSpeed
        newTop = clamp(newTop, 0f, bottom - minCrop)
        val newH = bottom - newTop
        clampCrop(Offset(cropTopLeft.x, newTop), Size(cropSize.width, newH))
    }

    fun resizeBottom(dy: Float) {
        val top = cropTopLeft.y
        var newBottom = top + cropSize.height + dy * resizeSpeed
        newBottom = clamp(newBottom, top + minCrop, viewportSize.height)
        val newH = newBottom - top
        clampCrop(cropTopLeft, Size(cropSize.width, newH))
    }


    fun applyDrag(d: Offset) {
        if (viewportSize == Size.Zero) return


        when (mode) {
            DragMode.None -> Unit
            DragMode.Move -> applyMove(d)


            DragMode.Left -> resizeLeft(d.x)
            DragMode.Right -> resizeRight(d.x)
            DragMode.Top -> resizeTop(d.y)
            DragMode.Bottom -> resizeBottom(d.y)


            DragMode.TopLeft -> { resizeLeft(d.x); resizeTop(d.y) }
            DragMode.TopRight -> { resizeRight(d.x); resizeTop(d.y) }
            DragMode.BottomLeft -> { resizeLeft(d.x); resizeBottom(d.y) }
            DragMode.BottomRight -> { resizeRight(d.x); resizeBottom(d.y) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(top = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconTitleRow(
                icon = Res.drawable.arrow_left,
                title = "Crop",
                onIconClick = { onBack() }
            )

            Spacer(Modifier.weight(1f))

            Text(
                "Next",
                color = Color.White,
                modifier = Modifier.clickableNoRipple {
                    // If user didn’t change selection (full image), return original bytes.
                    if (isFullImageSelection()) {
                        onNext(photoBytes)
                    } else {
                        val croppedBytes = cropToBytes(
                            source = imageBitmap,
                            cropTopLeft = cropTopLeft,
                            cropSize = cropSize,
                            viewportSize = viewportSize,
                            imageScale = imageScale
                        )
                        onNext(croppedBytes)
                    }
                }
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // support both landscape and portrait
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.DarkGray)
        ) {
            val vw = constraints.maxWidth.toFloat()
            val vh = constraints.maxHeight.toFloat()
            ensureViewportAndCrop(vw, vh)


            Image(
                bitmap = imageBitmap,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = imageScale
                        scaleY = imageScale
                    }
            )


            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            ) {
                drawRect(color = maskColor)


                drawRect(
                    color = Color.Transparent,
                    topLeft = cropTopLeft,
                    size = cropSize,
                    blendMode = BlendMode.Clear
                )

                drawRect(
                    color = frameColor,
                    topLeft = cropTopLeft,
                    size = cropSize,
                    style = Stroke(2.dp.toPx())
                )

                val r = 9.dp.toPx()
                drawCircle(handleColor, r, tl())
                drawCircle(handleColor, r, tr())
                drawCircle(handleColor, r, bl())
                drawCircle(handleColor, r, br())
                drawCircle(handleColor, r, ct())
                drawCircle(handleColor, r, cb())
                drawCircle(handleColor, r, cl())
                drawCircle(handleColor, r, cr())
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { pickMode(it) },
                            onDragEnd = { mode = DragMode.None },
                            onDragCancel = { mode = DragMode.None },
                            onDrag = { change, drag ->
                                change.consume()
                                applyDrag(drag)
                            }
                        )
                    }
            )
        }


        Spacer(Modifier.height(18.dp))


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Zoom", color = Color.White, fontWeight = FontWeight.Medium)
                Spacer(Modifier.weight(1f))
                Text("${(imageScale * 100).toInt()}%", color = Color.White.copy(alpha = 0.8f))
            }


            Spacer(Modifier.height(10.dp))


            CustomSlider(
                value = imageScale,
                onValueChange = { imageScale = it },
                minValue = 0.5f,
                maxValue = 3.0f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(35.dp)
                    .platformGestureExclusion() // touch input in the area handle by the app
            )
        }
    }
}


@Composable
private fun CustomSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    minValue: Float,
    maxValue: Float,
    modifier: Modifier = Modifier
) {
    fun clamp(v: Float, lo: Float, hi: Float): Float =
        when {
            v < lo -> lo
            v > hi -> hi
            else -> v
        }


    BoxWithConstraints(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { down ->
                        val w = size.width.toFloat()
                        val t = clamp(down.x / w, 0f, 1f)
                        onValueChange(minValue + (maxValue - minValue) * t)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val w = size.width.toFloat()
                        val t = clamp(change.position.x / w, 0f, 1f)
                        onValueChange(minValue + (maxValue - minValue) * t)
                    }
                )
            }
    ) {
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()

        val tRaw = if (maxValue == minValue) 0f else (value - minValue) / (maxValue - minValue)
        val t = clamp(tRaw, 0f, 1f)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val trackY = h / 2f
            val trackH = 6.dp.toPx()
            val thumbR = 10.dp.toPx()

            drawRoundRect(
                color = Color.White.copy(alpha = 0.25f),
                topLeft = Offset(0f, trackY - trackH / 2f),
                size = Size(w, trackH),
                cornerRadius = CornerRadius(trackH, trackH)
            )

            drawRoundRect(
                color = Color.White.copy(alpha = 0.65f),
                topLeft = Offset(0f, trackY - trackH / 2f),
                size = Size(w * t, trackH),
                cornerRadius = CornerRadius(trackH, trackH)
            )

            drawCircle(
                color = Color.White,
                radius = thumbR,
                center = Offset(w * t, trackY)
            )
        }
    }
}

@Composable
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() }
    ) {
        onClick()
    }
