import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snap.frame.design.components.CustomSnackbarHost
import com.snap.frame.design.components.IconTextButton
import com.snap.frame.design.components.IconTitleRow
import com.snap.frame.media.PhotoSaver
import com.snap.frame.media.toImageBitmap
import kotlinx.coroutines.launch
import snapframe.composeapp.generated.resources.Res
import snapframe.composeapp.generated.resources.arrow_left
import snapframe.composeapp.generated.resources.download

@Composable
fun StudioScreen(
    photoBytes: ByteArray,
    onBack: () -> Unit,
    photoSaver: PhotoSaver
) {
    val imageBitmap = photoBytes.toImageBitmap()
    val snackbarHostState = remember { SnackbarHostState() }
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
                        onIconClick = { onBack() }
                    )
                }

                IconTextButton(
                    icon = Res.drawable.download,
                    text = "Save",
                    onClick = {
                        photoSaver.savePhotoToAlbum(photoBytes) { success ->
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
                    },
                    iconColor = Color.White,
                    textColor = Color.White
                )
            }

            Spacer(Modifier.height(12.dp))

            // image preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "Cropped photo",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

