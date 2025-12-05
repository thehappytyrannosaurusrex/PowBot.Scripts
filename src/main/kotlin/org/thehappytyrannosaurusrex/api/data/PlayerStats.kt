package org.thehappytyrannosaurusrex.api.data

import org.powbot.api.rt4.Component
import org.powbot.api.rt4.Components
import org.powbot.api.rt4.Skills

/**
 * Player stats accessors.
 *
 * Provides two ways to get skill level info:
 * 1. Via widget components (for reading displayed values)
 * 2. Via Skills API (for actual skill data)
 */
object PlayerStats {

    // =========================================================================
    // Widget-based Level Components
    // =========================================================================

    fun attackLvl(): Component = WidgetIds.Stats.attackLvl()
    fun strengthLvl(): Component = WidgetIds.Stats.strengthLvl()
    fun defenceLvl(): Component = WidgetIds.Stats.defenceLvl()
    fun rangeLvl(): Component = WidgetIds.Stats.rangeLvl()
    fun prayerLvl(): Component = WidgetIds.Stats.prayerLvl()
    fun magicLvl(): Component = WidgetIds.Stats.magicLvl()
    fun runecraftingLvl(): Component = WidgetIds.Stats.runecraftingLvl()
    fun constructionLvl(): Component = WidgetIds.Stats.constructionLvl()
    fun hitpointsLvl(): Component = WidgetIds.Stats.hitpointsLvl()
    fun agilityLvl(): Component = WidgetIds.Stats.agilityLvl()
    fun herbloreLvl(): Component = WidgetIds.Stats.herbloreLvl()
    fun thievingLvl(): Component = WidgetIds.Stats.thievingLvl()
    fun craftingLvl(): Component = WidgetIds.Stats.craftingLvl()
    fun fletchingLvl(): Component = WidgetIds.Stats.fletchingLvl()
    fun slayerLvl(): Component = WidgetIds.Stats.slayerLvl()
    fun hunterLvl(): Component = WidgetIds.Stats.hunterLvl()
    fun miningLvl(): Component = WidgetIds.Stats.miningLvl()
    fun smithingLvl(): Component = WidgetIds.Stats.smithingLvl()
    fun fishingLvl(): Component = WidgetIds.Stats.fishingLvl()
    fun cookingLvl(): Component = WidgetIds.Stats.cookingLvl()
    fun firemakingLvl(): Component = WidgetIds.Stats.firemakingLvl()
    fun woodcuttingLvl(): Component = WidgetIds.Stats.woodcuttingLvl()
    fun farmingLvl(): Component = WidgetIds.Stats.farmingLvl()
    fun sailingLvl(): Component = WidgetIds.Stats.sailingLvl()

    // =========================================================================
    // Components.stream() based lookups (more robust to widget ID changes)
    // =========================================================================

    /**
     * Find a skill level component by searching for the skill name
     */
    fun findSkillByName(skillName: String): Component {
        return Components.stream()
            .widget(WidgetIds.Stats.GROUP)
            .textContains(skillName)
            .viewable()
            .first()
    }
}