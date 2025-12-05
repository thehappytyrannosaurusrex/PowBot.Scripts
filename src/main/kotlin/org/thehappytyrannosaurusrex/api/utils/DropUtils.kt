package org.thehappytyrannosaurusrex.api.inventory

import org.powbot.api.Condition
import org.powbot.api.Input
import org.powbot.api.Random
import org.powbot.api.rt4.*
import org.thehappytyrannosaurusrex.api.data.WidgetIds
import org.thehappytyrannosaurusrex.api.utils.Logger

object DropUtils {

    private const val TAG = "Drop"

    // -------------------------------------------------------------------------
    // Core Functions
    // -------------------------------------------------------------------------

    fun isTapToDropEnabled(): Boolean = try {
        Inventory.shiftDroppingEnabled()
    } catch (_: Throwable) {
        false
    }

    fun ensureInventoryTab() {
        if (Game.tab() != Game.Tab.INVENTORY) {
            Game.tab(Game.Tab.INVENTORY)
            Condition.sleep(300)
        }
    }

    fun dropItem(item: Item): Boolean {
        if (!item.valid()) return false
        return if (isTapToDropEnabled()) {
            item.click() || item.interact("Drop")
        } else {
            item.interact("Drop") || item.click()
        }
    }

    fun dropItemByName(name: String): Boolean {
        val item = Inventory.stream().name(name).first()
        return dropItem(item)
    }

    // -------------------------------------------------------------------------
    // Tap-to-Drop Management
    // -------------------------------------------------------------------------

    fun enableTapToDrop(): Boolean {
        if (isTapToDropEnabled()) {
            Logger.info(TAG, "ENABLE", "Tap-to-drop already enabled.")
            return true
        }

        Logger.info(TAG, "ENABLE", "Attempting to enable tap-to-drop via settings...")
        return tryEnableViaSettings()
    }

    fun ensureTapToDropEnabled(stopOnFailure: (() -> Unit)? = null): Boolean {
        if (isTapToDropEnabled()) {
            Logger.info(TAG, "CHECK", "Tap-to-drop is ENABLED.")
            return true
        }

        Logger.info(TAG, "CHECK", "Tap-to-drop disabled. Attempting to enable...")
        val success = enableTapToDrop()

        if (!success) {
            Logger.warn(TAG, "CHECK", "Could not enable automatically.")
            Logger.warn(TAG, "CHECK", "Please enable manually: Settings > Controls > Tap to Drop")
            stopOnFailure?.invoke()
        }
        return success
    }

    // -------------------------------------------------------------------------
    // Bulk Drop Operations
    // -------------------------------------------------------------------------

    fun dropAllByName(name: String, delayRange: IntRange = 350..650) {
        ensureInventoryTab()
        Inventory.stream().name(name).forEach { item ->
            if (dropItem(item)) {
                Condition.sleep(Random.nextInt(delayRange.first, delayRange.last))
            }
        }
    }

    fun dropAllExcept(keepNames: Set<String>, delayRange: IntRange = 350..650) {
        ensureInventoryTab()
        val normalized = keepNames.map { it.lowercase().trim() }.toSet()

        Inventory.stream()
            .filter { it.valid() && it.name().lowercase().trim() !in normalized }
            .forEach { item ->
                if (dropItem(item)) {
                    Condition.sleep(Random.nextInt(delayRange.first, delayRange.last))
                }
            }
    }

    fun dropItems(items: List<Item>, delayRange: IntRange = 350..650) {
        ensureInventoryTab()
        items.shuffled().forEach { item ->
            if (item.valid() && dropItem(item)) {
                Condition.sleep(Random.nextInt(delayRange.first, delayRange.last))
            }
        }
    }

    // -------------------------------------------------------------------------
    // Settings UI Interaction
    // -------------------------------------------------------------------------

    private fun tryEnableViaSettings(): Boolean {
        if (!openSettingsTab()) {
            Logger.warn(TAG, "ENABLE", "Failed to open Settings tab.")
            return false
        }

        if (!clickAllSettingsButton()) {
            Logger.warn(TAG, "ENABLE", "Failed to click 'All Settings'.")
            return false
        }

        if (!waitForAllSettingsPanel()) {
            Logger.warn(TAG, "ENABLE", "All Settings panel did not open.")
            return false
        }

        searchForTapToDrop()

        if (!clickTapToDropToggle()) {
            Logger.warn(TAG, "ENABLE", "Failed to click toggle.")
            closeSettingsPanel()
            return false
        }

        Condition.sleep(Random.nextInt(300, 500))
        val success = isTapToDropEnabled()

        if (success) {
            Logger.info(TAG, "ENABLE", "Tap-to-drop enabled via settings UI!")
        } else {
            Logger.warn(TAG, "ENABLE", "Toggle clicked but still disabled.")
        }

        closeSettingsPanel()
        return success
    }

