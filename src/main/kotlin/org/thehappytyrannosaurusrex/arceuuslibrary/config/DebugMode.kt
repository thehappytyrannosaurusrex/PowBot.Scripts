package org.thehappytyrannosaurusrex.arceuuslibrary.config

enum class DebugMode {
    NONE,
    COMPREHENSIVE_PATH_DEBUG,
    PATH_STRESS_TEST,
    CHAT_PARSER_DEBUG,
    MANUAL_SOLVER_DEBUG,
    CHAT_AND_MANUAL_SOLVER_DEBUG;

    companion object {
        fun fromLabel(label: String?): DebugMode = when (label) {
            Options.Values.DEBUG_COMPREHENSIVE_PATH -> COMPREHENSIVE_PATH_DEBUG
            Options.Values.DEBUG_PATH_STRESS -> PATH_STRESS_TEST
            Options.Values.DEBUG_CHAT_PARSER -> CHAT_PARSER_DEBUG
            Options.Values.DEBUG_MANUAL_SOLVER -> MANUAL_SOLVER_DEBUG
            Options.Values.DEBUG_CHAT_AND_MANUAL_SOLVER -> CHAT_AND_MANUAL_SOLVER_DEBUG
            else -> NONE
        }
    }
}