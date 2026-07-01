package com.snap.frame.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSOperationQueue
import platform.Foundation.dataWithContentsOfURL
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.darwin.NSObject
import platform.posix.memcpy

@Composable
actual fun rememberPhotoPicker(): PhotoPicker {
    return remember { IosPhotoPicker() }
}

private class IosPhotoPicker : PhotoPicker {

    private var callback: ((ByteArray?) -> Unit)? = null

    private val delegate = object : NSObject(), PHPickerViewControllerDelegateProtocol {
        override fun picker(
            picker: PHPickerViewController,
            didFinishPicking: List<*>
        ) {
            picker.dismissViewControllerAnimated(true, completion = null)


            val results = didFinishPicking.filterIsInstance<PHPickerResult>()
            val first = results.firstOrNull()


            if (first == null) {
                callback?.invoke(null)
                callback = null
                return
            }


            val provider = first.itemProvider


            provider.loadFileRepresentationForTypeIdentifier("public.image") { url, err ->
                if (err != null || url == null) {
                    provider.loadDataRepresentationForTypeIdentifier("public.image") { data, err2 ->
                        val bytes =
                            if (err2 != null || data == null) null
                            else data.toByteArrayCompat()
                        callback?.invoke(bytes)
                        callback = null
                    }
                    return@loadFileRepresentationForTypeIdentifier
                }


                val data = NSData.dataWithContentsOfURL(url)
                callback?.invoke(data?.toByteArrayCompat())
                callback = null
            }
        }
    }

    override fun pick(onResult: (ByteArray?) -> Unit) {
        callback = onResult

        val root = topViewController() ?: run {
            callback?.invoke(null)
            callback = null
            return
        }

        val config = PHPickerConfiguration().apply {
            filter = PHPickerFilter.imagesFilter()
            selectionLimit = 1
        }

        val picker = PHPickerViewController(configuration = config).apply {
            delegate = this@IosPhotoPicker.delegate
        }

        // Present on main thread
        NSOperationQueue.mainQueue.addOperationWithBlock {
            root.presentViewController(picker, animated = true, completion = null)
        }
    }
}

private fun topViewController(): UIViewController? {
    val window = keyWindow() ?: return null
    var vc = window.rootViewController ?: return null
    while (true) {
        val presented = vc.presentedViewController
        if (presented != null) vc = presented else break
    }
    return vc
}

private fun keyWindow(): UIWindow? {
    // Simple single-window apps
    return UIApplication.sharedApplication.keyWindow
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArrayCompat(): ByteArray {
    val len = length.toInt()
    if (len <= 0) return ByteArray(0)

    val out = ByteArray(len)
    out.usePinned { pinned ->
        memcpy(
            pinned.addressOf(0),
            bytes?.reinterpret<ByteVar>(),
            length
        )
    }
    return out
}

