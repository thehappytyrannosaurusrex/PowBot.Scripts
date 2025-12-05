package org.thehappytyrannosaurusrex.arceuuslibrary.state

import org.thehappytyrannosaurusrex.arceuuslibrary.data.Books
import org.thehappytyrannosaurusrex.arceuuslibrary.data.WorkArea

data class ActiveRequest(
    val npcName: String?,
    val rawTitle: String?,
    val book: Books?
)

class LibraryState {

    var activeRequest: ActiveRequest? = null
    var currentRequestNpc: String? = null
    var nextRequestNpc: String? = null
    var nextWorkArea: WorkArea = WorkArea.SW_GROUND

    private val recentlyHelped: MutableSet<String> = mutableSetOf()

    // Reset all state to defaults
    fun resetAll() {
        activeRequest = null
        currentRequestNpc = null
        nextRequestNpc = null
        nextWorkArea = WorkArea.SW_GROUND
        recentlyHelped.clear()
    }

    // Called when a new customer request is detected
    fun onCustomerRequested(npcName: String?, rawTitle: String?, book: Books?) {
        activeRequest = ActiveRequest(
            npcName = npcName,
            rawTitle = rawTitle,
            book = book
        )

        val name = npcName?.trim()

        when (name) {
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
                    currentRequestNpc = name
                    nextRequestNpc = LibraryNpcs.PROFESSOR
                }
            }
        }
    }

    // Called when library layout resets
    fun onLayoutReset() {
        resetAll()
    }

    // Called when successfully helped an NPC
    fun markRecentlyHelped(npcName: String) {
        recentlyHelped.add(npcName)
    }

    // Check if NPC was recently helped
    fun wasRecentlyHelped(npcName: String): Boolean = npcName in recentlyHelped

    // Clear recently helped status
    fun clearRecentlyHelped() {
        recentlyHelped.clear()
    }

    // Clear the active request (after delivery)
    fun clearActiveRequest() {
        activeRequest = null
    }

    // Rotate to next NPC target
    fun rotateToNextNpc() {
        val next = nextRequestNpc
        if (next != null) {
            currentRequestNpc = next
            nextRequestNpc = when (next) {
                LibraryNpcs.PROFESSOR -> LibraryNpcs.VILLIA
                LibraryNpcs.VILLIA -> LibraryNpcs.PROFESSOR
                else -> LibraryNpcs.PROFESSOR
            }
        }
    }

    override fun toString(): String {
        return "LibraryState(active=${activeRequest?.book?.name}, current=$currentRequestNpc, next=$nextRequestNpc, area=$nextWorkArea)"
    }
}