import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.snap.frame.features.CropPhotoScreen
import com.snap.frame.features.HomeScreen
import com.snap.frame.features.studio.composable.StudioScreen
import com.snap.frame.media.IOSPhotoComposer
import com.snap.frame.media.IOSPhotoSaver
import com.snap.frame.media.b64Decode
import com.snap.frame.media.b64Encode

private enum class Route {
    HOME,
    CROP,
    STUDIO
}

@Composable
actual fun App() {
    var route by remember {
        mutableStateOf(Route.HOME)
    }

    val photoSaver = remember {
        IOSPhotoSaver()
    }

    val photoComposer = remember {
        IOSPhotoComposer()
    }

    var originalBytesB64 by remember {
        mutableStateOf<String?>(null)
    }

    var croppedBytesB64 by remember {
        mutableStateOf<String?>(null)
    }

    fun encode(bytes: ByteArray): String = b64Encode(bytes)

    fun decode(b64: String): ByteArray = b64Decode(b64)

    when (route) {
        Route.HOME -> {
            HomeScreen(
                onPhotoPicked = { bytes ->
                    originalBytesB64 = encode(bytes)
                    croppedBytesB64 = null
                    route = Route.CROP
                }
            )
        }

        Route.CROP -> {
            val b64 = originalBytesB64

            if (b64 == null) {
                route = Route.HOME
            } else {
                CropPhotoScreen(
                    photoBytes = decode(b64),
                    onBack = {
                        route = Route.HOME
                    },
                    onNext = { cropped ->
                        croppedBytesB64 = encode(cropped)
                        route = Route.STUDIO
                    }
                )
            }
        }

        Route.STUDIO -> {
            val b64 = croppedBytesB64

            if (b64 == null) {
                route = Route.HOME
            } else {
                StudioScreen(
                    photoBytes = decode(b64),
                    photoSaver = photoSaver,
                    photoComposer = photoComposer,
                    onBack = {
                        route = Route.CROP
                    }
                )
            }
        }
    }
}
