package org.thehappytyrannosaurusrex.arceuuslibrary.config

enum class DebugMode {
    NONE,
    COMPREHENSIVE_PATH,
    PATH_STRESS,
    CHAT_PARSER,
    MANUAL_SOLVER,
    CHAT_AND_MANUAL_SOLVER;

    companion object {
        fun fromLabel(label: String?): DebugMode = when (label) {
            Options.Values.DEBUG_COMPREHENSIVE_PATH -> COMPREHENSIVE_PATH
            Options.Values.DEBUG_PATH_STRESS -> PATH_STRESS
            Options.Values.DEBUG_CHAT_PARSER -> CHAT_PARSER
            Options.Values.DEBUG_MANUAL_SOLVER -> MANUAL_SOLVER
            Options.Values.DEBUG_CHAT_AND_MANUAL_SOLVER -> CHAT_AND_MANUAL_SOLVER
            else -> NONE
        }
    }
}