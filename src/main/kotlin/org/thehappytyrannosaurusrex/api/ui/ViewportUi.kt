package org.thehappytyrannosaurusrex.api.ui

import org.powbot.api.rt4.Components
import org.powbot.api.rt4.Game
import org.powbot.api.rt4.Widget
import org.powbot.api.rt4.Widgets
import org.thehappytyrannosaurusrex.api.utils.Logger

class ViewportUi {

    fun tidyOnStart() {
        closeSidePanelIfOpen()
        minimiseChatBoxIfPossible()
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

    private fun closeSidePanelIfOpen() {
        try {
            if (Game.closeOpenTab()) {
                Logger.info("[Arceuus Library] UI | Closed side panel")
            }
        } catch (e: Exception) {
            Logger.error("[Arceuus Library] UI | Failed to close side panel: ${e.message}")
        }
    }

    private fun minimiseChatBoxIfPossible() {
        try {
            // Only toggle if the chatbox is currently expanded.
            if (!isChatBoxExpanded()) {
                // Already minimised; do nothing so we don't re-open it.
                Logger.info("[Arceuus Library] UI | Chat box already minimised; skipping toggle.")
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

            Logger.info("[Arceuus Library] UI | Minimising chat box via widget(601).component(46).")

            if (hasToggleChat) {
                toggleComponent.click("Toggle chat")
            } else {
                // Fallback – usually still works as a toggle
                toggleComponent.click()
            }
        } catch (e: Exception) {
            Logger.error("[Arceuus Library] UI | Failed to minimise chat box: ${e.message}")
        }
    }
}
