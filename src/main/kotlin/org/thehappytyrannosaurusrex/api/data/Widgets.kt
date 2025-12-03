package org.thehappytyrannosaurusrex.api.data

import org.powbot.api.rt4.Component
import org.powbot.api.rt4.Widget
import org.powbot.api.rt4.Widgets

/**
 * Simple reference to a specific widget path: (group, component indices...).
 */
data class WidgetRef(
    val group: Int,
    // FIX: Changed vararg val path: Int to val path: IntArray for data class compatibility.
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


/**
 * Central place for widget + component IDs used by scripts.
 */
object WidgetIds {

    /**
     * Chatbox (All / Game / Public / Friends / Clan / Trade / etc).
     */
    object Chatbox {
        // Top row tabs (All / Game / Public / ...).
        // FIX: Used intArrayOf(...)
        val HEADER = WidgetRef(162, intArrayOf(1))
    }

    /**
     * Resizable inventory sidebar (side inventory pane).
     */
    object InventoryResizable {
        // Root component of the resizable inventory overlay.
        // FIX: Used intArrayOf(...)
        val ROOT = WidgetRef(149, intArrayOf(0))
    }

    /**
     * Resizable spellbook sidebar.
     */
    object SpellbookResizable {
        // Root component of the resizable spellbook overlay.
        // FIX: Used intArrayOf(...)
        val ROOT = WidgetRef(218, intArrayOf(0))
    }

    /**
     * Resizable combat sidebar.
     */
    object CombatResizable {
        // Root component of the resizable combat overlay.
        // FIX: Used intArrayOf(...)
        val ROOT = WidgetRef(593, intArrayOf(0))
    }

    /**
     * Toplevel viewport buttons (minimap, compass, chat toggle, etc).
     */
    object ToplevelButtons {
        // Minimap compass with "Look North/East/South/West" actions.
        // FIX: Used intArrayOf(...)
        val COMPASS = WidgetRef(601, intArrayOf(1))

        // XP Popup
        // FIX: Used intArrayOf(...)
        val MINI_XP_STATS = WidgetRef(601, intArrayOf(3))

        // Chatbox toggle button ("Toggle chat").
        // FIX: Used intArrayOf(...)
        val CHAT_TOGGLE = WidgetRef(601, intArrayOf(46))

        // Chatbox toggle button ("Toggle chat").
        // FIX: Used intArrayOf(...)
        val SKILLS_TOGGLE = WidgetRef(601, intArrayOf(46))

    }

    /**
     * Skills menu widget
     */

    object StatsWidget {
        private const val GROUP = 320
        private const val LVL_SUB_COMPONENT = 4

        // FIX: Used intArrayOf(...) for all calls
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

        // Small helper so PlayerStats does not need to know sub-component index
        fun levelComponent(ref: WidgetRef): Component =
            ref.component().component(LVL_SUB_COMPONENT)
    }

    /**
     * Antique lamp skill selection interface.
     */
    object AntiqueLamp {
        // Root, just to have the group handy.
        // FIX: Used intArrayOf(...)
        val ROOT = WidgetRef(240, intArrayOf(0))

        // "Confirm: [Skill]" button.
        // FIX: Used intArrayOf(...)
        val CONFIRM = WidgetRef(240, intArrayOf(27))
        // Skill buttons themselves are addressed via LampSkill.widgetIndex.
    }
}