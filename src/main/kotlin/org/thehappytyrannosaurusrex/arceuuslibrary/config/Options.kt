package org.thehappytyrannosaurusrex.arceuuslibrary.config

// --- Options model ---
object Options {
    object Keys {
        const val XP_TYPE = "XP Type"
        const val USE_GRACEFUL = "Use Graceful Set"
        const val USE_STAMINA = "Use Stamina Potions"
        const val ALLOW_TRAVEL_ITEMS = "Allow Travel Items"
        const val STOP_AT_LEVEL = "Stop At Target Level"
        const val DEBUG_MODE = "Debug Mode"
    }

    object Values {
        const val MAGIC = "Magic"
        const val RUNECRAFTING = "Runecrafting"

        const val DEBUG_NONE = "Disabled"
        const val DEBUG_PATH_STRESS = "Path stress test"
        const val DEBUG_COMPREHENSIVE_PATH = "Comprehensive path debug"
    }
}
