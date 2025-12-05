package org.thehappytyrannosaurusrex.arceuuslibrary.solver

import org.thehappytyrannosaurusrex.api.chat.ChatLinePoller
import org.thehappytyrannosaurusrex.api.chat.ChatParsingLoop
import org.thehappytyrannosaurusrex.api.chat.ChatSource
import org.thehappytyrannosaurusrex.api.utils.Logger
import org.thehappytyrannosaurusrex.arceuuslibrary.config.DebugMode
import org.thehappytyrannosaurusrex.arceuuslibrary.solver.LibraryChatEvent
import org.thehappytyrannosaurusrex.arceuuslibrary.solver.LibraryChatParser

/**
 * Controls debug behavior for chat parsing and manual solver modes.
 */
class LibraryDebugController(
    private val debugModeProvider: () -> DebugMode,
    private val onEventFromChat: (LibraryChatEvent) -> Unit
) {
    private val chatPoller = ChatLinePoller()

    private val chatLoop = ChatParsingLoop<LibraryChatEvent>(
        poller = chatPoller,
        parser = { msg, source -> LibraryChatParser.parse(msg, source) },
        onEvent = { event, source ->
            Logger.debug("[LibraryDebug] Parsed event: $event from $source")
            onEventFromChat(event)
        }
    )

    /**
     * Called from poll() to process NPC chat dialogue
     */
    fun tickChatDebug() {
        chatLoop.tickChat()
    }

    /**
     * Called from MessageEvent handler to process server messages
     */
    fun onServerMessage(message: String) {
        chatLoop.handleServerMessage(message)
    }

    /**
     * Reset manual solver state
     */
    fun resetManualSolver(reason: String) {
        Logger.debug("[LibraryDebug] Manual solver reset: $reason")
        // TODO: Implement manual solver reset when solver is complete
    }
}