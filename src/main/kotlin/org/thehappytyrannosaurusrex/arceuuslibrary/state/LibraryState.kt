package org.thehappytyrannosaurusrex.arceuuslibrary.state

import org.thehappytyrannosaurusrex.arceuuslibrary.data.Books
import org.thehappytyrannosaurusrex.arceuuslibrary.data.WorkArea

/**
 * Canonical NPC names care about in the library.
 */
object LibraryNpcs {
    const val PROFESSOR = "Professor Gracklebone"
    const val VILLIA = "Villia"
    const val SAM = "Sam"
}

/**
 * High-level, script-owned state for the Arceuus Library.
 */
data class ActiveRequest(
    val npcName: String?,
    val rawTitle: String?,
    val book: Books?
)

/**
 * Coarse-grained areas of the library operate in. These map onto the
 */

class LibraryState {

    /**
 * The current request are actively working on, if any.
 */
    var activeRequest: ActiveRequest? = null

    /**
 * The NPC want to ask for the *next* request ( primary target).
 */
    var currentRequestNpc: String? = null

    /**
 * The NPC plan to talk to *after* the currentRequestNpc.
 */
    var nextRequestNpc: String? = null

    /**
 * The work area want to operate in. start in SW_GROUND by default
 */
    var nextWorkArea: WorkArea = WorkArea.SW_GROUND

    /**
 * Very lightweight memory of NPCs 've recently helped or had
 */
    private val recentlyHelped: MutableSet<String> = mutableSetOf()

    /**
 * Reset all high-level state to the default values as if the script had
 */
    fun resetAll() {
        activeRequest = null
        currentRequestNpc = null
        nextRequestNpc = null
        nextWorkArea = WorkArea.SW_GROUND
        recentlyHelped.clear()
    }

    /**
 * Called when detect a new customer request via chat.
 */
    fun onCustomerRequested(
        npcName: String?,
        rawTitle: String?,
        book: Books?
    ) {
        // Always record the raw request, even if couldn't resolve the book enum yet.
        activeRequest = ActiveRequest(
            npcName = npcName,
            rawTitle = rawTitle,
            book = book
        )

        val name = npcName?.trim()

        when (name) {
            LibraryNpcs.PROFESSOR -> {
                // Ideal pattern: Professor now, Villia next.
                currentRequestNpc = LibraryNpcs.PROFESSOR
                nextRequestNpc = LibraryNpcs.VILLIA
            }

            LibraryNpcs.VILLIA -> {
                // Complement of the above: Villia now, Professor next.
                currentRequestNpc = LibraryNpcs.VILLIA
                nextRequestNpc = LibraryNpcs.PROFESSOR
            }

            LibraryNpcs.SAM -> {
                // Fallback: Sam is less ideal, treat it as a one-off and then
                // Bias ourselves back towards Professor.
                currentRequestNpc = LibraryNpcs.SAM
                nextRequestNpc = LibraryNpcs.PROFESSOR
            }

            else -> {
                // Unknown / generic NPC. If don't have a current target yet,
                // Treat one as current and fall back to Professor as next.
                if (currentRequestNpc == null) {
                    currentRequestNpc = name
                    nextRequestNpc = LibraryNpcs.PROFESSOR
                }
                // If *do* already have a currentRequestNpc, leave it alone.
            }
        }
    }

    /**
 * Called when detect that just recently fulfilled a request for an NPC
 */
    fun markRecentlyHelped(npcName: String) {
        val trimmed = npcName.trim()
        recentlyHelped.add(trimmed)

        when (trimmed) {
            LibraryNpcs.PROFESSOR -> {
                currentRequestNpc = LibraryNpcs.VILLIA
                nextRequestNpc = LibraryNpcs.PROFESSOR
            }

            LibraryNpcs.VILLIA -> {
                currentRequestNpc = LibraryNpcs.PROFESSOR
                nextRequestNpc = LibraryNpcs.VILLIA
            }

            LibraryNpcs.SAM -> {
                // One-off: after Sam, go back into Prof/Villia ping-pong.
                currentRequestNpc = LibraryNpcs.PROFESSOR
                nextRequestNpc = LibraryNpcs.VILLIA
            }
        }
    }

    /**
 * Called when the layout is reset (books shuffled).
 */
    fun onLayoutReset() {
        resetAll()
    }

    /**
 * Clear the current active request without touching any other state.
 */
    fun clearActiveRequest() {
        activeRequest = null
    }

    override fun toString(): String {
        return "LibraryState(" +
                "activeRequest=$activeRequest, " +
                "currentRequestNpc=$currentRequestNpc, " +
                "nextRequestNpc=$nextRequestNpc, " +
                "nextWorkArea=$nextWorkArea" +
                ")"
    }
}
