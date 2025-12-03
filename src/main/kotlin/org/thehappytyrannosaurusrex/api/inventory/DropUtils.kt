package org.thehappytyrannosaurusrex.api.inventory

import org.powbot.api.Condition
import org.powbot.api.rt4.Game
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Item

// Helpers for dropping items using tap-to-drop when available
object DropUtils {

    fun isTapToDropEnabled(): Boolean = try {
        Inventory.shiftDroppingEnabled()
    } catch (_: Throwable) {
        false
    }

    fun requireTapToDrop(onDisabled: (() -> Unit)? = null): Boolean {
        val enabled = isTapToDropEnabled()
        if (!enabled) onDisabled?.invoke()
        return enabled
    }

    fun ensureInventoryTab() {
        if (Game.tab() != Game.Tab.INVENTORY) {
            Game.tab(Game.Tab.INVENTORY)
            Condition.sleep(300)
        }
    }

    fun dropItemWithTapSupport(item: Item): Boolean {
        if (!item.valid()) return false
        return if (isTapToDropEnabled()) {
            item.click() || item.interact("Drop")
        } else {
            item.interact("Drop") || item.click()
        }
    }
}