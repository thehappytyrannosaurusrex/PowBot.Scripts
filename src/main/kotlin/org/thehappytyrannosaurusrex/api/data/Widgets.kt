package org.thehappytyrannosaurusrex.api.data

import org.powbot.api.rt4.Component
import org.powbot.api.rt4.Components
import org.powbot.api.rt4.Widgets

/**
 * Widget ID constants and component accessors.
 *
 * Uses two approaches:
 * 1. Direct index access (faster) via component() helper
 * 2. Components.stream() text search (more robust) via findByText()
 */
object WidgetIds {

    // =========================================================================
    // Generic Helpers
    // =========================================================================

    /**
     * Get a component by direct widget group and index path
     */
    fun component(group: Int, vararg path: Int): Component {
        require(path.isNotEmpty()) { "Path must contain at least one index" }
        var comp = Widgets.widget(group).component(path[0])
        for (i in 1 until path.size) {
            comp = comp.component(path[i])
        }
        return comp
    }

    /**
     * Find a component by text within a widget group (more robust to updates)
     */
    fun findByText(group: Int, text: String, exact: Boolean = false): Component {
        return if (exact) {
            Components.stream().widget(group).text(text).viewable().first()
        } else {
            Components.stream().widget(group).textContains(text).viewable().first()
        }
    }

    /**
     * Find a component by action within a widget group
     */
    fun findByAction(group: Int, action: String): Component {
        return Components.stream().widget(group).action(action).viewable().first()
    }

    // =========================================================================
    // Chatbox (162)
    // =========================================================================

    object Chatbox {
        const val GROUP = 162

        fun header(): Component = component(GROUP, 1)
        fun inputField(): Component = component(GROUP, 45)

        // Components.stream() alternatives
        fun findContinue(): Component = findByText(GROUP, "Click here to continue")
    }

    // =========================================================================
    // Inventory (149)
    // =========================================================================

    object Inventory {
        const val GROUP = 149

        fun root(): Component = component(GROUP, 0)
    }

    // =========================================================================
    // Inventory Resizable Overlay (161)
    // =========================================================================

    object InventoryResizable {
        const val GROUP = 161

        fun root(): Component = component(GROUP, 0)
    }

    // =========================================================================
    // Spellbook (218)
    // =========================================================================

    object Spellbook {
        const val GROUP = 218

        fun root(): Component = component(GROUP, 0)

        // Find spell by name using Components.stream()
        fun findSpell(spellName: String): Component = findByText(GROUP, spellName)
    }

    // =========================================================================
    // Spellbook Resizable (218 - same as Spellbook)
    // =========================================================================

    object SpellbookResizable {
        const val GROUP = 218

        fun root(): Component = component(GROUP, 0)

        fun findSpell(spellName: String): Component = findByText(GROUP, spellName)
    }

    // =========================================================================
    // Combat (593)
    // =========================================================================

    object Combat {
        const val GROUP = 593

        fun root(): Component = component(GROUP, 0)
    }

    // =========================================================================
    // Toplevel (601) - Main game frame
    // =========================================================================

    object Toplevel {
        const val GROUP = 601

        fun compass(): Component = component(GROUP, 1)
        fun miniXpStats(): Component = component(GROUP, 3)
        fun chatToggle(): Component = component(GROUP, 46)
    }

    // Alias for backwards compatibility
    object ToplevelButtons {
        const val GROUP = 601

        fun compass(): Component = component(GROUP, 1)
        fun chatToggle(): Component = component(GROUP, 46)

        // Components.stream() alternative
        fun findCompass(): Component = findByAction(GROUP, "Face North")
    }

    // =========================================================================
    // Stats Panel (320)
    // =========================================================================

    object Stats {
        const val GROUP = 320
        private const val LVL_SUB = 4

        // Direct index accessors (faster)
        fun attackLvl(): Component = component(GROUP, 1, LVL_SUB)
        fun strengthLvl(): Component = component(GROUP, 2, LVL_SUB)
        fun defenceLvl(): Component = component(GROUP, 3, LVL_SUB)
        fun rangeLvl(): Component = component(GROUP, 4, LVL_SUB)
        fun prayerLvl(): Component = component(GROUP, 5, LVL_SUB)
        fun magicLvl(): Component = component(GROUP, 6, LVL_SUB)
        fun runecraftingLvl(): Component = component(GROUP, 7, LVL_SUB)
        fun constructionLvl(): Component = component(GROUP, 8, LVL_SUB)
        fun hitpointsLvl(): Component = component(GROUP, 9, LVL_SUB)
        fun agilityLvl(): Component = component(GROUP, 10, LVL_SUB)
        fun herbloreLvl(): Component = component(GROUP, 11, LVL_SUB)
        fun thievingLvl(): Component = component(GROUP, 12, LVL_SUB)
        fun craftingLvl(): Component = component(GROUP, 13, LVL_SUB)
        fun fletchingLvl(): Component = component(GROUP, 14, LVL_SUB)
        fun slayerLvl(): Component = component(GROUP, 15, LVL_SUB)
        fun hunterLvl(): Component = component(GROUP, 16, LVL_SUB)
        fun miningLvl(): Component = component(GROUP, 17, LVL_SUB)
        fun smithingLvl(): Component = component(GROUP, 18, LVL_SUB)
        fun fishingLvl(): Component = component(GROUP, 19, LVL_SUB)
        fun cookingLvl(): Component = component(GROUP, 20, LVL_SUB)
        fun firemakingLvl(): Component = component(GROUP, 21, LVL_SUB)
        fun woodcuttingLvl(): Component = component(GROUP, 22, LVL_SUB)
        fun farmingLvl(): Component = component(GROUP, 23, LVL_SUB)
        fun sailingLvl(): Component = component(GROUP, 24, LVL_SUB)

