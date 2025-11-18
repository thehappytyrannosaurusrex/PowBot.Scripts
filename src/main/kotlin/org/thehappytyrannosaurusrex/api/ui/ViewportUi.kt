package org.thehappytyrannosaurusrex.api.ui

import org.powbot.api.rt4.Components
import org.powbot.api.rt4.Game
import org.powbot.api.rt4.Widget
import org.powbot.api.rt4.Widgets
import org.thehappytyrannosaurusrex.api.utils.Logger

class ViewportUi {

    fun tidyOnStart() {
        minimiseChatBoxIfPossible()
        closeInventoryOverlayIfOpen()
        closeSidePanelIfOpen()
    }

    /**
     * Returns true if the main chatbox is expanded (the row with All/Game/Public is visible),
     * false if the chatbox has been collapsed/hidden by the user.
     */
    private fun isChatBoxExpanded(): Boolean {
        val chatWidget = Widgets.widget(162)
        if (!chatWidget.valid()) {
            return false
        }

        // Component(1) is the top row (All / Game / Public / etc)
        val header = chatWidget.component(1)
        if (!header.valid()) {
            return false
        }

        return header.visible()
    }

    /**
     * Returns true if the inventory overlay (widget 149, component 0) is visible.
     */
    private fun isInventoryOverlayOpen(): Boolean {
        val inventoryWidget = Widgets.widget(149)
        if (!inventoryWidget.valid()) {
            return false
        }

        val root = inventoryWidget.component(0)
        if (!root.valid()) {
            return false
        }

        return root.visible()
    }

    /**
     * Closes the resizable inventory overlay if it's currently open.
     *
     * On some layouts this isn't handled by Game.closeOpenTab(), so we tap
     * widget(149).component(0) directly.
     */
    private fun closeInventoryOverlayIfOpen() {
        try {
            if (!isInventoryOverlayOpen()) {
                return
            }

            val inventoryWidget = Widgets.widget(149)
            val root = inventoryWidget.component(0)

            Logger.info("[Viewport] Closed side panel.")
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
                // Already minimised; do nothing so we don't re-open it.
                Logger.info("[Viewport] Chat box already minimised; skipping toggle.")
                return
            }

            // Widget 601 is the chatbox button group, component 46 is the toggle button
            val widget = Widgets.widget(601)
            if (!widget.valid()) {
                return
            }

            val toggleComponent = widget.component(46)
            if (!toggleComponent.valid()) {
                return
            }

            val actions = toggleComponent.actions()
            val hasToggleChat = actions.any { it.equals("Toggle chat", ignoreCase = true) }

            Logger.info("[Viewport] Minimising chat box via widget(601).component(46).")

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
