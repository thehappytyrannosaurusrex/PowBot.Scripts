package org.thehappytyrannosaurusrex.arceuuslibrary.config

data class Config(
    val xpType: XpType,
    val useGraceful: Boolean,
    val useStamina: Boolean,
    val allowTravelItems: Boolean,

    val stopAtLevel: Int,
    val debugMode: DebugMode,
) {
    val trackedSkillId: Int get() = xpType.skillId

    fun summary(): String =
        "XP=${xpType.label} (skillId=$trackedSkillId) | " +
                "Graceful=$useGraceful | " +
                "Stamina=$useStamina | " +
                "AllowTravelItems=$allowTravelItems | " +
                "StopAt=${if (stopAtLevel == 0) "disabled" else stopAtLevel} | " +
                "Debug=${debugMode.label}"
}
