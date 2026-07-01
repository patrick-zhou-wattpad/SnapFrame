package com.snap.frame.features.studio.model

sealed class StudioTool {
    data object Frame : StudioTool()
    data object BackgroundPicture : StudioTool()
    data object AI : StudioTool()
}
