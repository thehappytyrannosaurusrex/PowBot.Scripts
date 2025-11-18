package org.thehappytyrannosaurusrex.arceuuslibrary.config

enum class DebugMode(val label: String) {
    NONE(Options.Values.DEBUG_NONE),
    COMPREHENSIVE_PATH_DEBUG(Options.Values.DEBUG_COMPREHENSIVE_PATH),
    PATH_STRESS_TEST(Options.Values.DEBUG_PATH_STRESS),
    CHAT_PARSER_DEBUG(Options.Values.DEBUG_CHAT_PARSER),
    MANUAL_SOLVER_DEBUG(Options.Values.DEBUG_MANUAL_SOLVER),   // <-- add this
    CHAT_AND_MANUAL_SOLVER_DEBUG(Options.Values.DEBUG_CHAT_AND_MANUAL_SOLVER);

    companion object {
        fun fromLabel(label: String?): DebugMode =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: NONE
    }
}
