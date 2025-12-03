package org.thehappytyrannosaurusrex.crabtrapping.data

/**
 * Central place for all crab-related game messages and helpers
 * to interpret them.
 */
object CrabMessages {
    // Exact message strings (examples / placeholders)
    const val MSG_TRAP_BAITED_CLICK = "You add some bait to the hinged-lid trap."
    const val MSG_TRAP_BAITED_AUTO = "You setup the crab trap and add some bait to it."
    const val MSG_TRAP_WAIT = "You can't remove the bait right now."
    const val MSG_TRAP_CATCH = "One of your crab traps has been triggered."
    const val MSG_TRAP_EMPTIED = "You empty the crab trap."

    enum class Type {
        TRAP_BAITED_CLICK,
        TRAP_BAITED_AUTO,
        TRAP_CATCH,
        TRAP_EMPTIED,
        TRAP_WAIT
    }

    /**
     * Classify a game message as a crab-related event.
     */
    fun classify(message: String): Type? = when {
        message.contains(MSG_TRAP_BAITED_CLICK, ignoreCase = true) -> Type.TRAP_BAITED_CLICK
        message.contains(MSG_TRAP_BAITED_AUTO, ignoreCase = true)  -> Type.TRAP_BAITED_AUTO
        message.contains(MSG_TRAP_CATCH, ignoreCase = true)        -> Type.TRAP_CATCH
        message.contains(MSG_TRAP_EMPTIED, ignoreCase = true)      -> Type.TRAP_EMPTIED
        message.contains(MSG_TRAP_WAIT, ignoreCase = true)         -> Type.TRAP_WAIT
        else -> null
    }

    fun isCrabMessage(message: String): Boolean = classify(message) != null

}
