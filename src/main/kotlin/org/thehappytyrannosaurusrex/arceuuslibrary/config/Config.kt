package org.thehappytyrannosaurusrex.arceuuslibrary.config

import org.powbot.api.rt4.Constants

data class Config(
    val xpType: XpType,
    val useGraceful: Boolean,
    val useStamina: Boolean,
    val allowTravelItems: Boolean,
    val stopAtLevel: Int,
    val debugMode: DebugMode,
    val trackedSkillId: Int
) {
    fun summary(): String {
        return buildString {
            append("xpType=${xpType.label}")
            append(", useGraceful=$useGraceful")
            append(", useStamina=$useStamina")
            append(", allowTravelItems=$allowTravelItems")
            if (stopAtLevel > 0) append(", stopAtLevel=$stopAtLevel")
            if (debugMode != DebugMode.NONE) append(", debugMode=${debugMode.name}")
        }
    }
}