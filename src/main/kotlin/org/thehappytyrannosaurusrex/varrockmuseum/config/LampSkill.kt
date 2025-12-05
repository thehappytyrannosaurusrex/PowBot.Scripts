package org.thehappytyrannosaurusrex.varrockmuseum.config

import org.powbot.api.rt4.walking.model.Skill

enum class LampSkill(
    val displayName: String,
    val widgetIndex: Int,
    val trackedSkill: Skill?
) {
    ATTACK("Attack", 2, Skill.Attack),
    STRENGTH("Strength", 3, Skill.Strength),
    DEFENCE("Defence", 4, Skill.Defence),
    RANGED("Ranged", 5, Skill.Ranged),
    MAGIC("Magic", 6, Skill.Magic),
    HITPOINTS("Hitpoints", 7, Skill.Hitpoints),
    PRAYER("Prayer", 8, Skill.Prayer),
    AGILITY("Agility", 9, Skill.Agility),
    HERBLORE("Herblore", 10, Skill.Herblore),
    THIEVING("Thieving", 11, Skill.Thieving),
    CRAFTING("Crafting", 12, Skill.Crafting),
    FLETCHING("Fletching", 13, Skill.Fletching),
    SLAYER("Slayer", 14, Skill.Slayer),
    HUNTER("Hunter", 15, Skill.Hunter),
    MINING("Mining", 16, Skill.Mining),
    SMITHING("Smithing", 17, Skill.Smithing),
    FISHING("Fishing", 18, Skill.Fishing),
    COOKING("Cooking", 19, Skill.Cooking),
    FIREMAKING("Firemaking", 20, Skill.Firemaking),
    WOODCUTTING("Woodcutting", 21, Skill.Woodcutting),
    FARMING("Farming", 22, Skill.Farming),
    RUNECRAFTING("Runecrafting", 23, Skill.Runecrafting),
    CONSTRUCTION("Construction", 24, Skill.Construction),
    SAILING("Sailing", 25, null); // Sailing has no Skill enum yet

    companion object {
        /**
         * Find a LampSkill by display name (case-insensitive).
         * Returns null if not found.
         */
        fun fromDisplayName(name: String?): LampSkill? {
            if (name == null) return null
            return entries.firstOrNull { it.displayName.equals(name, ignoreCase = true) }
        }

        /**
         * Find a LampSkill by display name, with a default fallback.
         */
        fun fromDisplayNameOrDefault(name: String?, default: LampSkill): LampSkill {
            return fromDisplayName(name) ?: default
        }
    }
}