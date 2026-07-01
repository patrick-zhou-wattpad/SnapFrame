package com.snap.frame

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform