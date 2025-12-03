package org.thehappytyrannosaurusrex.api.data

import org.powbot.api.rt4.Component
import org.powbot.api.rt4.Widget
import org.powbot.api.rt4.Widgets

data class WidgetRef(
    val group: Int,
    val path: IntArray
) {
    fun widget(): Widget = Widgets.widget(group)

    fun component(): Component {
        require(path.isNotEmpty()) { "WidgetRef path must contain at least one index" }

        var comp: Component = widget().component(path[0])
        for (i in 1 until path.size) {
            comp = comp.component(path[i])
        }
        return comp
    }
}

object WidgetIds {

    object Chatbox {
        val HEADER = WidgetRef(162, intArrayOf(1))
    }

    object InventoryResizable {
        val ROOT = WidgetRef(149, intArrayOf(0))
    }

    object SpellbookResizable {
        val ROOT = WidgetRef(218, intArrayOf(0))
    }

    object CombatResizable {
        val ROOT = WidgetRef(593, intArrayOf(0))
    }

    object ToplevelButtons {
        val COMPASS = WidgetRef(601, intArrayOf(1))
        val MINI_XP_STATS = WidgetRef(601, intArrayOf(3))
        val CHAT_TOGGLE = WidgetRef(601, intArrayOf(46))
        val SKILLS_TOGGLE = WidgetRef(601, intArrayOf(46))
    }

    object StatsWidget {
        private const val GROUP = 320
        private const val LVL_SUB_COMPONENT = 4

        val ATTACK = WidgetRef(GROUP, intArrayOf(1))
        val STRENGTH = WidgetRef(GROUP, intArrayOf(2))
        val DEFENCE = WidgetRef(GROUP, intArrayOf(3))
        val RANGE = WidgetRef(GROUP, intArrayOf(4))
        val PRAYER = WidgetRef(GROUP, intArrayOf(5))
        val MAGIC = WidgetRef(GROUP, intArrayOf(6))
        val RUNECRAFTING = WidgetRef(GROUP, intArrayOf(7))
        val CONSTRUCTION = WidgetRef(GROUP, intArrayOf(8))
        val HITPOINTS = WidgetRef(GROUP, intArrayOf(9))
        val AGILITY = WidgetRef(GROUP, intArrayOf(10))
        val HERBLORE = WidgetRef(GROUP, intArrayOf(11))
        val THIEVING = WidgetRef(GROUP, intArrayOf(12))
        val CRAFTING = WidgetRef(GROUP, intArrayOf(13))
        val FLETCHING = WidgetRef(GROUP, intArrayOf(14))
        val SLAYER = WidgetRef(GROUP, intArrayOf(15))
        val HUNTER = WidgetRef(GROUP, intArrayOf(16))
        val MINING = WidgetRef(GROUP, intArrayOf(17))
        val SMITHING = WidgetRef(GROUP, intArrayOf(18))
        val FISHING = WidgetRef(GROUP, intArrayOf(19))
        val COOKING = WidgetRef(GROUP, intArrayOf(20))
        val FIREMAKING = WidgetRef(GROUP, intArrayOf(21))
        val WOODCUTTING = WidgetRef(GROUP, intArrayOf(22))
        val FARMING = WidgetRef(GROUP, intArrayOf(23))
        val SAILING = WidgetRef(GROUP, intArrayOf(24))

        fun levelComponent(ref: WidgetRef): Component =
            ref.component().component(LVL_SUB_COMPONENT)
    }

    object AntiqueLamp {
        val ROOT = WidgetRef(240, intArrayOf(0))
        val CONFIRM = WidgetRef(240, intArrayOf(27))
    }
}