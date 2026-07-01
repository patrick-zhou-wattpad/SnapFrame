package com.snap.frame.media

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSDataBase64DecodingIgnoreUnknownCharacters
import platform.Foundation.base64EncodedStringWithOptions
import platform.Foundation.create
import platform.posix.memcpy


@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual fun b64Encode(bytes: ByteArray): String {
    val data = bytes.usePinned { pinned ->
        NSData.create(
            bytes = pinned.addressOf(0),
            length = bytes.size.toULong()
        )
    }
    return data.base64EncodedStringWithOptions(0uL)
}


@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual fun b64Decode(b64: String): ByteArray {
    val data = NSData.create(
        base64EncodedString = b64,
        options = NSDataBase64DecodingIgnoreUnknownCharacters
    ) ?: return ByteArray(0)


    val len = data.length.toInt()
    if (len == 0) return ByteArray(0)


    val out = ByteArray(len)
    out.usePinned { pinned ->
        memcpy(pinned.addressOf(0), data.bytes, data.length)
    }
    return out
}

