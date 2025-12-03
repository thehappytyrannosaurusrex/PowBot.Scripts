package org.thehappytyrannosaurusrex.api.ui

import org.powbot.api.Condition
import org.powbot.api.rt4.Game
import org.thehappytyrannosaurusrex.api.data.WidgetIds
import org.thehappytyrannosaurusrex.api.utils.Logger

class ViewportUi {

    fun tidyOnStart() {
        minimiseChatBoxIfPossible()
        closeInventoryOverlayIfOpen()
        closeSidePanelIfOpen()
    }

    fun ensureInventoryOpen() {
        if (Game.tab() == Game.Tab.INVENTORY) return
        Game.tab(Game.Tab.INVENTORY)
        Condition.wait({ Game.tab() == Game.Tab.INVENTORY }, 200, 10)
    }

    private fun isChatBoxExpanded(): Boolean {
        val header = WidgetIds.Chatbox.HEADER.component()
        return header.valid() && header.visible()
    }

    private fun isInventoryOverlayOpen(): Boolean {
        val root = WidgetIds.InventoryResizable.ROOT.component()
        return root.valid() && root.visible()
    }

    private fun closeInventoryOverlayIfOpen() {
        try {
            if (!isInventoryOverlayOpen()) return
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
            if (!isChatBoxExpanded()) {
                Logger.info("[Viewport] Chat box already minimised; skipping toggle.")
                return
            }

            val toggleComponent = WidgetIds.ToplevelButtons.CHAT_TOGGLE.component()
            if (!toggleComponent.valid()) return

            val actions = toggleComponent.actions()
            val hasToggleChat = actions.any { it.equals("Toggle chat", ignoreCase = true) }

            Logger.info("[Viewport] Minimising chat box")

            if (hasToggleChat) {
                toggleComponent.click("Toggle chat")
            } else {
                toggleComponent.click()
            }
        } catch (e: Exception) {
            Logger.error("[Viewport] Failed to minimise chat box: ${e.message}")
        }
    }
}