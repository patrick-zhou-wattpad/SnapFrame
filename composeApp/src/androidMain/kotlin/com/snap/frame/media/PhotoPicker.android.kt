package com.snap.frame.media

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.io.ByteArrayOutputStream
import java.io.InputStream

private class PhotoPickerCallbackHolder {
    var callback: ((ByteArray?) -> Unit)? = null
}

@Composable
actual fun rememberPhotoPicker(): PhotoPicker {
    val context = LocalContext.current
    val resolver = context.contentResolver

    // Keeps the result callback stable across recompositions
    val callbackHolder = remember {
        PhotoPickerCallbackHolder()
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val bytes = readUriBytes(resolver, uri)

        callbackHolder.callback?.invoke(bytes)
        callbackHolder.callback = null
    }

    return remember(launcher) {
        object : PhotoPicker {
            override fun pick(
                onResult: (ByteArray?) -> Unit
            ) {
                callbackHolder.callback = onResult
                launcher.launch("image/*")
            }
        }
    }
}

private fun readUriBytes(
    resolver: android.content.ContentResolver,
    uri: Uri?
): ByteArray? {
    if (uri == null) return null

    val input: InputStream = resolver.openInputStream(uri) ?: return null

    return try {
        input.readAllBytesCompat()
    } finally {
        input.close()
    }
}

private fun InputStream.readAllBytesCompat(): ByteArray {
    val buffer = ByteArray(8 * 1024)
    val out = ByteArrayOutputStream()
    while (true) {
        val n = read(buffer)
        if (n <= 0) break
        out.write(buffer, 0, n)
    }
    return out.toByteArray()
}