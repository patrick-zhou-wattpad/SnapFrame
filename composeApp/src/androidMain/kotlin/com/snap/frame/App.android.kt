import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.snap.frame.features.CropPhotoScreen
import com.snap.frame.features.HomeScreen
import com.snap.frame.features.studio.composable.StudioScreen
import com.snap.frame.media.AndroidPhotoSaver
import com.snap.frame.media.b64Decode
import com.snap.frame.media.b64Encode

private object Routes {
    const val HOME = "home"
    const val CROP = "crop"
    const val STUDIO = "studio"
}

@Composable
actual fun App() {
    val navController = rememberNavController()

    val context = LocalContext.current
    val photoSaver = remember {
        AndroidPhotoSaver(context.applicationContext)
    }

    var originalBytesB64 by rememberSaveable { mutableStateOf<String?>(null) }
    var croppedBytesB64 by rememberSaveable { mutableStateOf<String?>(null) }

    fun encode(bytes: ByteArray): String = b64Encode(bytes)
    fun decode(b64: String): ByteArray = b64Decode(b64)

    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            HomeScreen(
                onPhotoPicked = { bytes ->
                    originalBytesB64 = encode(bytes)
                    croppedBytesB64 = null
                    navController.navigate(Routes.CROP)
                }
            )
        }

        composable(Routes.CROP) {
            val b64 = originalBytesB64
            if (b64 == null) {
                LaunchedEffect(Unit) { navController.popBackStack() }
            } else {
                CropPhotoScreen(
                    photoBytes = decode(b64),
                    onBack = { navController.popBackStack() },
                    onNext = { cropped ->
                        croppedBytesB64 = encode(cropped)
                        navController.navigate(Routes.STUDIO)
                    }
                )
            }
        }

        composable(Routes.STUDIO) {
            val b64 = croppedBytesB64
            if (b64 == null) {
                LaunchedEffect(Unit) { navController.popBackStack() }
            } else {
                StudioScreen(
                    photoBytes = decode(b64),
                    photoSaver = photoSaver,
                    // goes to CROP, which uses ORIGINAL
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
