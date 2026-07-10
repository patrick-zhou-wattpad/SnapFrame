package com.snap.frame.features

import androidx.collection.objectListOf
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.snap.frame.media.PhotoPicker
import com.snap.frame.media.rememberPhotoPicker
import org.jetbrains.compose.resources.painterResource
import snapframe.composeapp.generated.resources.Res
import snapframe.composeapp.generated.resources.compose_multiplatform
import snapframe.composeapp.generated.resources.p1
import snapframe.composeapp.generated.resources.p2

@Composable
fun HomeScreen(
    onPhotoPicked: (ByteArray) -> Unit
) {
    val picker: PhotoPicker = rememberPhotoPicker()

    var selectedBytes by remember { mutableStateOf<ByteArray?>(null) }
    var statusText by remember { mutableStateOf("No image selected") }

    val carouselImages = objectListOf(
        Res.drawable.p1,
        Res.drawable.p2,
        Res.drawable.compose_multiplatform
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeContentPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Carousel
        AutoCarousel(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            itemCount = carouselImages.size,
            autoScrollIntervalMs = 2_000L,
        ) { page ->
            Image(
                painter = painterResource(carouselImages[page]),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(18.dp)
                )
                .clickable {
                    picker.pick { bytes ->
                        selectedBytes = bytes
                        statusText =
                            if (bytes != null) "Selected: ${bytes.size} bytes"
                            else "Canceled"

                        // Navigate only when success
                        if (bytes != null) {
                            onPhotoPicked(bytes)
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+ background picture",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = statusText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
