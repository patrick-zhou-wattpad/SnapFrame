package com.snap.frame.media

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

class AndroidPhotoSaver(
    private val context: Context
) : PhotoSaver {

    override fun savePhotoToAlbum(
        photoBytes: ByteArray,
        completion: (Boolean) -> Unit
    ) {
        var uri: Uri? = null

        try {
            val resolver = context.contentResolver

            val fileName =
                "snap_frame_${System.currentTimeMillis()}.png"

            val values = ContentValues().apply {
                put(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    fileName
                )
                put(
                    MediaStore.Images.Media.MIME_TYPE,
                    "image/png"
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        "Pictures/SnapFrame"
                    )
                    put(
                        MediaStore.Images.Media.IS_PENDING,
                        1
                    )
                }
            }

            uri = resolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )

            val savedUri = uri

            if (savedUri == null) {
                completion(false)
                return
            }

            resolver.openOutputStream(savedUri)?.use { outputStream ->
                outputStream.write(photoBytes)
            } ?: run {
                resolver.delete(
                    savedUri,
                    null,
                    null
                )

                completion(false)
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val updateValues = ContentValues().apply {
                    put(
                        MediaStore.Images.Media.IS_PENDING,
                        0
                    )
                }

                resolver.update(
                    savedUri,
                    updateValues,
                    null,
                    null
                )
            }

            completion(true)
        } catch (e: Exception) {
            e.printStackTrace()

            uri?.let { failedUri ->
                runCatching {
                    context.contentResolver.delete(
                        failedUri,
                        null,
                        null
                    )
                }
            }

            completion(false)
        }
    }
}
