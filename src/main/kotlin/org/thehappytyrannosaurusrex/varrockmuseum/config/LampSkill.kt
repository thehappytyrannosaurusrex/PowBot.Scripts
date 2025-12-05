package org.thehappytyrannosaurusrex.varrockmuseum.config

import org.powbot.api.rt4.walking.model.Skill

enum class LampSkill(val displayName: String, val widgetIndex: Int, val trackedSkill: Skill?) {
    ATTACK("Attack", 2, Skill.Attack),
    STRENGTH("Strength", 3, Skill.Strength),
    RANGED("Ranged", 4, Skill.Ranged),
    MAGIC("Magic", 5, Skill.Magic),
    DEFENCE("Defence", 6, Skill.Defence),
    SAILING("Sailing", 7, Skill.Overall),
    HITPOINTS("Hitpoints", 8, Skill.Hitpoints),
    PRAYER("Prayer", 9, Skill.Prayer),
    AGILITY("Agility", 10, Skill.Agility),
    HERBLORE("Herblore", 11, Skill.Herblore),
    THIEVING("Thieving", 12, Skill.Thieving),
    CRAFTING("Crafting", 13, Skill.Crafting),
    RUNECRAFTING("Runecrafting", 14, Skill.Runecrafting),
    SLAYER("Slayer", 15, Skill.Slayer),
    FARMING("Farming", 16, Skill.Farming),
    MINING("Mining", 17, Skill.Mining),
    SMITHING("Smithing", 18, Skill.Smithing),
    FISHING("Fishing", 19, Skill.Fishing),
    COOKING("Cooking", 20, Skill.Cooking),
    FIREMAKING("Firemaking", 21, Skill.Firemaking),
    WOODCUTTING("Woodcutting", 22, Skill.Woodcutting),
    FLETCHING("Fletching", 23, Skill.Fletching),
    CONSTRUCTION("Construction", 24, Skill.Construction),
    HUNTER("Hunter", 25, Skill.Hunter);

    companion object {
        fun fromDisplayNameOrDefault(name: String?, default: LampSkill): LampSkill =
            if (name == null) default
            else entries.firstOrNull { it.displayName.equals(name, ignoreCase = true) } ?: default
    }
}