        // Components.stream() alternative - find skill by name
        fun findSkillByName(skillName: String): Component = findByText(GROUP, skillName)
    }

    // =========================================================================
    // Antique Lamp (240)
    // =========================================================================

    object AntiqueLamp {
        const val GROUP = 240

        fun root(): Component = component(GROUP, 0)
        fun confirm(): Component = component(GROUP, 27)

        // Find skill option by name
        fun findSkill(skillName: String): Component = findByText(GROUP, skillName)
    }

    // =========================================================================
    // Settings Tab (116)
    // =========================================================================

    object Settings {
        const val GROUP = 116

        fun allSettingsButton(): Component = component(GROUP, 32)
    }

    // Alias for backwards compatibility
    object SettingsTab {
        const val GROUP = 116

        fun allSettingsButton(): Component = component(GROUP, 32)

        // Components.stream() alternative
        fun findAllSettingsButton(): Component = findByAction(GROUP, "All Settings")
    }

    // =========================================================================
    // All Settings Panel (134)
    // =========================================================================

    object AllSettings {
        const val GROUP = 134

        fun closeButton(): Component = component(GROUP, 4)
        fun searchBar(): Component = component(GROUP, 11)
        fun tapToDropToggle(): Component = component(GROUP, 167)

        // Components.stream() alternatives
        fun findCloseButton(): Component = findByAction(GROUP, "Close")
        fun findTapToDropToggle(): Component = findByText(GROUP, "Tap to drop")
    }

    // Alias for backwards compatibility
    object AllSettingsPanel {
        const val GROUP = 134

        fun closeButton(): Component = component(GROUP, 4)
        fun searchBar(): Component = component(GROUP, 11)
        fun tapToDropToggle(): Component = component(GROUP, 167)

        // Components.stream() alternatives
        fun findCloseButton(): Component = findByAction(GROUP, "Close")
        fun findTapToDropToggle(): Component = findByText(GROUP, "Tap to drop")
    }

    // =========================================================================
    // Bank (12)
    // =========================================================================

    object Bank {
        const val GROUP = 12

        fun depositInventory(): Component = component(GROUP, 42)
        fun depositEquipment(): Component = component(GROUP, 44)
        fun closeButton(): Component = component(GROUP, 2, 11)

        // Components.stream() alternatives
        fun findDepositInventory(): Component = findByAction(GROUP, "Deposit inventory")
        fun findCloseButton(): Component = findByAction(GROUP, "Close")
    }

    // =========================================================================
    // Deposit Box (192)
    // =========================================================================

    object DepositBox {
        const val GROUP = 192

        fun depositInventory(): Component = component(GROUP, 2)
        fun depositEquipment(): Component = component(GROUP, 4)
    }

    // =========================================================================
    // GE (465)
    // =========================================================================

    object GrandExchange {
        const val GROUP = 465

        fun closeButton(): Component = component(GROUP, 2, 11)
    }

    // =========================================================================
    // Make-X / Production (270)
    // =========================================================================

    object MakeX {
        const val GROUP = 270

        fun makeButton(): Component = component(GROUP, 14)
        fun quantityField(): Component = component(GROUP, 5)

        // Find option by item name
        fun findOption(itemName: String): Component = findByText(GROUP, itemName)
    }

    // =========================================================================
    // Dialogue Options (219)
    // =========================================================================

    object DialogueOptions {
        const val GROUP = 219

        fun option1(): Component = component(GROUP, 1, 0)
        fun option2(): Component = component(GROUP, 1, 1)
        fun option3(): Component = component(GROUP, 1, 2)
        fun option4(): Component = component(GROUP, 1, 3)
        fun option5(): Component = component(GROUP, 1, 4)

        // Find option by text
        fun findOption(text: String): Component = findByText(GROUP, text)
    }

    // =========================================================================
    // NPC Dialogue (231)
    // =========================================================================

    object NpcDialogue {
        const val GROUP = 231

        fun text(): Component = component(GROUP, 4)
        fun npcName(): Component = component(GROUP, 2)
    }

    // =========================================================================
    // Player Dialogue (217)
    // =========================================================================

    object PlayerDialogue {
        const val GROUP = 217

        fun text(): Component = component(GROUP, 4)
    }
}