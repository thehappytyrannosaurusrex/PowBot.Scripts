package org.thehappytyrannosaurusrex.arceuuslibrary.config

import org.powbot.api.rt4.Constants

enum class XpType(val label: String, val skillId: Int) {
    MAGIC(Options.Values.MAGIC, Constants.SKILLS_MAGIC),
    RUNECRAFTING(Options.Values.RUNECRAFTING, Constants.SKILLS_RUNECRAFTING);

    companion object {
        fun fromLabel(label: String?): XpType =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: MAGIC
    }
}
