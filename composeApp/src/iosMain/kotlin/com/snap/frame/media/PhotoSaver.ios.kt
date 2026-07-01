package com.snap.frame.media

import platform.Foundation.NSData
import platform.Foundation.create
import platform.Photos.PHAccessLevelAddOnly
import platform.Photos.PHAssetChangeRequest
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIImage

// ByteArray -> Base64 String -> NSData -> UIImage -> Photo Album
class IOSPhotoSaver : PhotoSaver {

    override fun savePhotoToAlbum(
        photoBytes: ByteArray,
        completion: (Boolean) -> Unit
    ) {
        try {
            val base64 = b64Encode(photoBytes)

            val data = NSData.create(
                base64EncodedString = base64,
                options = 0u
            )

            if (data == null) {
                completion(false)
                return
            }

            val image = UIImage.imageWithData(data)

            if (image == null) {
                completion(false)
                return
            }

            PHPhotoLibrary.requestAuthorizationForAccessLevel(
                accessLevel = PHAccessLevelAddOnly
            ) { status ->
                if (
                    status != PHAuthorizationStatusAuthorized &&
                    status != PHAuthorizationStatusLimited
                ) {
                    completion(false)
                    return@requestAuthorizationForAccessLevel
                }

                PHPhotoLibrary.sharedPhotoLibrary().performChanges(
                    changeBlock = {
                        PHAssetChangeRequest.creationRequestForAssetFromImage(image)
                    },
                    completionHandler = { success, _ ->
                        completion(success)
                    }
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            completion(false)
        }
    }
}
