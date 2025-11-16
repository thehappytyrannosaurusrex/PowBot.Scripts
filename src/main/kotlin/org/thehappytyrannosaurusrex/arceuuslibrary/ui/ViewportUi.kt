package org.thehappytyrannosaurusrex.arceuuslibrary.ui

import org.powbot.api.rt4.Components
import org.powbot.api.rt4.Game
import org.powbot.api.rt4.Widgets
import org.thehappytyrannosaurusrex.arceuuslibrary.utils.Logger

class ViewportUi {

    fun tidyOnStart() {
        closeSidePanelIfOpen()
        minimiseChatBoxIfPossible()
    }

    private fun isChatBoxExpanded(): Boolean {
        // If any component of widget 162 is viewable, the chatbox is expanded
        val anyChatComponent = Components.stream()
            .widget(162)
            .viewable()
            .first()

        return anyChatComponent.valid()
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
