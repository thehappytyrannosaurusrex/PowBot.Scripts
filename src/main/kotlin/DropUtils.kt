import org.powbot.api.Condition
import org.powbot.api.Input
import org.powbot.api.Random
import org.powbot.api.rt4.Components
import org.powbot.api.rt4.Game
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Item
import org.powbot.api.rt4.Widgets
import org.thehappytyrannosaurusrex.api.data.WidgetIds
import org.thehappytyrannosaurusrex.api.utils.Logger

object DropUtils {

    private const val SCRIPT_NAME = "Drop"

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

    fun enableTapToDrop(): Boolean {
        if (isTapToDropEnabled()) {
            Logger.info(SCRIPT_NAME, "ENABLE", "Tap-to-drop already enabled.")
            return true
        }

        Logger.info(SCRIPT_NAME, "ENABLE", "Attempting to enable tap-to-drop via settings...")
        return tryEnableSettings()
    }

    private fun tryEnableSettings(): Boolean {
        if (!openSettingsTab()) {
            Logger.warn(SCRIPT_NAME, "ENABLE", "Failed to open Settings tab.")
            return false
        }

        if (!clickAllSettingsButton()) {
            Logger.warn(SCRIPT_NAME, "ENABLE", "Failed to click 'All Settings'.")
            return false
        }

        if (!waitForAllSettingsPanel()) {
            Logger.warn(SCRIPT_NAME, "ENABLE", "All Settings panel did not open.")
            return false
        }

        if (!searchForTapToDrop()) {
            Logger.warn(SCRIPT_NAME, "ENABLE", "Failed to search for setting.")
        }

        if (!clickTapToDropToggle()) {
            Logger.warn(SCRIPT_NAME, "ENABLE", "Failed to click toggle.")
            closeSettingsPanel()
            return false
        }

        Condition.sleep(Random.nextInt(300, 500))
        val success = isTapToDropEnabled()

        if (success) {
            Logger.info(SCRIPT_NAME, "ENABLE", "Tap-to-drop enabled via settings UI!")
        } else {
            Logger.warn(SCRIPT_NAME, "ENABLE", "Toggle clicked but still disabled.")
        }

        closeSettingsPanel()
        return success
    }

    fun ensureTapToDropEnabled(stopOnFailure: (() -> Unit)? = null): Boolean {
        if (isTapToDropEnabled()) {
            Logger.info(SCRIPT_NAME, "CHECK", "Tap-to-drop is ENABLED.")
            return true
        }

        Logger.info(SCRIPT_NAME, "CHECK", "Tap-to-drop disabled. Attempting to enable...")

        val success = enableTapToDrop()
        if (!success) {
            Logger.warn(SCRIPT_NAME, "CHECK", "Could not enable automatically.")
            Logger.warn(SCRIPT_NAME, "CHECK", "Please enable manually: Settings > Controls > Tap to Drop")
            stopOnFailure?.invoke()
        }
        return success
    }

    private fun openSettingsTab(): Boolean {
        if (Game.tab() == Game.Tab.SETTINGS) return true
        if (!Game.tab(Game.Tab.SETTINGS)) return false
        return Condition.wait({ Game.tab() == Game.Tab.SETTINGS }, 200, 15)
    }

    private fun clickAllSettingsButton(): Boolean {
        val button = WidgetIds.SettingsTab.ALL_SETTINGS_BUTTON.component()
        if (!button.valid() || !button.visible()) return false

        val clicked = button.click() || button.interact("All Settings")
        if (!clicked) return false

        Condition.sleep(Random.nextInt(400, 700))
        return true
    }

    private fun waitForAllSettingsPanel(): Boolean {
        val panel = Widgets.widget(WidgetIds.AllSettingsPanel.GROUP)
        return Condition.wait({ panel.valid() }, 200, 20)
    }

    private fun searchForTapToDrop(): Boolean {
        val searchBar = WidgetIds.AllSettingsPanel.SEARCH_BAR.component()
        if (!searchBar.valid() || !searchBar.visible()) return false

        val clicked = searchBar.click() || searchBar.interact("Show keyboard")
        if (!clicked) return false

        Condition.sleep(Random.nextInt(400, 600))

        // Type search text - try sendln first (adds newline which may dismiss keyboard)
        // If that fails, use send with newline appended
        try {
            Input.sendln("tap to")
        } catch (_: Throwable) {
            try {
                Input.send("tap to\n")
            } catch (e: Throwable) {
                Logger.debug(SCRIPT_NAME, "ENABLE", "Input failed: ${e.message}")
                return false
            }
        }

        Condition.sleep(Random.nextInt(400, 600))

        // Click panel background to dismiss keyboard if still open
        dismissKeyboardByClickingPanel()

        Condition.sleep(Random.nextInt(400, 600))
        return true
    }

    private fun dismissKeyboardByClickingPanel() {
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
        val toggle = WidgetIds.AllSettingsPanel.TAP_TO_DROP_TOGGLE.component()

        if (toggle.valid() && toggle.visible()) {
            val clicked = toggle.click() || toggle.interact("Toggle")
            if (clicked) {
                Condition.sleep(Random.nextInt(300, 500))
                return true
            }
        }

        return clickTapToDropToggleByText()
    }

    private fun clickTapToDropToggleByText(): Boolean {
        val toggle = Components.stream()
            .widget(WidgetIds.AllSettingsPanel.GROUP)
            .textContains("Tap to drop")
            .viewable()
            .first()

        if (toggle.valid()) {
            val clicked = toggle.click() || toggle.interact("Toggle")
            if (clicked) {
                Condition.sleep(Random.nextInt(800, 1400))
                return true
            }
        }
        return false
    }

    private fun closeSettingsPanel() {
        val closeBtn = WidgetIds.AllSettingsPanel.CLOSE_BUTTON.component()

        if (closeBtn.valid() && closeBtn.visible()) {
            if (closeBtn.click() || closeBtn.interact("Close")) {
                Condition.sleep(Random.nextInt(200, 400))
                return
            }
        }

        Game.tab(Game.Tab.INVENTORY)
        Condition.sleep(Random.nextInt(200, 400))
    }
}