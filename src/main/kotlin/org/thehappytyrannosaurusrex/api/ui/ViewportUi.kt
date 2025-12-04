package org.thehappytyrannosaurusrex.api.ui

import org.powbot.api.Condition
import org.powbot.api.rt4.Game
import org.thehappytyrannosaurusrex.api.data.WidgetIds
import org.powbot.api.rt4.Widgets
import org.thehappytyrannosaurusrex.api.utils.Logger

class ViewportUi {

    fun tidyOnStart() {
        minimiseChatBoxIfPossible()
        closeInventoryOverlayIfOpen()
        closeSidePanelIfOpen()
    }

    /**
 * Ensure the main inventory tab is open.
 */
    fun ensureInventoryOpen() {
        if (Game.tab() == Game.Tab.INVENTORY) {
            return
        }

        Game.tab(Game.Tab.INVENTORY)
        Condition.wait(
            { Game.tab() == Game.Tab.INVENTORY },
            200,
            10
        )
    }

    /**
 * Returns true if the main chatbox is expanded (the row with All/Game/Public is visible),
 */
    private fun isChatBoxExpanded(): Boolean {
        val header = WidgetIds.Chatbox.HEADER.component()
        if (!header.valid()) {
            return false
        }

        return header.visible()
    }


    /**
 * Returns true if the inventory overlay (resizable side-pane) is visible.
 */
    private fun isInventoryOverlayOpen(): Boolean {
        val root = WidgetIds.InventoryResizable.ROOT.component()
        if (!root.valid()) {
            return false
        }

        return root.visible()
    }

    /**
 * Closes the resizable inventory overlay if it's currently open.
 */
    private fun closeInventoryOverlayIfOpen() {
        try {
            if (!isInventoryOverlayOpen()) {
                return
            }

            Logger.info("[Viewport] Closing side panel via Game.closeOpenTab().")
            Game.closeOpenTab()
        } catch (e: Exception) {
            Logger.error("[Viewport] Failed to close side panel: ${e.message}")
        }
    }


    private fun closeSidePanelIfOpen() {
        try {
            if (Game.closeOpenTab()) {
                Logger.info("[Viewport] Closed side panel")
            }
        } catch (e: Exception) {
            Logger.error("[Viewport] Failed to close side panel: ${e.message}")
        }
    }

    private fun minimiseChatBoxIfPossible() {
        try {
            // Only toggle if the chatbox is currently expanded.
            if (!isChatBoxExpanded()) {
                // Already minimised; do nothing so don't re-open it.
                Logger.info("[Viewport] Chat box already minimised; skipping toggle.")
                return
            }

            val toggleComponent = WidgetIds.ToplevelButtons.CHAT_TOGGLE.component()
            if (!toggleComponent.valid()) {
                return
            }

            val actions = toggleComponent.actions()
            val hasToggleChat = actions.any { it.equals("Toggle chat", ignoreCase = true) }

            Logger.info(
                "[Viewport] Minimising chat box via " +
                        "widget(${WidgetIds.ToplevelButtons.CHAT_TOGGLE.group})." +
                        "component(${WidgetIds.ToplevelButtons.CHAT_TOGGLE.path})."
            )

            if (hasToggleChat) {
                toggleComponent.click("Toggle chat")
            } else {
                // Fallback – usually still works as a toggle
                toggleComponent.click()
            }
        } catch (e: Exception) {
            Logger.error("[Viewport] Failed to minimise chat box: ${e.message}")
        }
    }
}
