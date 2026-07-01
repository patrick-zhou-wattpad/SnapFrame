package com.snap.frame.media

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.io.ByteArrayOutputStream
import java.io.InputStream

@Composable
actual fun rememberPhotoPicker(): PhotoPicker {
    val context = LocalContext.current
    val resolver = context.contentResolver

    var callback: ((ByteArray?) -> Unit)? = remember { null }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        callback?.invoke(readUriBytes(resolver, uri))
    }

    return remember {
        object : PhotoPicker {
            override fun pick(onResult: (ByteArray?) -> Unit) {
                callback = onResult
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
