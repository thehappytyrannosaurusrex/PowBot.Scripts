package org.thehappytyrannosaurusrex.arceuuslibrary.config

import org.powbot.api.rt4.Constants
import org.powbot.api.script.tree.TreeScript

data class Config(
    val xpType: XpType,
    val useGraceful: Boolean,
    val useStamina: Boolean,
    val allowTravelItems: Boolean,
    val stopAtLevel: Int,
    val debugMode: DebugMode,
    val trackedSkillId: Int
)

fun TreeScript.buildConfig(): Config {
    val xpTypeLabel = try { getOption<String>(Options.Keys.XP_TYPE) } catch (_: Exception) { null }
    val xpType = XpType.fromLabel(xpTypeLabel)

    val useGraceful = try { getOption<Boolean>(Options.Keys.USE_GRACEFUL) } catch (_: Exception) { false }
    val useStamina = try { getOption<Boolean>(Options.Keys.USE_STAMINA) } catch (_: Exception) { false }
    val allowTravelItems = try { getOption<Boolean>(Options.Keys.ALLOW_TRAVEL_ITEMS) } catch (_: Exception) { true }
    val stopAtLevel = try { getOption<Int>(Options.Keys.STOP_AT_LEVEL) } catch (_: Exception) { 0 }

    val debugLabel = try { getOption<String>(Options.Keys.DEBUG_MODE) } catch (_: Exception) { null }
    val debugMode = DebugMode.fromLabel(debugLabel)

    val trackedSkillId = when (xpType) {
        XpType.MAGIC -> Constants.SKILLS_MAGIC
        XpType.RUNECRAFTING -> Constants.SKILLS_RUNECRAFTING
    }

    return Config(
        xpType = xpType,
        useGraceful = useGraceful,
        useStamina = useStamina,
        allowTravelItems = allowTravelItems,
        stopAtLevel = stopAtLevel,
        debugMode = debugMode,
        trackedSkillId = trackedSkillId
    )
}