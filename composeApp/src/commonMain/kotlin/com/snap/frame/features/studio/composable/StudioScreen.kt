package com.snap.frame.features.studio.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snap.frame.design.components.CustomSnackbarHost
import com.snap.frame.design.components.IconTextButton
import com.snap.frame.design.components.IconTitleRow
import com.snap.frame.media.OverlayComposition
import com.snap.frame.media.PhotoComposer
import com.snap.frame.media.PhotoSaver
import com.snap.frame.media.rememberPhotoPicker
import com.snap.frame.media.toImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import snapframe.composeapp.generated.resources.Res
import snapframe.composeapp.generated.resources.arrow_left
import snapframe.composeapp.generated.resources.download

@Composable
fun StudioScreen(
    photoBytes: ByteArray,
    onBack: () -> Unit,
    photoSaver: PhotoSaver,
    photoComposer: PhotoComposer
) {
    val imageBitmap = remember(photoBytes) {
        photoBytes.toImageBitmap()
    }

    val backgroundAspectRatio =
        imageBitmap.width.toFloat() / imageBitmap.height.toFloat()

    val photoPicker = rememberPhotoPicker()

    val overlays = remember {
        mutableStateListOf<OverlayPhoto>()
    }

    var nextOverlayId by remember {
        mutableStateOf(0L)
    }

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = {
            CustomSnackbarHost(
                snackbarHostState = snackbarHostState,
                textSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(innerPadding)
                .padding(24.dp)
                .padding(top = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    IconTitleRow(
                        icon = Res.drawable.arrow_left,
                        title = "Studio",
                        onIconClick = onBack
                    )
                }

                IconTextButton(
                    icon = Res.drawable.download,
                    text = "Save",
                    onClick = {
                        val overlaySnapshot = overlays.map { overlay ->
                            OverlayComposition(
                                photoBytes = overlay.photoBytes,
                                leftFraction =
                                    overlay.transform.leftFraction,
                                topFraction =
                                    overlay.transform.topFraction,
                                widthFraction =
                                    overlay.transform.widthFraction
                            )
                        }

                        scope.launch {
                            val composedPhotoBytes = withContext(
                                Dispatchers.Default
                            ) {
                                photoComposer.compose(
                                    backgroundPhotoBytes = photoBytes,
                                    overlays = overlaySnapshot
                                )
                            }

                            if (composedPhotoBytes == null) {
                                snackbarHostState.showSnackbar(
                                    message = "Failed to save photo"
                                )
                                return@launch
                            }

                            photoSaver.savePhotoToAlbum(
                                composedPhotoBytes
                            ) { success ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = if (success) {
                                            "Photo saved successfully"
                                        } else {
                                            "Failed to save photo"
                                        }
                                    )
                                }
                            }
                        }
                    },
                    iconColor = Color.White,
                    textColor = Color.White
                )
            }

            Spacer(Modifier.height(12.dp))

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                val availableAspectRatio =
                    maxWidth.value / maxHeight.value

                val canvasWidth =
                    if (availableAspectRatio > backgroundAspectRatio) {
                        maxHeight * backgroundAspectRatio
                    } else {
                        maxWidth
                    }

                val canvasHeight =
                    if (availableAspectRatio > backgroundAspectRatio) {
                        maxHeight
                    } else {
                        maxWidth / backgroundAspectRatio
                    }

                PhotoEditorCanvas(
                    backgroundImage = imageBitmap,
                    overlays = overlays,
                    onOverlayTransformChange = { id, transform ->
                        val index = overlays.indexOfFirst {
                            it.id == id
                        }

                        if (index >= 0) {
                            overlays[index] = overlays[index].copy(
                                transform = transform
                            )
                        }
                    },
                    modifier = Modifier.size(
                        width = canvasWidth,
                        height = canvasHeight
                    )
                )
            }

            Spacer(Modifier.height(16.dp))

            ChooseOverlayPhotoButton(
                onClick = {
                    photoPicker.pick { bytes ->
                        if (bytes != null) {
                            val overlayImage = bytes.toImageBitmap()
                            val initialTransform = createInitialOverlayTransform(
                                backgroundAspectRatio = backgroundAspectRatio,
                                overlayImage = overlayImage
                            )

                            overlays += OverlayPhoto(
                                id = nextOverlayId++,
                                photoBytes = bytes,
                                image = overlayImage,
                                transform = initialTransform
                            )
                        }
                    }
                }
            )
        }
    }
}

// Overlay photo picker button
@Composable
private fun ChooseOverlayPhotoButton(
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.size(52.dp),
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            )
        ) {
            Text(
                text = "+",
                fontSize = 28.sp
            )
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Choose artwork or picture",
            color = Color.White,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}
