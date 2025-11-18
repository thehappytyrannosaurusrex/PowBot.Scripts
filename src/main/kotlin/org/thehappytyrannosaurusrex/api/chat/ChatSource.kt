package org.thehappytyrannosaurusrex.api.chat

/**
 * Source of a chat line so scripts can distinguish between in-game server
 * messages and player-visible chat box lines.
 */
enum class ChatSource {
    SERVER,
    CHAT
}
