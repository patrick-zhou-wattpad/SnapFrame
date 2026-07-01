package com.snap.frame.media

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore

class AndroidPhotoSaver(
    private val context: Context
) : PhotoSaver {

    override fun savePhotoToAlbum(
        photoBytes: ByteArray,
        completion: (Boolean) -> Unit
    ) {
        try {
            val resolver = context.contentResolver
            val fileName = "snap_frame_${System.currentTimeMillis()}.jpg"

            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/SnapFrame")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val uri = resolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )

            if (uri == null) {
                completion(false)
                return
            }

            resolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(photoBytes)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }

            completion(true)
        } catch (e: Exception) {
            e.printStackTrace()
            completion(false)
        }
    }
}
