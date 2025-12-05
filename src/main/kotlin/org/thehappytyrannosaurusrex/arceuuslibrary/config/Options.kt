package org.thehappytyrannosaurusrex.arceuuslibrary.config

object Options {

    object Keys {
        const val XP_TYPE = "xpType"
        const val USE_GRACEFUL = "useGraceful"
        const val USE_STAMINA = "useStamina"
        const val ALLOW_TRAVEL_ITEMS = "allowTravelItems"
        const val STOP_AT_LEVEL = "stopAtLevel"
        const val DEBUG_MODE = "debugMode"
    }

    object Values {
        // XP Types
        const val MAGIC = "Magic"
        const val RUNECRAFTING = "Runecrafting"

        // Debug Modes
        const val DEBUG_NONE = "None"
        const val DEBUG_COMPREHENSIVE_PATH = "Comprehensive Path"
        const val DEBUG_PATH_STRESS = "Path Stress"
        const val DEBUG_CHAT_PARSER = "Chat Parser"
        const val DEBUG_MANUAL_SOLVER = "Manual Solver"
        const val DEBUG_CHAT_AND_MANUAL_SOLVER = "Chat + Manual Solver"
    }
}