package org.thehappytyrannosaurusrex.api.inventory

import org.powbot.api.Condition
import org.powbot.api.rt4.Game
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Item

/**
 * Helpers for dropping items using tap-to-drop / shift-drop when available.
 */
object DropUtils {

    /**
 * Returns true if tap-to-drop / shift-dropping appears enabled.
 */
    fun isTapToDropEnabled(): Boolean {
        return try {
            Inventory.shiftDroppingEnabled()
        } catch (_: Throwable) {
            false
        }
    }

    /**
 * Require tap-to-drop / shift-dropping to be enabled.
 */
    fun requireTapToDrop(
        onDisabled: (() -> Unit)? = null
    ): Boolean {
        val enabled = isTapToDropEnabled()
        if (!enabled) {
            onDisabled?.invoke()
        }
        return enabled
    }

    /**
 * Ensure the inventory tab is open before performing inventory-heavy actions.
 */
    fun ensureInventoryTab() {
        if (Game.tab() != Game.Tab.INVENTORY) {
            Game.tab(Game.Tab.INVENTORY)
            Condition.sleep(300)
        }
    }

    /**
 * Drops an item, preferring tap-to-drop when available.
 */
    fun dropItemWithTapSupport(item: Item): Boolean {
        if (!item.valid()) return false

        val tapDropEnabled = isTapToDropEnabled()
        return if (tapDropEnabled) {
            item.click() || item.interact("Drop")
        } else {
            item.interact("Drop") || item.click()
        }
    }
}
