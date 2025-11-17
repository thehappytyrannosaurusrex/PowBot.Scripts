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

        const val DEBUG_NONE = "DISABLED"
        const val DEBUG_COMPREHENSIVE_PATH = "MULTI-HOP PATH TEST"
        const val DEBUG_PATH_STRESS = "PATH STRESS TEST"
        const val DEBUG_CHAT_PARSER = "MANUAL CHAT PARSER"
        const val DEBUG_MANUAL_SOLVER = "MANUAL LIBRARY SOLVER"
    }
}
