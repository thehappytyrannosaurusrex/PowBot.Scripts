package org.thehappytyrannosaurusrex.arceuuslibrary.config

enum class DebugMode(val label: String) {
    NONE(Options.Values.DEBUG_NONE),
    PATH_STRESS_TEST(Options.Values.DEBUG_PATH_STRESS),
    COMPREHENSIVE_PATH_DEBUG(Options.Values.DEBUG_COMPREHENSIVE_PATH),
    CHAT_PARSER_DEBUG(Options.Values.DEBUG_CHAT_PARSER);

    companion object {
        fun fromLabel(label: String?): DebugMode =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: NONE
    }
}
