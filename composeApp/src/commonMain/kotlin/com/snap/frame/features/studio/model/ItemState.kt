package com.snap.frame.features.studio.model

data class ItemState(
    val selectedTool: StudioTool? = null,
    val defaultTemplates: List<TemplateItem> = emptyList(),
    val customTemplates: List<TemplateItem> = emptyList(),
    val selectedTemplateIndex: Int = 0
)
