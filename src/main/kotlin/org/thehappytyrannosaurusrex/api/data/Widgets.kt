package org.thehappytyrannosaurusrex.api.data

import org.powbot.api.rt4.Component
import org.powbot.api.rt4.Widgets

object WidgetIds {

    // Generic helper to get a component
    fun component(group: Int, vararg path: Int): Component {
        require(path.isNotEmpty()) { "Path must contain at least one index" }
        var comp = Widgets.widget(group).component(path[0])
        for (i in 1 until path.size) {
            comp = comp.component(path[i])
        }
        return comp
    }

    object Chatbox {
        const val GROUP = 162
        fun header(): Component = component(GROUP, 1)
    }

    object Inventory {
        const val GROUP = 149
        fun root(): Component = component(GROUP, 0)
    }

    object Spellbook {
        const val GROUP = 218
        fun root(): Component = component(GROUP, 0)
    }

    object Combat {
        const val GROUP = 593
        fun root(): Component = component(GROUP, 0)
    }

    object Toplevel {
        const val GROUP = 601
        fun compass(): Component = component(GROUP, 1)
        fun miniXpStats(): Component = component(GROUP, 3)
        fun chatToggle(): Component = component(GROUP, 46)
    }

    object Stats {
        const val GROUP = 320
        private const val LVL_SUB = 4

        // Direct level component accessors
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
    }

    object AntiqueLamp {
        const val GROUP = 240
        fun root(): Component = component(GROUP, 0)
        fun confirm(): Component = component(GROUP, 27)
    }

    object Settings {
        const val GROUP = 116
        fun allSettingsButton(): Component = component(GROUP, 32)
    }

    object AllSettings {
        const val GROUP = 134
        fun closeButton(): Component = component(GROUP, 4)
        fun searchBar(): Component = component(GROUP, 11)
        fun tapToDropToggle(): Component = component(GROUP, 167)
    }
}