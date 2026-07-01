package com.snap.frame.features.studio.viewmodel

import com.snap.frame.features.studio.model.ItemState
import com.snap.frame.features.studio.model.StudioTool
import com.snap.frame.features.studio.model.TemplateItem
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class StudioViewModel {

    private val _state = MutableStateFlow(
        ItemState(
            defaultTemplates = listOf(
                TemplateItem("default_original", "Original", false),
                TemplateItem("default_bright", "Bright", false),
                TemplateItem("default_warm", "Warm", false)
            )
        )
    )
    val state: StateFlow<ItemState> = _state


    fun selectTool(tool: StudioTool) {
        _state.value = _state.value.copy(selectedTool = tool)
    }

    fun closeTool() {
        _state.value = _state.value.copy(selectedTool = null)
    }

    fun addCustomTemplate(bytes: ByteArray) {
        val newTemplate = TemplateItem(
            id = "custom_${Clock.System.now()}",
            name = "Custom",
            deletable = true,
            imageBytes = bytes
        )

        _state.value = _state.value.copy(
            customTemplates = _state.value.customTemplates + newTemplate
        )
    }

    fun deleteTemplate(id: String) {
        _state.value = _state.value.copy(
            customTemplates = _state.value.customTemplates.filterNot { it.id == id }
        )
    }
}
