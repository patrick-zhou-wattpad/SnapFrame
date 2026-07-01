package com.snap.frame.media

import android.util.Base64

actual fun b64Encode(bytes: ByteArray): String =
    Base64.encodeToString(bytes, Base64.NO_WRAP)

actual fun b64Decode(b64: String): ByteArray =
    Base64.decode(b64, Base64.NO_WRAP)
