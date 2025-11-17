package org.thehappytyrannosaurusrex.arceuuslibrary.config

import org.powbot.api.script.tree.TreeScript
import org.thehappytyrannosaurusrex.api.utils.Logger

// Convenience helper to safely read script options with defaults.
private inline fun <reified T> TreeScript.optionOrDefault(key: String, default: T): T = try {
    getOption<T>(key)
} catch (_: IllegalArgumentException) {
    Logger.error("[Arceuus Library] CONFIG | Option '$key' not found; using default '$default'.")
    default
} catch (e: Exception) {
    Logger.error("[Arceuus Library] CONFIG | Failed to read option '$key': ${e.message}. Using default '$default'.")
    default
}

fun TreeScript.buildConfig(): Config {
    val xpTypeLabel = optionOrDefault(Options.Keys.XP_TYPE, Options.Values.MAGIC)
    val xpType = XpType.fromLabel(xpTypeLabel)
    val useGraceful = optionOrDefault(Options.Keys.USE_GRACEFUL, false)
    val useStamina = optionOrDefault(Options.Keys.USE_STAMINA, false)
    val allowTravel = optionOrDefault(Options.Keys.ALLOW_TRAVEL_ITEMS, true)
    val rawStop = optionOrDefault(Options.Keys.STOP_AT_LEVEL, 0)
    val stopAtLevel = if (rawStop == 0) 0 else rawStop.coerceIn(1, 99)
    val debugLabel = optionOrDefault(Options.Keys.DEBUG_MODE, Options.Values.DEBUG_NONE)
    val debugMode = DebugMode.fromLabel(debugLabel)

    return Config(
        xpType = xpType,
        useGraceful = useGraceful,
        useStamina = useStamina,
        allowTravelItems = allowTravel,
        stopAtLevel = stopAtLevel,
        debugMode = debugMode
    ).also {
        Logger.info("[Arceuus Library] CONFIG | ${it.summary()}")
    }
}
