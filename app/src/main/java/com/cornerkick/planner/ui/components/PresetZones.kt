package com.cornerkick.planner.ui.components

/**
 * Predefined pitch zones expressed in relative coordinates (0f..1f).
 * y = 0 is the goal line at the top of the board; y = 1 is the edge of the
 * playing area at the bottom. These power the simple, stable "tap a zone"
 * placement flow (no fragile drag-and-drop).
 */
data class PresetZone(val name: String, val x: Float, val y: Float)

object PresetZones {
    val all: List<PresetZone> = listOf(
        PresetZone("Corner Arc (Left)", 0.09f, 0.09f),
        PresetZone("Corner Arc (Right)", 0.91f, 0.09f),
        PresetZone("Near Post Zone", 0.40f, 0.16f),
        PresetZone("Far Post Zone", 0.60f, 0.16f),
        PresetZone("Six-Yard Box", 0.50f, 0.13f),
        PresetZone("Goalkeeper Area", 0.50f, 0.07f),
        PresetZone("Penalty Spot Zone", 0.50f, 0.31f),
        PresetZone("Edge Of Box", 0.50f, 0.47f),
    )
}
