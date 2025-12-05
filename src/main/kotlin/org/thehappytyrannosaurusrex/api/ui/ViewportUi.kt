package org.thehappytyrannosaurusrex.api.ui

import org.powbot.api.Condition
import org.powbot.api.rt4.Game
import org.powbot.api.rt4.Widgets
import org.thehappytyrannosaurusrex.api.data.WidgetIds
import org.thehappytyrannosaurusrex.api.utils.Logger

class ViewportUi {

    // Run all tidy operations on script start
    fun tidyOnStart() {
        minimiseChatBox()
        closeInventoryOverlay()
        closeSidePanel()
    }

    // Ensure inventory tab is open
    fun ensureInventoryOpen() {
        if (Game.tab() == Game.Tab.INVENTORY) return
        Game.tab(Game.Tab.INVENTORY)
        Condition.wait({ Game.tab() == Game.Tab.INVENTORY }, 200, 10)
    }

    // -------------------------------------------------------------------------
    // Private Helpers
    // -------------------------------------------------------------------------

    private fun isChatBoxExpanded(): Boolean {
        val header = WidgetIds.Chatbox.HEADER.component()
        return header.valid() && header.visible()
    }

    private fun isInventoryOverlayOpen(): Boolean {
        val root = WidgetIds.InventoryResizable.ROOT.component()
        return root.valid() && root.visible()
    }

    private fun minimiseChatBox() {
        try {
            if (!isChatBoxExpanded()) {
                Logger.info("[Viewport] Chat box already minimised.")
                return
            }

            val toggle = WidgetIds.ToplevelButtons.CHAT_TOGGLE.component()
            if (!toggle.valid()) return

            val actions = toggle.actions()
            val hasToggle = actions.any { it.equals("Toggle chat", ignoreCase = true) }

            Logger.info("[Viewport] Minimising chat box.")

            if (hasToggle) {
                toggle.click("Toggle chat")
            } else {
                toggle.click()
            }
        } catch (e: Exception) {
            Logger.error("[Viewport] Failed to minimise chat box: ${e.message}")
        }
    }

    private fun closeInventoryOverlay() {
        try {
            if (!isInventoryOverlayOpen()) return
            Logger.info("[Viewport] Closing inventory overlay.")
            Game.closeOpenTab()
        } catch (e: Exception) {
            Logger.error("[Viewport] Failed to close inventory overlay: ${e.message}")
        }
    }

    private fun closeSidePanel() {
        try {
            if (Game.closeOpenTab()) {
                Logger.info("[Viewport] Closed side panel.")
            }
        } catch (e: Exception) {
            Logger.error("[Viewport] Failed to close side panel: ${e.message}")
        }
    }
}