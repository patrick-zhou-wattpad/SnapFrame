package com.snap.frame.features.studio.model

data class TemplateItem(
    val id: String,
    val name: String,
    val deletable: Boolean,
    val imageBytes: ByteArray? = null
)
