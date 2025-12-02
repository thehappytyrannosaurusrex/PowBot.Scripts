package org.thehappytyrannosaurusrex.api.chat

/**
 * Very small helper for routing server / game messages to one or more handlers.
 */
class ServerMessageDispatcher {

    private val handlers = mutableListOf<(String) -> Unit>()

    fun register(handler: (String) -> Unit) {
        handlers += handler
    }

    /**
 * Pass a raw message string (nullable). Blank / null messages are ignored.
 */
    fun handle(message: String?) {
        val msg = message?.takeIf { it.isNotBlank() } ?: return
        handlers.forEach { it(msg) }
    }
}
