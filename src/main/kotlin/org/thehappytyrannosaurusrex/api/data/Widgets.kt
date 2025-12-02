package org.thehappytyrannosaurusrex.api.data

import org.powbot.api.rt4.Component
import org.powbot.api.rt4.Widget
import org.powbot.api.rt4.Widgets

/**
 * Simple reference to a specific widget component: (group, component[1]], ]).
 */
data class WidgetRef(
    val group: Int,
    val path: IntArray
) {
    constructor(group: Int, vararg path: Int) : this(group, path)

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
        val HEADER = WidgetRef(162, 1)
    }

    /**
 * Resizable inventory sidebar (side inventory pane).
 */
    object InventoryResizable {
        // Root component of the resizable inventory overlay.
        val ROOT = WidgetRef(149, 0)
    }

    /**
 * Resizable spellbook sidebar.
 */
    object SpellbookResizable {
        // Root component of the resizable spellbook overlay.
        val ROOT = WidgetRef(218, 0)
    }

    /**
 * Resizable combat sidebar.
 */
    object CombatResizable {
        // Root component of the resizable combat overlay.
        val ROOT = WidgetRef(593, 0)
    }

    /**
 * Toplevel viewport buttons (minimap, compass, chat toggle, etc).
 */
    object ToplevelButtons {
        // Minimap compass with "Look North/East/South/West" actions.
        val COMPASS = WidgetRef(601, 1)

        // XP Popup
        val MINI_XP_STATS = WidgetRef(601, 3)

        // Chatbox toggle button ("Toggle chat").
        val CHAT_TOGGLE = WidgetRef(601, 46)

        // Chatbox toggle button ("Toggle chat").
        val SKILLS_TOGGLE = WidgetRef(601, 46)

    }

    /**
     * Skills menu widget
     */

    object StatsWidget {
        private const val GROUP = 320
        private const val LVL_SUB_COMPONENT = 4

        val ATTACK = WidgetRef(GROUP, 1)
        val STRENGTH = WidgetRef(GROUP, 2)
        val DEFENCE = WidgetRef(GROUP, 3)
        val RANGE = WidgetRef(GROUP, 4)
        val PRAYER = WidgetRef(GROUP, 5)
        val MAGIC = WidgetRef(GROUP, 6)
        val RUNECRAFTING = WidgetRef(GROUP, 7)
        val CONSTRUCTION = WidgetRef(GROUP, 8)
        val HITPOINTS = WidgetRef(GROUP, 9)
        val AGILITY = WidgetRef(GROUP, 10)
        val HERBLORE = WidgetRef(GROUP, 11)
        val THIEVING = WidgetRef(GROUP, 12)
        val CRAFTING = WidgetRef(GROUP, 13)
        val FLETCHING = WidgetRef(GROUP, 14)
        val SLAYER = WidgetRef(GROUP, 15)
        val HUNTER = WidgetRef(GROUP, 16)
        val MINING = WidgetRef(GROUP, 17)
        val SMITHING = WidgetRef(GROUP, 18)
        val FISHING = WidgetRef(GROUP, 19)
        val COOKING = WidgetRef(GROUP, 20)
        val FIREMAKING = WidgetRef(GROUP, 21)
        val WOODCUTTING = WidgetRef(GROUP, 22)
        val FARMING = WidgetRef(GROUP, 23)
        val SAILING = WidgetRef(GROUP, 24)

        // Small helper so PlayerStats does not need to know sub-component index
        fun levelComponent(ref: WidgetRef): Component =
            ref.component().component(LVL_SUB_COMPONENT)
    }
    /**
     * Antique lamp skill selection interface.
     */
    object AntiqueLamp {
        // Root, just to have the group handy.
        val ROOT = WidgetRef(240, 0)

        // "Confirm: [Skill]" button.
        val CONFIRM = WidgetRef(240, 27)
        // Skill buttons themselves are addressed via LampSkill.widgetIndex.
    }
}


