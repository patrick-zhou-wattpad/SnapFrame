package com.snap.frame.media

interface PhotoSaver {
    fun savePhotoToAlbum(
        photoBytes: ByteArray,
        completion: (Boolean) -> Unit
    )
}