    private fun openSettingsTab(): Boolean {
        if (Game.tab() == Game.Tab.SETTINGS) return true
        if (!Game.tab(Game.Tab.SETTINGS)) return false
        return Condition.wait({ Game.tab() == Game.Tab.SETTINGS }, 200, 15)
    }

    private fun clickAllSettingsButton(): Boolean {
        // Try direct index first
        val button = WidgetIds.SettingsTab.allSettingsButton()
        if (button.valid() && button.visible()) {
            val clicked = button.click() || button.interact("All Settings")
            if (clicked) {
                Condition.sleep(Random.nextInt(400, 700))
                return true
            }
        }

        // Fallback: search by action
        val buttonByAction = WidgetIds.SettingsTab.findAllSettingsButton()
        if (buttonByAction.valid()) {
            val clicked = buttonByAction.click() || buttonByAction.interact("All Settings")
            if (clicked) {
                Condition.sleep(Random.nextInt(400, 700))
                return true
            }
        }

        return false
    }

    private fun waitForAllSettingsPanel(): Boolean {
        val panel = Widgets.widget(WidgetIds.AllSettingsPanel.GROUP)
        return Condition.wait({ panel.valid() }, 200, 20)
    }

    private fun searchForTapToDrop(): Boolean {
        val searchBar = WidgetIds.AllSettingsPanel.searchBar()
        if (!searchBar.valid() || !searchBar.visible()) return false

        val clicked = searchBar.click() || searchBar.interact("Show keyboard")
        if (!clicked) return false

        Condition.sleep(Random.nextInt(400, 600))

        try {
            Input.sendln("tap to")
        } catch (_: Throwable) {
            try {
                Input.send("tap to\n")
            } catch (e: Throwable) {
                Logger.debug(TAG, "ENABLE", "Input failed: ${e.message}")
                return false
            }
        }

        Condition.sleep(Random.nextInt(400, 600))
        dismissKeyboard()
        Condition.sleep(Random.nextInt(400, 600))
        return true
    }

    private fun dismissKeyboard() {
        try {
            val panel = Widgets.widget(WidgetIds.AllSettingsPanel.GROUP)
            if (panel.valid()) {
                val bg = panel.component(0)
                if (bg.valid() && bg.visible()) {
                    bg.click()
                    Condition.sleep(Random.nextInt(200, 400))
                }
            }
        } catch (_: Throwable) { }
    }

    private fun clickTapToDropToggle(): Boolean {
        // Try direct index first
        val toggle = WidgetIds.AllSettingsPanel.tapToDropToggle()
        if (toggle.valid() && toggle.visible()) {
            val clicked = toggle.click() || toggle.interact("Toggle")
            if (clicked) {
                Condition.sleep(Random.nextInt(800, 1300))
                return true
            }
        }

        // Fallback: search by text using Components.stream()
        val toggleByText = WidgetIds.AllSettingsPanel.findTapToDropToggle()
        if (toggleByText.valid()) {
            val clicked = toggleByText.click() || toggleByText.interact("Toggle")
            if (clicked) {
                Condition.sleep(Random.nextInt(900, 1300))
                return true
            }
        }

        return false
    }

    private fun closeSettingsPanel() {
        // Try direct index first
        val closeBtn = WidgetIds.AllSettingsPanel.closeButton()
        if (closeBtn.valid() && closeBtn.visible()) {
            if (closeBtn.click() || closeBtn.interact("Close")) {
                Condition.sleep(Random.nextInt(200, 400))
                return
            }
        }

        // Fallback: search by action
        val closeBtnByAction = WidgetIds.AllSettingsPanel.findCloseButton()
        if (closeBtnByAction.valid()) {
            if (closeBtnByAction.click() || closeBtnByAction.interact("Close")) {
                Condition.sleep(Random.nextInt(200, 400))
                return
            }
        }

        // Last resort: just switch tabs
        Game.tab(Game.Tab.INVENTORY)
        Condition.sleep(Random.nextInt(200, 400))
    }
}