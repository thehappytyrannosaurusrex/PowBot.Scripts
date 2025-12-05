package org.thehappytyrannosaurusrex.api.chat

import org.powbot.api.rt4.Chat

/**
 * Small helper that reads the latest NPC / dialogue line from the PowBot Chat API
 */
class ChatLinePoller {

    private var lastMessage: String? = null

    /**
 * Returns a new chat message if one exists, or null if there is nothing new
 */
    fun pollLatest(): String? {
        if (!Chat.chatting()) {
            lastMessage = null
            return null
        }

        val msg = Chat.getChatMessage() ?: return null
        if (msg.isBlank() || msg == lastMessage) {
            return null
        }

        lastMessage = msg
        return msg
    }

    /**
 * Clears the remembered last message. Call if change context
 */
    fun reset() {
        lastMessage = null
    }
}
