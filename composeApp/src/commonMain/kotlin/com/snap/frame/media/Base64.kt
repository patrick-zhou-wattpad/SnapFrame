package com.snap.frame.media

expect fun b64Encode(bytes: ByteArray): String
expect fun b64Decode(b64: String): ByteArray
