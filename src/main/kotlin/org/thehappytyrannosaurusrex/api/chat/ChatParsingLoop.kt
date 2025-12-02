package org.thehappytyrannosaurusrex.api.chat

/**
 * Small reusable chat parsing loop that wires together:
 */
class ChatParsingLoop<T>(
    private val poller: ChatLinePoller,
    private val parser: (String, ChatSource) -> T?,
    private val onEvent: (T, ChatSource) -> Unit
) {

    /**
 * Called from script's poll() method to process NPC-style dialogue
 */
    fun tickChat() {
        val msg = poller.pollLatest() ?: return
        val event = parser(msg, ChatSource.CHAT) ?: return
        onEvent(event, ChatSource.CHAT)
    }

    /**
 * Called from script's MessageEvent handler to process server /
 */
    fun handleServerMessage(message: String) {
        val event = parser(message, ChatSource.SERVER) ?: return
        onEvent(event, ChatSource.SERVER)
    }
}
