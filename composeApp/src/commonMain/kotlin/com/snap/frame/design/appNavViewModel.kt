package com.snap.frame.design

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val SnapPrimary = Color(0xFF18403AL)
private val SnapOnPrimary = Color(0xFFFFFFFFL)
private val SnapBackground = Color(0xFFF7F7FBL)
private val SnapSurface = Color(0xFFFFFFFFL)
private val SnapSurfaceVariant = Color(0xFFEDEDF5L)
private val SnapOnSurface = Color(0xFF111827L)
private val SnapOnSurfaceVariant = Color(0xFF374151L)

val SnapLightColors = lightColorScheme(
    primary = SnapPrimary,
    onPrimary = SnapOnPrimary,
    background = SnapBackground,
    surface = SnapSurface,
    surfaceVariant = SnapSurfaceVariant,
    onSurface = SnapOnSurface,
    onSurfaceVariant = SnapOnSurfaceVariant
)

val SnapDarkColors = darkColorScheme(
    primary = SnapPrimary,
    onPrimary = SnapOnPrimary,
    background = Color(0xFF0B1220L),
    surface = Color(0xFF0F172AL),
    surfaceVariant = Color(0xFF1F2A44L),
    onSurface = Color(0xFFE5E7EBL),
    onSurfaceVariant = Color(0xFFCBD5E1L)
)
