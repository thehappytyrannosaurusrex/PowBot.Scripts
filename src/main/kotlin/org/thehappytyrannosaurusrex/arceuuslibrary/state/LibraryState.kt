package org.thehappytyrannosaurusrex.arceuuslibrary.state

import org.thehappytyrannosaurusrex.arceuuslibrary.data.Books
import org.thehappytyrannosaurusrex.arceuuslibrary.data.WorkArea

// Canonical NPC names in the library
object LibraryNpcs {
    const val PROFESSOR = "Professor Gracklebone"
    const val VILLIA = "Villia"
    const val SAM = "Sam"
}

data class ActiveRequest(
    val npcName: String?,
    val rawTitle: String?,
    val book: Books?
)

// High-level script state for the Arceuus Library
class LibraryState {

    var activeRequest: ActiveRequest? = null
    var currentRequestNpc: String? = null  // NPC to ask for next request
    var nextRequestNpc: String? = null     // NPC to talk to after current
    var nextWorkArea: WorkArea = WorkArea.SW_GROUND

    private val recentlyHelped: MutableSet<String> = mutableSetOf()

    fun resetAll() {
        activeRequest = null
        currentRequestNpc = null
        nextRequestNpc = null
        nextWorkArea = WorkArea.SW_GROUND
        recentlyHelped.clear()
    }

    // Called when a new customer request is detected via chat
    fun onCustomerRequested(npcName: String?, rawTitle: String?, book: Books?) {
        activeRequest = ActiveRequest(npcName, rawTitle, book)

        when (npcName?.trim()) {
            LibraryNpcs.PROFESSOR -> {
                currentRequestNpc = LibraryNpcs.PROFESSOR
                nextRequestNpc = LibraryNpcs.VILLIA
            }
            LibraryNpcs.VILLIA -> {
                currentRequestNpc = LibraryNpcs.VILLIA
                nextRequestNpc = LibraryNpcs.PROFESSOR
            }
            LibraryNpcs.SAM -> {
                currentRequestNpc = LibraryNpcs.SAM
                nextRequestNpc = LibraryNpcs.PROFESSOR
            }
            else -> {
                if (currentRequestNpc == null) {
                    currentRequestNpc = npcName?.trim()
                    nextRequestNpc = LibraryNpcs.PROFESSOR
                }
            }
        }
    }

    // Called when we've just fulfilled a request for an NPC
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
                currentRequestNpc = LibraryNpcs.PROFESSOR
                nextRequestNpc = LibraryNpcs.VILLIA
            }
        }
    }

    fun onLayoutReset() = resetAll()

    fun clearActiveRequest() {
        activeRequest = null
    }

    override fun toString(): String =
        "LibraryState(activeRequest=$activeRequest, currentRequestNpc=$currentRequestNpc, " +
                "nextRequestNpc=$nextRequestNpc, nextWorkArea=$nextWorkArea)"
}